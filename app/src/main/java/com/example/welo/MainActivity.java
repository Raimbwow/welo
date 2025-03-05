package com.example.welo;
import static com.google.android.gms.maps.CameraUpdateFactory.zoomTo;


import com.example.welo.R;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.Priority;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.pm.PackageManager;
import android.Manifest;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import androidx.core.app.ActivityCompat;
import org.osmdroid.util.BoundingBox;
import org.osmdroid.views.overlay.MapEventsOverlay;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import java.io.IOException;
import java.util.ArrayList;

import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import java.util.List;
import java.util.Locale;

import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.views.overlay.Polyline;

public class MainActivity extends AppCompatActivity {
    private MapView mapView = null;
    private Marker userMarker; // Pointeur de l'utilisateur
    private Marker searchMarker; // Pointeur pour la recherche
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private static final int REQUEST_PERMISSIONS = 1;
    ArrayList<OverlayItem> items = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        //handle permissions first, before map is created. not depicted here

        //load/initialize the osmdroid configuration, this can be done
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        //setting this before the layout is inflated is a good idea
        //it 'should' ensure that the map has a writable location for the map cache, even without permissions
        //if no tiles are displayed, you can try overriding the cache path using Configuration.getInstance().setCachePath
        //see also StorageUtils
        //note, the load method also sets the HTTP User Agent to your application's package name, abusing osm's
        //tile servers will get you banned based on this string

        // Configuration de OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(new File(getCacheDir(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(getCacheDir(), "osmdroid/tiles"));

        //inflate and create the map
        setContentView(R.layout.activity_main);

        // Initialisation de la MapView
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.OpenTopo);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Vérifie si des marqueurs existent déjà et ajuste le zoom
        adjustZoom();

        CompassOverlay mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapView);
        mCompassOverlay.enableCompass();
        mapView.getOverlays().add(mCompassOverlay);

        // Centrer la carte sur une position initiale (Paris)
        mapView.getController().setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(48.39, -4.48); // Coordonnées de Brest
        mapView.getController().setCenter(startPoint);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    updateUserLocation(new GeoPoint(location.getLatitude(), location.getLongitude()));
                }
            }
        };
        // Demander les permissions nécessaires

        // IMPORTANT PROBLEME A REGLER
        //*********************************************************//
         requestPermissionsIfNecessary(new String []{
                Manifest.permission.ACCESS_FINE_LOCATION,
                 Manifest.permission.WRITE_EXTERNAL_STORAGE}
        );
        //*********************************************************************

        startLocationUpdates();

        // Configuration de la barre de recherche
        SearchView searchView = findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchLocation(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //NavigationUI.setupWithNavController(binding.navView, navController);
        //your items
        ArrayList<OverlayItem> items = new ArrayList<>();
        items.add(new OverlayItem("Title", "Description", new GeoPoint(48.400002, -4.48333))); // Lat/Lon decimal degrees
        items.add(new OverlayItem("Title", "Description", new GeoPoint(48.400002, -4.48)));
        // Ajoutez un écouteur de clic sur la carte

        MapEventsOverlay events = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                Toast.makeText(getApplicationContext(), "Tap on (" + p.getLatitude() + "," + p.getLongitude() + ")", Toast.LENGTH_SHORT).show();
                items.add(new OverlayItem("Title", "Description", new GeoPoint(48.400002, -4.40)));
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                GeoPoint click = p;
                Toast.makeText(getApplicationContext(), "Long press on (" + p.getLatitude() + "," + p.getLongitude() + ")", Toast.LENGTH_SHORT).show();
                mapView.invalidate();
                showMenu(p);
                return false;
            }
        });
        mapView.getOverlayManager().add(events);
        // Supposons que vous avez une ListView ou une autre vue sur laquelle vous voulez le menu contextuel
        registerForContextMenu(mapView);
        };
    // Gérer les événements de carte (tap et long press)
    // Gestion des résultats des demandes de permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                // Informer l'utilisateur et fermer l'application si les permissions sont refusées
                finish();
            }
        }
    }

    @SuppressLint("MissingPermission")
    public void calculateRoute(MapView mapView, GeoPoint p) {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();

                        ArrayList<GeoPoint> waypoints = new ArrayList<>();
                        waypoints.add(new GeoPoint(latitude, longitude)); // Position actuelle
                        waypoints.add(p); // Destination

                        Toast.makeText(this, "Latitude: " + latitude + ", Longitude: " + longitude, Toast.LENGTH_LONG).show();

                        // Calcul de la route après avoir obtenu la position
                        OSRMRoadManager roadManager = new OSRMRoadManager(this, "welo/0 (Linux; Android 12; Pixel 5)");
                        roadManager.setMean(OSRMRoadManager.MEAN_BY_BIKE); // Mode vélo
                        Road road = roadManager.getRoad(waypoints);

                        if (road == null || road.mStatus != Road.STATUS_OK) {
                            Log.e("RoutingError", "Failed to calculate the road.");
                            return;
                        }

                        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
                        mapView.getOverlays().add(roadOverlay);

                        @SuppressLint("UseCompatLoadingForDrawables")
                        Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);

                        for (int i = 0; i < road.mNodes.size(); i++) {
                            RoadNode node = road.mNodes.get(i);
                            Marker nodeMarker = new Marker(mapView);
                            nodeMarker.setPosition(node.mLocation);
                            nodeMarker.setIcon(nodeIcon);
                            nodeMarker.setTitle("Step " + i);
                            mapView.getOverlays().add(nodeMarker);
                        }

                        mapView.invalidate(); // Rafraîchir la carte

                    } else {
                        Toast.makeText(this, "Impossible de récupérer la position", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000); // Mise à jour toutes les 5 secondes
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
        } else {
            requestPermissionsIfNecessary(new String[]{Manifest.permission.ACCESS_FINE_LOCATION});
        }
    }

    private void updateUserLocation(GeoPoint userLocation) {
        if (mapView == null) return; // Vérifie que la carte est bien initialisée

        if (userMarker == null) {
            userMarker = new Marker(mapView);
            userMarker.setTitle("Vous êtes ici");
            userMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(userMarker);
        }

        userMarker.setPosition(userLocation);
        mapView.invalidate(); // Rafraîchit la carte
    }

    private void searchLocation(String location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = geocoder.getFromLocationName(location, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                GeoPoint geoPoint = new GeoPoint(address.getLatitude(), address.getLongitude());

                if (mapView == null) return;

                if (searchMarker != null) {
                    mapView.getOverlays().remove(searchMarker);
                }

                calculateRoute(mapView, geoPoint);

                searchMarker = new Marker(mapView);
                searchMarker.setPosition(geoPoint);
                searchMarker.setTitle("Destination");
                searchMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                mapView.getOverlays().add(searchMarker);

                adjustZoom();

                mapView.invalidate();
            } else {
                Toast.makeText(this, "Lieu introuvable", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erreur de recherche", Toast.LENGTH_SHORT).show();
        }
    }

    private void adjustZoom() {
        if (mapView == null) return;
        if (userMarker != null && searchMarker != null) {
            // Désactive temporairement l'auto-zoom de la localisation pour éviter le recentrage constant
            fusedLocationClient.removeLocationUpdates(locationCallback);

            // Ajuste le zoom pour voir les deux points
            double minLat = Math.min(userMarker.getPosition().getLatitude(), searchMarker.getPosition().getLatitude());
            double maxLat = Math.max(userMarker.getPosition().getLatitude(), searchMarker.getPosition().getLatitude());
            double minLon = Math.min(userMarker.getPosition().getLongitude(), searchMarker.getPosition().getLongitude());
            double maxLon = Math.max(userMarker.getPosition().getLongitude(), searchMarker.getPosition().getLongitude());
            double latDiff = maxLat - minLat;
            double lonDiff = maxLon - minLon;
            double paddingFactorLat = latDiff * 0.5;
            double paddingFactorLon = lonDiff * 0.5;

            BoundingBox boundingBox = new BoundingBox(
                    maxLat + paddingFactorLat, maxLon + paddingFactorLon,
                    minLat - paddingFactorLat, minLon - paddingFactorLon
            );
            mapView.zoomToBoundingBox(boundingBox, true);
        } else if (userMarker != null) {
            // Zoom sur la position de l'utilisateur s'il est le seul point
            mapView.getController().setCenter(userMarker.getPosition());
            mapView.getController().setZoom(18.0);
        } else if (searchMarker != null) {
            // Zoom sur la destination si elle est le seul point
            mapView.getController().setCenter(searchMarker.getPosition());
            mapView.getController().setZoom(18.0);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            int REQUEST_PERMISSIONS_REQUEST_CODE = 10;
            ActivityCompat.requestPermissions(
                    this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 1);
        }
    }

    // Gérer le cycle de vie pour libérer les ressources liées à la carte
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDetach(); // Libérer les ressources de la MapView
        }
        if (fusedLocationClient != null && locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        // Vérifiez que c'est bien la MapView qui a déclenché le menu
        if (v.getId() == R.id.map) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu); // Charger le fichier XML
    }}

    private void addMarker(GeoPoint point) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle("Marqueur ici");
        mapView.getOverlays().add(marker);
        mapView.invalidate(); // Re-dessiner la carte
    }

    private void zoomTo(GeoPoint point) {
        mapView.getController().setZoom(18.0);
        mapView.getController().setCenter(point);
    }

    @SuppressLint("NonConstantResourceId")
    private void showMenu(GeoPoint p) {
        // Créer un PopupMenu
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.map));
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.context_menu, popupMenu.getMenu());
        // Gérer les clics sur les options du menu
        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.item12) {
                addMarker(p); // Ajouter un marqueur à l'endroit du clic long
                return true;
            } else if (item.getItemId() == R.id.item22) {
                zoomTo(p); // Zoomer sur le point
                return true;
            }
            return false;
        });
        popupMenu.show();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.item12) {
            GeoPoint point = new GeoPoint(48.400002, -4.48333);
            Marker marker = new Marker(mapView);
            marker.setPosition(point);
            marker.setTitle("Nouveau marqueur");
            mapView.getOverlays().add(marker);
            mapView.invalidate();
            return true;
        } else if (id == R.id.item22) {
            mapView.getController().zoomOut();
            return true;
        } else {
            return super.onContextItemSelected(item);
        }
    }
    }

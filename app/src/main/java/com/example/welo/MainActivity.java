package com.example.welo;

import android.app.AlertDialog;
import android.annotation.SuppressLint;
import android.os.Bundle;
import com.example.welo.R;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.widget.EditText;
import android.content.pm.PackageManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

import java.util.List;
import java.util.ArrayList;

import androidx.core.app.ActivityCompat;
import androidx.viewbinding.ViewBinding;

import org.osmdroid.views.overlay.GroundOverlay;

import org.osmdroid.views.overlay.MapEventsOverlay;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import java.util.ArrayList;


import android.Manifest;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.PopupMenu;
import android.widget.Toast;


import androidx.annotation.NonNull;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Marker;


import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import com.graphhopper.GHRequest;
import com.graphhopper.GHResponse;
import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import com.graphhopper.util.shapes.GHPoint;

import java.io.File;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import java.lang.reflect.Array;
import java.util.ArrayList;
import com.example.welo.databinding.ActivityMainBinding;

import org.osmdroid.bonuspack.clustering.RadiusMarkerClusterer;
import org.osmdroid.bonuspack.clustering.StaticCluster;
import org.osmdroid.bonuspack.kml.KmlDocument;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.KmlFolder;
import org.osmdroid.bonuspack.kml.KmlLineString;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPoint;
import org.osmdroid.bonuspack.kml.KmlPolygon;
import org.osmdroid.bonuspack.kml.KmlTrack;
import org.osmdroid.bonuspack.kml.Style;
import org.osmdroid.bonuspack.location.NominatimPOIProvider;
import org.osmdroid.bonuspack.location.OverpassAPIProvider;
import org.osmdroid.bonuspack.location.POI;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.bonuspack.utils.BonusPackHelper;
import org.osmdroid.views.overlay.Polyline;

public class MainActivity extends AppCompatActivity {

    private MapView mapView = null;

    private static final int REQUEST_PERMISSIONS = 1;

    ArrayList<OverlayItem> items = new ArrayList<>();
    private int selectedIconRes = 0; // Stocke l'icône choisie par l'utilisateur
    private final int selectedIconIndex = -1; // Index du type d'événement sélectionné
    private long longPressStartTime = 0;
    public class CustomInfoWindow extends MarkerInfoWindow {
        public CustomInfoWindow(MapView mapView) {
            //super(org.osmdroid.library.R.layout.bonuspack_bubble, mapView);
            super(R.layout.custom_info_window, mapView);
        }
    }
    private void showAddMarkerDialog(GeoPoint point) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ajouter un événement");

        // Ajouter un champ de texte pour la description
        final EditText input = new EditText(this);
        input.setHint("Description de l'événement");
        builder.setView(input);

        // Liste des types d'événements
        String[] eventTypes = {"Accident", "Travaux", "Radar"};
        Integer[] eventIcons = {R.drawable.accident, R.drawable.construction, R.drawable.panneau_2};

        builder.setSingleChoiceItems(eventTypes, -1, (dialog, which) -> {
            selectedIconRes = eventIcons[which]; // Sauvegarde l'icône choisie
        });

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String description = input.getText().toString();
            if (!description.isEmpty() && selectedIconRes != 0) {
                addMarker(point, eventTypes[selectedIconIndex], description, selectedIconRes);
            } else {
                Toast.makeText(this, "Veuillez entrer une description et choisir un type", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Annuler", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void resetMap() {
        // Récupérer les overlays actuels
        List<Overlay> overlays = mapView.getOverlays();

        // Créer une nouvelle liste qui ne contiendra que les éléments interactifs
        List<Overlay> toKeep = new ArrayList<>();

        for (Overlay overlay : overlays) {
            // Vérifier si l'overlay est un gestionnaire d'interactions (ex: écouteur de clics)
            if (overlay instanceof MapEventsOverlay) {
                toKeep.add(overlay); // Ne pas supprimer les écouteurs d'événements
            }
        }

        // Remplacer les overlays actuels par ceux qu'on garde
        mapView.getOverlays().clear();
        mapView.getOverlays().addAll(toKeep);

        // Rafraîchir la carte
        mapView.invalidate();
    }

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
  
        CompassOverlay mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapView);
        mCompassOverlay.enableCompass();
        mapView.getOverlays().add(mCompassOverlay);

        // Centrer la carte sur une position initiale (Paris)
        mapView.getController().setZoom(15.0);
        GeoPoint startPoint = new GeoPoint(48.39, -4.48); // Coordonnées de Brest
        mapView.getController().setCenter(startPoint);

        // Ajouter un marqueur à la position initiale
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        startMarker.setTitle("Vous êtes ici !");
        mapView.getOverlays().add(startMarker);

        // Demander les permissions nécessaires

        // IMPORTANT PROBLEME A REGLER
        //*********************************************************//
         requestPermissionsIfNecessary(new String []{
                Manifest.permission.ACCESS_FINE_LOCATION,
                 Manifest.permission.WRITE_EXTERNAL_STORAGE}
        );
        //*********************************************************************

            //NavigationUI.setupWithNavController(binding.navView, navController);
            //your items
            ArrayList<OverlayItem> items = new ArrayList<>();
            items.add(new OverlayItem("Title", "Description", new GeoPoint(48.400002, -4.48333))); // Lat/Lon decimal degrees
            items.add(new OverlayItem("Title", "Description", new GeoPoint(48.400002, -4.48)));
            // Ajoutez un écouteur de clic sur la carte
            calculateRoute(mapView);

        MapEventsOverlay events = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
// Affiche un Toast avec les coordonnées
                Toast.makeText(getApplicationContext(), "Tap sur (" + p.getLatitude() + ", " + p.getLongitude() + ")", Toast.LENGTH_SHORT).show();

                // Création d’un OverlayItem
                OverlayItem overlayItem = new OverlayItem(
                        "Point tapé",
                        "Coordonnées : " + p.getLatitude() + ", " + p.getLongitude(),
                        p
                );

                // Ajout d'une icône personnalisée pour le marqueur
                overlayItem.setMarker(ContextCompat.getDrawable(getApplicationContext(), R.drawable.panneau_2));

                // Ajout à la liste des items et rafraîchissement
                items.add(overlayItem);
                mapView.invalidate();

                return true;

            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                GeoPoint click = p;
                Toast.makeText(getApplicationContext(), "Long press on (" + p.getLatitude() + "," + p.getLongitude() + ")", Toast.LENGTH_SHORT).show();
                GroundOverlay myGroundOverlay = new GroundOverlay();
                /*myGroundOverlay.setPosition(click,p);
                myGroundOverlay.setImage(getResources().getDrawable(R.drawable.ic_launcher).mutate());
                myGroundOverlay.set(2000.0f);
                map.getOverlays().add(myGroundOverlay);*/

                mapView.invalidate();

                showMenu(p);
                return false;
            }

        });
        mapView.getOverlayManager().add(events);



        // Supposons que vous avez une ListView ou une autre vue sur laquelle vous voulez le menu contextuel


        registerForContextMenu(mapView);




        mapView.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    longPressStartTime = System.currentTimeMillis();
                    break;

                case MotionEvent.ACTION_UP:
                    long pressDuration = System.currentTimeMillis() - longPressStartTime;
                    if (pressDuration > 500) { // Seuil pour un long press (500ms)
                        IGeoPoint geoPoint = mapView.getProjection().fromPixels((int) event.getX(), (int) event.getY());
                        GeoPoint point = new GeoPoint(geoPoint.getLatitude(), geoPoint.getLongitude());
                        showAddMarkerDialog(point);
                    }
                    break;
            }
            return false;
        });


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
    public void calculateRoute(MapView mapView) {
        ArrayList<GeoPoint> waypoints = new ArrayList<>();
        waypoints.add(new GeoPoint(48.858844, 2.294351)); // Tour Eiffel
        waypoints.add(new GeoPoint(48.856613, 2.352222)); // Notre-Dame

        OSRMRoadManager roadManager = new OSRMRoadManager(this, "welo/0 (Linux; Android 12; Pixel 5)");
        roadManager.setMean(OSRMRoadManager.MEAN_BY_BIKE); // Mode velo

        Road road = roadManager.getRoad(waypoints);

        if (road == null || road.mStatus != Road.STATUS_OK) {
            Log.e("RoutingError", "Failed to calculate the road.");
            return;
        }

        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        mapView.getOverlays().add(roadOverlay);
        @SuppressLint("UseCompatLoadingForDrawables") Drawable nodeIcon = getResources().getDrawable(R.drawable.marker_node);
        for (int i=0; i<road.mNodes.size(); i++){
            RoadNode node = road.mNodes.get(i);
            Marker nodeMarker = new Marker(mapView);
            nodeMarker.setPosition(node.mLocation);
            nodeMarker.setIcon(nodeIcon);
            nodeMarker.setTitle("Step "+i);
            mapView.getOverlays().add(nodeMarker);
        }

        mapView.invalidate(); // Actualise la carte
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
    private void addMarker(GeoPoint point, String title, String description, int iconRes) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        marker.setSnippet(description); // Description de l'événement

        // Définir une icône personnalisée
        @SuppressLint("UseCompatLoadingForDrawables")
        Drawable icon = getResources().getDrawable(iconRes, null);
        marker.setIcon(icon);

        // Ajouter une fenêtre d'information au clic
        marker.setInfoWindow(new CustomInfoWindow(mapView));

        // Ajouter le marqueur à la carte
        mapView.getOverlays().add(marker);
        mapView.invalidate();
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

        popupMenu.setOnMenuItemClickListener(item -> {
            String title;
            String description = "Événement signalé"; // Par défaut
            int iconRes;

            if (item.getItemId() == R.id.Trajet) {
                title = "Trajet";
                iconRes = R.drawable.panneau_2; // Remplace par ton icône
                addMarker(p, title, description, iconRes);
                return true;
            }
            if (item.getItemId() == R.id.Route) {
                title = "Route fermée";
                iconRes = R.drawable.accident; // Remplace par ton icône
                addMarker(p, title, description, iconRes);
                return true;
            }
            if (item.getItemId() == R.id.Travaux) {
                title = "Travaux en cours";
                iconRes = R.drawable.construction; // Remplace par ton icône
                addMarker(p, title, description, iconRes);
                return true;
            }
            if (item.getItemId() == R.id.Reset) {
                resetMap(); // Réinitialiser la carte
                return true;
            }

            return false;
        });


        popupMenu.show();
    }

    }


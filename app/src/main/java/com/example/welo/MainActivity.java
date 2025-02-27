package com.example.welo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import androidx.appcompat.widget.SearchView;

import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.util.BoundingBox;

import android.content.Context;

public class MainActivity extends AppCompatActivity {
    private MapView mapView = null;
    private Marker userMarker; // Pointeur de l'utilisateur
    private Marker searchMarker; // Pointeur pour la recherche

    private static final int REQUEST_PERMISSIONS = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(new File(getCacheDir(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(getCacheDir(), "osmdroid/tiles"));

        // Initialisation de la carte
        mapView = findViewById(R.id.map);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

        // Définir un point initial pour éviter une vue vide
        GeoPoint startPoint = new GeoPoint(48.8566, 2.3522); // Paris par défaut
        mapView.getController().setCenter(startPoint);
        mapView.getController().setZoom(15.0);

        // Vérifie si des marqueurs existent déjà et ajuste le zoom
        adjustZoom();

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

        requestPermissionsIfNecessary(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION
        });

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

        // Centrer la carte sur la position actuelle et appliquer le zoom
        mapView.getController().animateTo(userLocation);
        mapView.getController().setZoom(18.0); // Ajuste le zoom
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

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
        mapView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Permission refusée, localisation désactivée", Toast.LENGTH_LONG).show();
            }
        }
    }
}
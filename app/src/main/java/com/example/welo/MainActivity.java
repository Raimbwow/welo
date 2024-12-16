package com.example.welo;


import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.pm.PackageManager;



import androidx.core.app.ActivityCompat;


import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;

import java.util.ArrayList;


import android.Manifest;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;


import androidx.annotation.NonNull;



import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.File;



import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;


public class MainActivity extends AppCompatActivity implements MapEventsReceiver{

    //private ActivityMainBinding binding;

    private MapView mapView;
    private static final int REQUEST_PERMISSIONS = 1;
    @Override public boolean singleTapConfirmedHelper(GeoPoint p) {
        ArrayList<OverlayItem> items = new ArrayList<>();
        items.add(new OverlayItem("Title", "Description", p));
        Toast.makeText(this, "Tapped", Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "Tap on ("+p.getLatitude()+","+p.getLongitude()+")", Toast.LENGTH_SHORT).show();
        return true;
    }
    @Override public boolean longPressHelper(GeoPoint p) {
        //DO NOTHING FOR NOW:
        return false;
    }

    @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Configuration de OSMDroid
            Configuration.getInstance().setUserAgentValue(getPackageName());
            Configuration.getInstance().setOsmdroidBasePath(new File(getCacheDir(), "osmdroid"));
            Configuration.getInstance().setOsmdroidTileCache(new File(getCacheDir(), "osmdroid/tiles"));

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
            GeoPoint startPoint = new GeoPoint(48.400002, -4.48333); // Coordonnées de Brest
            mapView.getController().setCenter(startPoint);

            // Ajouter un marqueur à la position initiale
            Marker startMarker = new Marker(mapView);
            startMarker.setPosition(startPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            startMarker.setTitle("Vous êtes ici !");
            mapView.getOverlays().add(startMarker);



            // Demander les permissions nécessaires

            // IMPORTANT PROBLEME A REGLER

            // ça peut régler le problème https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Java)

            //*********************************************************//
            /* requestPermissionsIfNecessary(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
            }); */
            //*********************************************************************

            //binding = ActivityMainBinding.inflate(getLayoutInflater());
            //setContentView(binding.getRoot());

            BottomNavigationView navView = findViewById(R.id.nav_view);
            // Passing each menu ID as a set of Ids because each
            // menu should be considered as top level destinations.
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                    .build();
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            //NavigationUI.setupWithNavController(binding.navView, navController);
            //your items
            ArrayList<OverlayItem> items = new ArrayList<>();
            items.add(new OverlayItem("Title", "Description", new GeoPoint(48.400002, -4.48333))); // Lat/Lon decimal degrees
            items.add(new OverlayItem("Title", "Description", new GeoPoint(48.400002, -4.48)));
            // Ajoutez un écouteur de clic sur la carte



        //https://github.com/osmdroid/osmdroid/wiki/How-to-use-the-osmdroid-library-(Java)
        //the overlay
        /*
        ItemizedOverlayWithFocus<OverlayItem> mOverlay = new ItemizedOverlayWithFocus<>(items,
                new ItemizedIconOverlay.OnItemGestureListener<>() {
                    @Override
                    public boolean onItemSingleTapUp(final int index, final OverlayItem item) {
                        //do something
                        return true;
                    }
                    @Override
                    public boolean onItemLongPress(final int index, final OverlayItem item) {
                        return false;
                    }
                }, this);



        MapEventsReceiver mapEventsReceiver = (MapEventsReceiver) null;

        mapView.getOverlays().add(new MapEventsOverlay(mapEventsReceiver){

            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    // Récupérez les coordonnées du clic
                    Projection projection = mapView.getProjection();
                    GeoPoint geoPoint = (GeoPoint) projection.fromPixels((int) event.getX(), (int) event.getY());

                    // Ajouter un nouvel item à la carte
                    items.add(new OverlayItem("Title", "Description", geoPoint));

                    return true; // Clic traité
                };
                return false; // Sinon, passe au traitement standard
            }
        });

        mOverlay.setFocusItemsOnTap(true);
        MotionEvent MotionEvent = null;

        mapView.getOverlays().add(mOverlay);
        */

        }

    // Demander les permissions nécessaires au démarrage
    private void requestPermissionsIfNecessary(String[] permissions) {
        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS);
                return;
            }
        }
    }

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


}
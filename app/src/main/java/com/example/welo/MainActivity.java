package com.example.welo;
import static com.google.android.gms.maps.CameraUpdateFactory.zoomTo;

import com.example.welo.R;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.content.pm.PackageManager;



import androidx.core.app.ActivityCompat;

import org.osmdroid.views.overlay.GroundOverlay;
import org.osmdroid.views.overlay.GroundOverlay4;
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



import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.io.File;


import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;



public class MainActivity extends AppCompatActivity {

    //private ActivityMainBinding binding;



private MapView mapView;
    private static final int REQUEST_PERMISSIONS = 1;

    ArrayList<OverlayItem> items = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
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

        MapEventsOverlay events = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {

                Toast.makeText(getApplicationContext(), "Tap on (" + p.getLatitude() + "," + p.getLongitude() + ")", Toast.LENGTH_SHORT).show();
                items.add(new OverlayItem("Title", "Description", new GeoPoint(48.400002, -4.40)));
                /*Polygon circle = new Polygon();
                circle.setPoints(Polygon.pointsAsCircle(p, 200.0));
                mapView.getOverlays().add(circle);

                circle.setTitle("Centered on " + p.getLatitude() + "," + p.getLongitude());*/


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
                Polygon circle = new Polygon();
                circle.setPoints(Polygon.pointsAsCircle(p, 2000.0));
                mapView.getOverlays().add(circle);
                mapView.invalidate();

                showMenu(p);
                return false;
            }

        });
        mapView.getOverlayManager().add(events);


        // mapView.();

        // Supposons que vous avez une ListView ou une autre vue sur laquelle vous voulez le menu contextuel

        // ATTENTIION MAP N4EST SUREMENT PAS ADAPTE ICI


        registerForContextMenu(mapView);

        };
    // Gérer les événements de carte (tap et long press)

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
    /*protected void onClick(Bundle savedInstanceState){
        final MapEventsReceiver mReceive = new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                Toast.makeText(getApplicationContext(), "Tap on (" + p.getLatitude() + "," + p.getLongitude() + ")", Toast.LENGTH_SHORT).show();
                items.add(new OverlayItem("Title", "Description", new GeoPoint(48.400002, -4.40)));
                Polygon circle = new Polygon();
                circle.setPoints(Polygon.pointsAsCircle(p, 2000.0));
                mapView.getOverlays().add(circle);

                circle.setTitle("Centered on " + p.getLatitude() + "," + p.getLongitude());
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                Toast.makeText(getApplicationContext(), "Long press on (" + p.getLatitude() + "," + p.getLongitude() + ")", Toast.LENGTH_SHORT).show();
                Polygon circle = new Polygon();
                circle.setPoints(Polygon.pointsAsCircle(p, 2000.0));
                return true;
            }

        };
    }*/

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

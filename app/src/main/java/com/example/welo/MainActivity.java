package com.example.welo;

import android.app.AlertDialog;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;


import org.osmdroid.api.IGeoPoint;
import org.osmdroid.events.DelayedMapListener;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;

import org.osmdroid.views.overlay.GroundOverlay;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;


import androidx.appcompat.app.AppCompatActivity;

import androidx.core.content.ContextCompat;



import android.widget.EditText;
import android.content.pm.PackageManager;

import android.Manifest;

import android.content.Context;

import android.graphics.drawable.Drawable;

import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;


import java.util.List;
import java.util.ArrayList;

import androidx.core.app.ActivityCompat;

import org.osmdroid.views.overlay.MapEventsOverlay;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;





import android.view.ContextMenu;

import android.view.MenuInflater;

import android.view.MotionEvent;
import android.view.View;

import android.widget.PopupMenu;
import android.widget.Toast;


import androidx.annotation.NonNull;
import org.osmdroid.views.overlay.Overlay;
import org.osmdroid.views.overlay.Marker;


import org.osmdroid.util.GeoPoint;



import java.io.File;
import org.osmdroid.views.overlay.OverlayItem;

import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;


import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;

import org.osmdroid.views.overlay.Polyline;

public class MainActivity extends AppCompatActivity {

    private MapView mapView = null;

    private static final int REQUEST_PERMISSIONS = 1;

    ArrayList<OverlayItem> items = new ArrayList<>();

    private final int selectedIconIndex = -1; // Index du type d'événement sélectionné
    private long longPressStartTime = 0;

    public static class CustomInfoWindow extends MarkerInfoWindow {
        public CustomInfoWindow(MapView mapView) {
            //super(org.osmdroid.library.R.layout.bonuspack_bubble, mapView);
            super(R.layout.custom_info_window, mapView);
        }
    }
    public Drawable resizeDrawable(Context context, Drawable drawable, int width, int height) {
        if (drawable != null) {
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return new BitmapDrawable(context.getResources(), bitmap);
        } else {
            Log.e("DrawableError", "Drawable est null");
            return null;
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

        // Variable pour stocker l'index du type sélectionné
        final int[] selectedIndex = {-1}; // -1 signifie qu'aucun choix n'a été fait

        builder.setSingleChoiceItems(eventTypes, -1, (dialog, which) -> {
            selectedIndex[0] = which; // Sauvegarde l'index sélectionné
        });

        builder.setPositiveButton("Ajouter", (dialog, which) -> {
            String description = input.getText().toString().trim();

            if (selectedIndex[0] == -1) { // Vérifie si l'utilisateur a sélectionné un type
                Toast.makeText(this, "Veuillez choisir un type d'événement", Toast.LENGTH_SHORT).show();
                return;
            }

            if (description.isEmpty()) { // Vérifie si la description est vide
                Toast.makeText(this, "Veuillez entrer une description", Toast.LENGTH_SHORT).show();
                return;
            }

            // Récupérer les valeurs sélectionnées
            String selectedTitle = eventTypes[selectedIndex[0]];
            int selectedIconRes = eventIcons[selectedIndex[0]];

            // Ajouter le marqueur
            addMarker(point, selectedTitle, description, selectedIconRes);
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
                return false;
            }

        });

        mapView.getOverlayManager().add(events);



        // Supposons que vous avez une ListView ou une autre vue sur laquelle vous voulez le menu contextuel


        registerForContextMenu(mapView);


        /*Quand tu restes appuyé plus de 500 ms, ça déclenche le long press.
Ça récupère les coordonnées du point appuyé.
Puis ça appelle showAddMarkerDialog(point) qui ouvre le dialogue d'ajout d'événement avec choix du type d'événement et description.*/

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
        });mapView.addMapListener(new DelayedMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                return false; // Pas besoin de réagir au scroll
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                //Adapte l'icone à la taille de la carte (et pas du zoom associé)
                //updateMarkerIcons();
                return true;
            }
        }, 100)); // Délai pour éviter trop d'appels en rafale

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
        icon = resizeDrawable(this, icon, 80, 80); // Par exemple, taille constante 50x50
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

    }




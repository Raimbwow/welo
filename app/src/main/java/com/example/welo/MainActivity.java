package com.example.welo;

import android.os.Bundle;

import com.google.android.gms.maps.model.Tile;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;


import com.example.welo.R;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import org.mapsforge.map.android.util.AndroidUtil;
import org.mapsforge.map.layer.renderer.TileRendererLayer;
import org.osmdroid.config.Configuration;
import org.osmdroid.shape.ShapeConverter;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.io.File;
import java.util.List;

import com.example.welo.databinding.ActivityMainBinding;

import org.mapsforge.map.reader.MapFile;
import org.mapsforge.map.layer.cache.TileCache;
//import org.mapsforge.map.layer.TileRendererLayer;
import org.mapsforge.map.android.graphics.AndroidGraphicFactory;
import org.osmdroid.views.MapView;


public class MainActivity extends AppCompatActivity {

    //private ActivityMainBinding binding;

    private MapView mapView;
    private static final int REQUEST_PERMISSIONS = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configuration de OSMDroid
        Configuration.getInstance().setUserAgentValue(getPackageName());
        Configuration.getInstance().setOsmdroidBasePath(new File(getCacheDir(), "osmdroid"));
        Configuration.getInstance().setOsmdroidTileCache(new File(getCacheDir(), "osmdroid/tiles"));

        setContentView(R.layout.activity_main);
        // Initialiser Mapsforge
        AndroidGraphicFactory.createInstance(this.getApplication());


        // Initialisation de la MapView
        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.OpenTopo);
        mapView.setBuiltInZoomControls(true);
        mapView.setMultiTouchControls(true);

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


        // Charger le fichier .map de Mapsforge
        File mapFile = new File(getExternalFilesDir(null), "your-map-file.map");
        if (!mapFile.exists()) {
            throw new RuntimeException("Le fichier .map est introuvable");
        }
        MapFile mapDataStore = new MapFile(mapFile);

        // Créer un cache de tuiles pour Mapsforge
        TileCache tileCache = AndroidUtil.createTileCache(
                this,
                "mapsforgeCache",
                mapView.getTileRequestCompleteHandler(),
                mapView.getModel().displayModel.getTileSize(),
                1f
        );


        List<Overlay> folder = ShapeConverter.convert(mMapView, new File(myshape));
        mMapView.getOverlayManager().addAll(folder);
        mMapView.invalidate();
        // Demander les permissions nécessaires

        // IMPORTANT PROBLEME A REGLER
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

        // Ajouter la couche Mapsforge à OSMDroid
        mapView.getOverlayManager().add(tileRendererLayer);
        mapView.setZoomLevel(15);

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
package com.example.welo.ui.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.welo.R;
import com.example.welo.ui.modules.MapRenderer;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.overlay.Marker;

/**
 * Service that listens for location updates and updates the map accordingly.
 */
public class Localisation extends Service {
    private static final int REQUEST_LOCATION = 1;
    private final IBinder binder = new LocalBinder();
    private LocationManager locationManager;
    private LocationListener locationListener;
    private MapRenderer mapRenderer;
    private Marker currentMarker;
    private boolean liveTracking = false;
    private int updateIntervalInSeconds = 10; // Default to 10 seconds, can be changed via Intent

    public class LocalBinder extends Binder {
        public Localisation getService() {
            return Localisation.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateIntervalInSeconds = intent.getIntExtra("updateInterval", 10);
        liveTracking = intent.getBooleanExtra("LIVE_TRACKING", liveTracking);
        mapRenderer = MapRenderer.getInstance();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Consider calling ActivityCompat#requestPermissions here to request the missing permissions
            // Permissions would typically be requested beforehand in an Activity context
            Log.e("LocalisationService", "Location permissions not granted.");
            return; // Stop further execution if no permission
        }

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                Toast.makeText(getApplicationContext(), "Long press on (" + location.getLatitude() + "," + location.getLongitude() + ")", Toast.LENGTH_SHORT).show();
                GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                if (currentMarker == null) {
                    Drawable icon = getResources().getDrawable(R.drawable.icon_people_marker);
                    currentMarker = mapRenderer.addMarker(point, "Votre position", icon, 0.5f, 1.0f);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}
            public void onProviderEnabled(String provider) {}
            public void onProviderDisabled(String provider) {}
        };

        requestLocationUpdates();
    }

    public void requestLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 * updateIntervalInSeconds, 0, locationListener, Looper.getMainLooper());
            } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000 * updateIntervalInSeconds, 0, locationListener, Looper.getMainLooper());
            } else {
                Log.e("LocalisationService", "No suitable provider found.");
            }
        } else {
            Log.e("LocalisationService", "Location permissions not granted.");
        }
    }

    public void setLiveTracking(boolean liveTracking) {
        this.liveTracking = liveTracking;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }
}

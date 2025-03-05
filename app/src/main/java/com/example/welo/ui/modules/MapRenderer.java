package com.example.welo.ui.modules;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.example.welo.R;
//import com.oniverse.fitmap.TrackActivity;
//import com.oniverse.fitmap.modules.gpxparser.Gpx;
//import com.oniverse.fitmap.modules.gpxparser.TrackPoint;
//import com.oniverse.fitmap.modules.tracks.Track;
//import com.oniverse.fitmap.modules.tracks.TrackList;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.gridlines.LatLonGridlineOverlay2;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
/**
 * Class to render a map
 * Contains methods to add markers, polylines, and overlays
 * @see MapView
 */
public class MapRenderer {
    private static MapRenderer mapRenderer = null;
    private final MapView map;
    private final Context context;

    private MyLocationNewOverlay mLocationOverlay;
    private CompassOverlay mCompassOverlay;
    private RotationGestureOverlay mRotationGestureOverlay;
    private ScaleBarOverlay mScaleBarOverlay;

    private ArrayList<Marker> markers = new ArrayList<>();
    private ArrayList<Polyline> polylines = new ArrayList<>();
    private Polyline polylineLive;

    public MapRenderer(MapView map) {
        this(map, TileSourceFactory.MAPNIK);
    }

    public MapRenderer(MapView map, ITileSource tileSource) {
        this.map = map;
        this.map.setTileSource(tileSource);

        this.map.setBuiltInZoomControls(true);
        this.map.setMultiTouchControls(true);

        this.context = map.getContext();

        mapRenderer = this;
    }

    public void setZoom(float zoom) {
        IMapController mapController = map.getController();
        mapController.setZoom(zoom);
    }

    public void setCenter(GeoPoint center) {
        IMapController mapController = map.getController();
        mapController.setCenter(center);
    }

    public void setZoom(float zoom, GeoPoint center) {
        IMapController mapController = map.getController();
        mapController.setZoom(zoom);
        mapController.setCenter(center);
    }
    /**
     * Add the MyLocationOverlay to the map
     * The MyLocationOverlay shows the user's location on the map
     */
    public void addMyLocationOverlay() {
        // TODO: Use another method because this one seems to don't work
        if (this.mLocationOverlay == null) {
            mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
            mLocationOverlay.enableMyLocation();
        }
        if (!map.getOverlays().contains(mLocationOverlay)) {
            map.getOverlays().add(mLocationOverlay);
            map.invalidate();
        }
    }

    public void removeMyLocationOverlay() {
        map.getOverlays().remove(mLocationOverlay);
    }

    public void addCompassOverlay() {
        mCompassOverlay = new CompassOverlay(context, new InternalCompassOrientationProvider(context), map);
        mCompassOverlay.enableCompass();
        map.getOverlays().add(mCompassOverlay);
    }

    public void removeCompassOverlay() {
        map.getOverlays().remove(mCompassOverlay);
    }

    public void addGridlinesOverlay() {
        LatLonGridlineOverlay2 overlay = new LatLonGridlineOverlay2();
        map.getOverlays().add(overlay);
    }

    public void addRotationGestureOverlay() {
        mRotationGestureOverlay = new RotationGestureOverlay(context, map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(mRotationGestureOverlay);
    }

    public void removeRotationGestureOverlay() {
        map.getOverlays().remove(mRotationGestureOverlay);
    }

    public void addMapScaleBarOverlay() {
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        mScaleBarOverlay = new ScaleBarOverlay(map);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        map.getOverlays().add(mScaleBarOverlay);
    }

    public void removeMapScaleBarOverlay() {
        map.getOverlays().remove(mScaleBarOverlay);
    }

    public void addAllOverlays() {
        addMyLocationOverlay();
        addCompassOverlay();
        addGridlinesOverlay();
        addRotationGestureOverlay();
        addMapScaleBarOverlay();
    }

    public void removeAllOverlays() {
        removeMyLocationOverlay();
        removeCompassOverlay();
        removeRotationGestureOverlay();
        removeMapScaleBarOverlay();
    }

    public Marker addMarker(GeoPoint point, String title, Drawable icon) {
        return addMarker(point, title, icon, Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER);
    }

    public Marker addMarker(GeoPoint point, String title, Drawable icon,
            float anchorU, float anchorV) {
        Marker marker = new Marker(map);
        markers.add(marker);

        marker.setPosition(point);
        marker.setAnchor(anchorU, anchorV);
        map.getOverlays().add(marker);

        marker.setIcon(icon);
        marker.setTitle(title);
        return marker;
    }

    public void removeMarker(Marker marker) {
        map.getOverlays().remove(marker);
        markers.remove(marker);
    }

    public Polyline addPolyline(ArrayList<GeoPoint> geoPoints) {
        Polyline line = new Polyline();
        polylines.add(line);
        line.setPoints(geoPoints);
        line.setOnClickListener(null);
        map.getOverlayManager().add(line);
        return line;
    }

    public void removePolyline(Polyline line) {
        map.getOverlayManager().remove(line);
        polylines.remove(line);
    }

    public void clear() {
        CopyOnWriteArrayList<Marker> markers = new CopyOnWriteArrayList<>();
        CopyOnWriteArrayList<Polyline> polylines = new CopyOnWriteArrayList<>();
        markers.addAll(this.markers);
        polylines.addAll(this.polylines);

        for (Marker marker : markers) {
            removeMarker(marker);
        }
        for (Polyline line : polylines) {
            removePolyline(line);
        }
    }

    // TODO: Add other makers and lines ( https://github.com/osmdroid/osmdroid/wiki/Markers,-Lines-and-Polygons-(Java)#marker-using-a-text-label-instead-of-an-icon )

    public void onResume() {
        map.onResume();
    }

    public void onPause() {
        map.onPause();
    }

    public void setTileSource(ITileSource tileSource) {
        map.setTileSource(tileSource);
    }

    /*public void drawPoint(TrackPoint trackPoint, String title) {
        GeoPoint startPoint = new GeoPoint(trackPoint.getLatitude(), trackPoint.getLongitude());
        this.addMarker(startPoint,  title, context.getDrawable(R.drawable.icon_location),
                Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
    }

    public void drawPointWithGpxLoader(TrackPoint trackPoint, String title,
                                       Context context, Intent locationIntent) {
        GeoPoint startPoint = new GeoPoint(trackPoint.getLatitude(), trackPoint.getLongitude());
        Marker marker = this.addMarker(startPoint,  title, context.getDrawable(R.drawable.icon_location),
                Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);

        marker.setOnMarkerClickListener((marker1, mapView) -> {
            TrackList trackList = TrackList.getInstance();
            Track track = trackList.getTrack(title);

            // Stop the service
            context.stopService(locationIntent);

            // Clear the memory
            System.gc();
            Runtime.getRuntime().gc();

            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            Intent intent = new Intent(context, TrackActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("track", track);
            intent.putExtras(bundle);
            context.startActivity(intent);

            return true;
        });
    }

    public void setGpx(Gpx gpx) {
        this.clear();

        ArrayList<GeoPoint> geoPoints = new ArrayList<>();
        for (TrackPoint trackPoint : gpx.getTrack().get(0).getFirstSegment().getTrackPoints()) {
            geoPoints.add(new GeoPoint(trackPoint.getLatitude(), trackPoint.getLongitude()));
        }
        TrackPoint startPoint = gpx.getTrack().get(0).getFirstSegment().getFirstTrackPoint();
        TrackPoint endPoint = gpx.getTrack().get(0).getLastSegment().getLastTrackPoint();
        this.drawPoint(startPoint, "Start");
        this.drawPoint(endPoint, "End");
        this.addPolyline(geoPoints);

        // Zoom and center the track
        this.setZoom(15);
        this.setCenter(geoPoints.get(0));
    }

    public void updateMarkerPosition(Marker currentMarker, GeoPoint point) {
        currentMarker.setPosition(point);
    }

    public void addPolylineLive(GeoPoint geoPoint) {
        if (polylineLive == null) {
            polylineLive = new Polyline();
            polylineLive.setColor(context.getColor(R.color.colorPolylineLive));
            polylineLive.setWidth(5);
            polylineLive.setGeodesic(true);
            map.getOverlayManager().add(polylineLive);
        }
        polylineLive.addPoint(geoPoint);
    }*/

    public void clearPolylineLive() {
        if (polylineLive != null) {
            polylineLive.setPoints(new ArrayList<>());
        }
    }

    public static MapRenderer getInstance() {
        return mapRenderer;
    }
}

    package com.example.virtualplaque;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.os.PersistableBundle;

import com.mapbox.android.core.location.LocationEngine;
//import com.mapbox.android.core.location.LocationEngineListener;
//import com.mapbox.android.core.location.LocationEnginePriority;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.GeoJson;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerOptions;
import com.mapbox.mapboxsdk.plugins.locationlayer.LocationLayerPlugin;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;

    public class Map extends AppCompatActivity implements OnMapReadyCallback,
        MapboxMap.OnMapClickListener {

    MapView mapView;
    private MapboxMap mapboxMap;
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationComponent locationComponent;
    /*private LocationLayerPlugin locationLayerPlugin = new LocationLayerPlugin(mapView,mapboxMap);
    private Location originLocation;
    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //MapBox access to page view
        Mapbox.getInstance(this,getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map);

        mapView = (MapView) findViewById(R.id.mapV);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {

        Map.this.mapboxMap = mapboxMap;

        MarkerViewManager markerViewManager = new MarkerViewManager(mapView,mapboxMap);


        mapboxMap.setStyle(Style.MAPBOX_STREETS, style -> {

                CameraPosition pos = new CameraPosition.Builder()
                        .target(new LatLng(37.7749,-122.4194))
                        .zoom(16)
                        .bearing(180)
                        .tilt(30)
                        .build();

                mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(pos));
                enableLocationComponent(style);

                addDestinationIconSymbolLayer(style);
                mapboxMap.addOnMapClickListener(this);
        });
    }

    private void addDestinationIconSymbolLayer(Style loadedMapStyle) {
        //-----Providing the Marker Image-----
        loadedMapStyle.addImage("destinationMarkerID", BitmapFactory.decodeResource(this.getResources(),
                R.drawable.markersym));
        GeoJsonSource geoJsonSource = new GeoJsonSource("destinationSourceID");
        loadedMapStyle.addSource(geoJsonSource);

        SymbolLayer destinationSymbolLayer = new SymbolLayer("destinationSymbolLayerID","destinationSourceID");
        destinationSymbolLayer.withProperties(iconImage("destinationMarkerID"),
                iconAllowOverlap(true),
                iconIgnorePlacement(true));

        loadedMapStyle.addLayer(destinationSymbolLayer);
    }


    private void enableLocationComponent(Style style) {
    }

    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        //---- Convert LngLat object to Point object
        Point destinationPoint = Point.fromLngLat(point.getLongitude(),point.getLatitude());
        //---- Get Current LngLat coordinates and convert them to Point obj
        Point getOriginPoint = Point.fromLngLat(locationComponent.getLastKnownLocation().getLongitude(),
                locationComponent.getLastKnownLocation().getLatitude());
        //---- Pass to GeoJSON as Source and pass using the Source ID String
        GeoJsonSource source = mapboxMap.getStyle().getSourceAs("destinationSourceID");
        if(source!=null){
            source.setGeoJson(Feature.fromGeometry(destinationPoint));
        }
        return true;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

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
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }


    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
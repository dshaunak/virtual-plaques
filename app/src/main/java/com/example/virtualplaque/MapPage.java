package com.example.virtualplaque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.Iterables;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.Document;
import com.google.gson.JsonObject;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import android.graphics.BitmapFactory;
import android.view.View;
import android.widget.Toast;

import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

//import com.mapbox.virtualplaque.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

import static com.mapbox.mapboxsdk.style.expressions.Expression.stop;
import static com.mapbox.mapboxsdk.style.expressions.Expression.zoom;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconIgnorePlacement;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconSize;


public class MapPage extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener{

    private static final int REQUEST_CODE_AUTOCOMPLETE = 7171;
    public static final String SOURCE_ID = "SOURCE_ID";
    public static final String ICON_ID = "ICON_ID";
    public static final String LAYER_ID = "LAYER_ID";


    public MapView mapView;
    public MapboxMap mapboxMap;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;
    public List<Feature> markerList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        setContentView(R.layout.activity_map_page);
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fAuth=FirebaseAuth.getInstance();
        fStore=FirebaseFirestore.getInstance();
        fUser=fAuth.getCurrentUser();

        markerList= new ArrayList<>();

        String eMail = fUser.getEmail();
        CollectionReference colRef = (CollectionReference) fStore.collection("users").document(eMail)
                .collection("plaques");

        colRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                 int count = value.size();
                        Toast.makeText(MapPage.this, "Firebase Doc Snapshot received." + count, Toast.LENGTH_SHORT).show();
                        int i =0;

                        List<DocumentSnapshot> docSnap = value.getDocuments();
                        ListIterator itr =docSnap.listIterator(0);

                        while(i<count){

                            DocumentSnapshot dS = docSnap.get(i);
                            markerList.add(Feature.fromGeometry(
                                    Point.fromLngLat(Double.parseDouble(dS.getString("loCord")), Double.parseDouble(dS.getString("laCord")))));
                            i++;
                        }



                    }
                });
        //int count = value.size();
        //Toast.makeText(MapPage.this, "Firebase Doc Snapshot received." + count, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {

        this.mapboxMap = mapboxMap;

        List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(-57.225365, -33.213144)));
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(-54.14164, -33.981818)));
        symbolLayerIconFeatureList.add(Feature.fromGeometry(
                Point.fromLngLat(-56.990533, -30.583266)));

        mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {


                /*------------Default Hardcoded Marker Co-ordinates
                style.addSource(new GeoJsonSource(SOURCE_ID,
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)));   */

                style.addSource(new GeoJsonSource(SOURCE_ID,
                        FeatureCollection.fromFeatures(markerList)));

                // Add the marker image to map
                style.addImage(ICON_ID, BitmapFactory.decodeResource(
                        MapPage.this.getResources(), R.drawable.markersym));

                // Adding an offset so that the bottom of the blue icon gets fixed to the coordinate, rather than the
                // middle of the icon being fixed to the coordinate point.
                style.addLayer(new SymbolLayer(LAYER_ID, SOURCE_ID)
                        .withProperties(PropertyFactory.iconImage(ICON_ID),
                                iconAllowOverlap(true),
                                iconOffset(new Float[]{0f, -9f}),
                                iconSize(interpolate(exponential(1f),zoom(),stop(12,0.25f)))
                ));
                mapboxMap.addOnMapClickListener(MapPage.this);
            }
        });
    }



    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
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

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}

/*
Previously used setStyle() method:

 mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/mapbox/cjf4m44iw0uza2spb3q0a7s41")
                .withImage(ICON_ID,BitmapFactory.decodeResource(
                        MapPage.this.getResources(),R.drawable.markersym))
                .withSource(new GeoJsonSource(SOURCE_ID,
                        FeatureCollection.fromFeatures(symbolLayerIconFeatureList)))
                    .withLayer(new SymbolLayer(LAYER_ID,SOURCE_ID)
                        .withProperties(
                                iconImage(ICON_ID),
                                iconAllowOverlap(true),
                                iconIgnorePlacement(true),
                                iconSize(interpolate(exponential(1f),zoom(),stop(12,0.1f)))
                        )
                    )
        );

*

 XML---------




        <com.google.android.material.floatingactionbutton.FloatingActionButton
            android:id="@+id/floatingActionBt"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_gravity="end|bottom"
            android:layout_margin="16dp"
            android:clickable="true"
            android:tint="@android:color/white"
            mapbox:backgroundTint="@color/colorPrimary"
            app:srcCompat="?android:attr/actionModeWebSearchDrawable" />







 */
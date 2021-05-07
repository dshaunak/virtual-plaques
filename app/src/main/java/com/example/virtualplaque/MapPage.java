package com.example.virtualplaque;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PointF;
import android.os.Bundle;
import android.graphics.BitmapFactory;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import android.util.Log;
import android.widget.Toast;

import static com.mapbox.mapboxsdk.style.expressions.Expression.exponential;
import static com.mapbox.mapboxsdk.style.expressions.Expression.interpolate;

import java.util.ArrayList;
import java.util.Arrays;
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
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.literal;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAnchor;


public class MapPage extends AppCompatActivity implements OnMapReadyCallback, MapboxMap.OnMapClickListener{

    private static final int REQUEST_CODE_AUTOCOMPLETE = 7171;
    public static final String SOURCE_ID = "SOURCE_ID";
    public static final String ICON_ID = "ICON_ID";
    public static final String LAYER_ID = "LAYER_ID";
    private static final String CALLOUT_LAYER_ID = "CALLOUT_LAYER_ID";
    private static final String PROPERTY_SELECTED = "selected";
    private static final String PLAQUE_NAME = "title";
    private static final String PLAQUE_DESCRIPTION = "description";


    public MapView mapView;
    public MapboxMap mapboxMap;
    FirebaseAuth fAuth;
    FirebaseUser fUser;
    FirebaseFirestore fStore;
    public List<Feature> markerList;
    public List<Point> pointList;
    private GeoJsonSource source;
    private FeatureCollection featureCollection;
    public SymbolManager symbolManager;
    public DocumentSnapshot dS;
    public double[] arr = new double[400];
    public int count =0;
    public static final String TAG = "Points";

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
        pointList= new ArrayList<>();

        //-------------------Getting saved User Plaque data from Google FireStore and adding it to the "markerList" Feature List to display.

        String eMail = fUser.getEmail();
        CollectionReference colRef = (CollectionReference) fStore.collection("users").document(eMail)
                .collection("plaques");

        colRef.addSnapshotListener(this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                                 count = value.size();
                        Toast.makeText(MapPage.this, "Firebase Doc Snapshot received." + count, Toast.LENGTH_SHORT).show();
                        int i =0;
                        int j=0;
                        List<DocumentSnapshot> docSnap = value.getDocuments();
                        ListIterator itr =docSnap.listIterator(0);

                        while(i<count){

                            dS = docSnap.get(i);
                            markerList.add(Feature.fromGeometry(
                                    Point.fromLngLat(Double.parseDouble(dS.getString("loCord")), Double.parseDouble(dS.getString("laCord")))));
                                    //arr[j++]=Double.parseDouble(dS.getString("loCord"));
                                    //arr[j++]=Double.parseDouble(dS.getString("laCord"));
                            pointList.add(Point.fromLngLat(Double.parseDouble(dS.getString("loCord")), Double.parseDouble(dS.getString("laCord"))));
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

                // ------ WORKING CODE COMMENTED TO TEST SYMBOL MANAGER
                /*
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
                */
                //------------------------------------------------------------------------------------------

                style.addImage(ICON_ID, BitmapFactory.decodeResource(
                        MapPage.this.getResources(), R.drawable.markersym));

                symbolManager = new SymbolManager(mapView,mapboxMap,style);
                symbolManager.setIconAllowOverlap(true);
                symbolManager.setTextAllowOverlap(true);

                //symbolManager.create(new SymbolOptions().withData(new GeoJsonSource(SOURCE_ID, FeatureCollection.fromFeatures(markerList))));

                int arri=0;

                //arr[arri],arr[arri+1]
                //while(arri+1<arr.length)
                ListIterator iterator = pointList.listIterator();
                while(iterator.hasNext()){
                    /*Point p = Point.fromLngLat(arr[arri],arr[arri+1]);
                    symbolManager.create(new SymbolOptions().withGeometry(p)
                            .withIconImage(ICON_ID).withIconSize(0.2f));
                    arri+=2;*/

                    symbolManager.create(new SymbolOptions().withGeometry((Point) iterator.next())
                            .withIconImage(ICON_ID).withIconSize(0.2f));
                    //Toast.makeText(MapPage.this, "Loop Counts"+arri, Toast.LENGTH_SHORT).show();
                    arri++;
                }
                //Toast.makeText(MapPage.this, "Points: "+pointList, Toast.LENGTH_SHORT).show();
                Log.d(TAG,"onFailure: "+ pointList);


                symbolManager.addClickListener(new OnSymbolClickListener() {
                    @Override
                    public void onAnnotationClick(Symbol symbol) {
                        Toast.makeText(MapPage.this, "Symbol Clicked", Toast.LENGTH_SHORT).show();
                    }
                });

                //----------
                //--FALSE: ------------WORKING ONCLICK ----- UNCOMMENT IF MAP BREAKS

                //-----Adding Plaque Title and Descriptions using InfoWindow SymbolLayer
                //new LoadGeoJsonDataTask(MapPage.this).execute();
                mapboxMap.addOnMapClickListener(MapPage.this);
            }
        });
    }


    @Override
    public boolean onMapClick(@NonNull LatLng point) {
        PointF pointf = mapboxMap.getProjection().toScreenLocation(point);



        Toast.makeText(MapPage.this, "Layer Clicked" + point, Toast.LENGTH_SHORT).show();
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


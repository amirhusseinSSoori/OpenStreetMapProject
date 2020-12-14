package com.toutorial.openstreetmapsample;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.osmdroid.api.IGeoPoint;
import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.MinimapOverlay;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.simplefastpoint.LabelledGeoPoint;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlay;
import org.osmdroid.views.overlay.simplefastpoint.SimpleFastPointOverlayOptions;
import org.osmdroid.views.overlay.simplefastpoint.SimplePointTheme;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LocationListener {
    MapView mapView;
    IMapController controller;
    FloatingActionButton actionButton;
    LocationManager locationManager;
    Marker marker;
    Button details;
    static final DecimalFormat df = new DecimalFormat("#.000000");
    int requstpermission = 101;
    int requst = 1001;
    int check = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mapView = (MapView) findViewById(R.id.mapview);
        actionButton = (FloatingActionButton) findViewById(R.id.flot_action_botton);
        details = (Button) findViewById(R.id.btnshowPostion);
        //set quality map
        mapView.setTilesScaledToDpi(true);
        mapView.isTilesScaledToDpi();


        setFastOverlay();
        setpermission_file();
        details.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), marker.getPosition().toString(), Toast.LENGTH_SHORT).show();
            marker.getPosition();

        });



    }

    void setpermission_file() {
        if (Build.VERSION.SDK_INT >= 23) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, requst);
            } else {
                setUpMap();
            }
        } else {
            setUpMap();
        }
    }

    public void setUpMap() {
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);
        check = 1;
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        TileSourceFactory.addTileSource(TileSourceFactory.MAPNIK);
        controller = mapView.getController();
        controller.setZoom(16);
        GeoPoint startPoint = new GeoPoint(35.69461516288605, 51.39276614753953);
        controller.setCenter(startPoint);


        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);


        rotateMap();
        AddMarker(startPoint);

        //add CompassOverlay
        CompassOverlay mCompassOverlay = new CompassOverlay(this, new InternalCompassOrientationProvider(this), mapView);
        mCompassOverlay.enableCompass();
        mapView.getOverlays().add(mCompassOverlay);


        actionButton.setOnClickListener(v -> {
            if (getGpsProvierCheck()) {
                getPremissionCheck();
                Toast.makeText(this,"gps  enabled",Toast.LENGTH_SHORT).show();
            } else {
                Dialog_message dialog_message = new Dialog_message(MainActivity.this);
                dialog_message.show();
            }

        });
        mapView.addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                changeMarker();
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        });
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

    }

    void changeMarker() {
        IGeoPoint geoPoint = mapView.getMapCenter();
        double lat = Double.parseDouble(df.format(geoPoint.getLatitude()));
        double lon = Double.parseDouble(df.format(geoPoint.getLongitude()));
        GeoPoint geoPoint1 = new GeoPoint(lat, lon);
        marker.setPosition(geoPoint1);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
    }

    boolean getGpsProvierCheck() {

        Boolean check = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (check)
            return true;
        return false;
    }

    void getPremissionCheck() {
        if (Build.VERSION.SDK_INT >= 23) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, requstpermission);
            } else {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);

            }

        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    public void AddMarker(GeoPoint point) {
        marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
        marker.setIcon(getResources().getDrawable(R.drawable.ic_placeholder));


    }

    void GetLocationUser() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (check != 0)
            mapView.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause() {
        super.onPause();
        if (check != 0)
            mapView.onPause();  //needed for compass, my location overlays, v6.0.0 and up
    }

    public void rotateMap() {
        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(this, mapView);
        mRotationGestureOverlay.setEnabled(true);
        mapView.setMultiTouchControls(true);
        mapView.getOverlays().add(mRotationGestureOverlay);
    }


    public void scaleBar() {
        final Context context = getApplicationContext();
        final DisplayMetrics dm = context.getResources().getDisplayMetrics();
        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(mapView);
        mScaleBarOverlay.setCentred(true);

        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        mapView.getOverlays().add(mScaleBarOverlay);
    }

    public void miniMap() {
        MinimapOverlay mMinimapOverlay = new MinimapOverlay(getApplicationContext(), mapView.getTileRequestCompleteHandler());
        mMinimapOverlay.setWidth(200);
        mMinimapOverlay.setHeight(200);

        mapView.getOverlays().add(mMinimapOverlay);
    }


    @Override
    public void onLocationChanged(@NonNull Location location) {
//        if (location != null) {
//            GeoPoint geoPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
//            controller.setCenter(geoPoint);
//            marker.setPosition(geoPoint);
//            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
//            mapView.getOverlays().add(marker);
//
//        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {

    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == requstpermission) {
            if (grantResults[0] == 0) {
                GetLocationUser();
            }
        } else {
            if (requestCode == requst) {
                if (grantResults[0] == 0) {
                    setUpMap();
                }
            }
        }
    }

    public void setFastOverlay() {
        List<IGeoPoint> points = new ArrayList<>();
        for (int i = 0; i < 10000; i++) {
            points.add(new LabelledGeoPoint(37 + Math.random() * 5, -8 + Math.random() * 5
                    , "Point #" + i));
        }

// wrap them in a theme
        SimplePointTheme pt = new SimplePointTheme(points, true);

// create label style
        Paint textStyle = new Paint();
        textStyle.setStyle(Paint.Style.FILL);
        textStyle.setColor(Color.parseColor("#0000ff"));
        textStyle.setTextAlign(Paint.Align.CENTER);
        textStyle.setTextSize(24);

              // set some visual options for the overlay
              // we use here MAXIMUM_OPTIMIZATION algorithm, which works well with >100k points
        SimpleFastPointOverlayOptions opt = SimpleFastPointOverlayOptions.getDefaultStyle()
                .setAlgorithm(SimpleFastPointOverlayOptions.RenderingAlgorithm.MAXIMUM_OPTIMIZATION)
                .setRadius(7).setIsClickable(true).setCellSize(15).setTextStyle(textStyle);

                  // create the overlay with the theme
        final SimpleFastPointOverlay sfpo = new SimpleFastPointOverlay(pt, opt);

                 // onClick callback
        sfpo.setOnClickListener(new SimpleFastPointOverlay.OnClickListener() {
            @Override
            public void onClick(SimpleFastPointOverlay.PointAdapter points, Integer point) {
                Toast.makeText(mapView.getContext()
                        , "You clicked " + ((LabelledGeoPoint) points.get(point)).getLabel()
                        , Toast.LENGTH_SHORT).show();
            }
        });

          // add overlay
        mapView.getOverlays().add(sfpo);

    }


}
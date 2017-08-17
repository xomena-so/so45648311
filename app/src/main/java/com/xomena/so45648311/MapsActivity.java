package com.xomena.so45648311;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Point;
import android.graphics.Shader;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.maps.android.SphericalUtil;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnCameraIdleListener {

    private GoogleMap mMap;
    private RichLayer richLayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setZoomControlsEnabled(true);

        LatLng ny1 = new LatLng(40.711322,-74.007844);
        LatLng ny2 = new LatLng(40.782493,-73.965424);

        LatLng ny3 = new LatLng(40.75675,-73.98571);

        mMap.setOnCameraIdleListener(this);

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ny3, 12f));

        richLayer = new RichLayer.Builder(findViewById(R.id.map), mMap).zIndex(100).build();

        this.showPolylineAndShade(ny1, ny2);
    }

    @Override
    public void onCameraIdle() {
        // Refresh the RichLayer each time the camera changes
        richLayer.refresh();
    }

    private void showPolylineAndShade(LatLng p1, LatLng p2) {
        this.showCurvedLine(p1, p2, 0.1, Color.argb(30, 220,220,220), Color.argb(30, 192,192,192), Color.argb(30, 105,105,105),15);
        this.showCurvedLine(p1, p2, 0.3, Color.argb(255, 0,191,255), Color.argb(255, 26,188,156), Color.argb(255, 40,123,177),6);
    }

    private void showCurvedLine (LatLng p1, LatLng p2, double k, int basecolor, int color1, int color2,  int w) {
        //Calculate distance and heading between two points
        double d = SphericalUtil.computeDistanceBetween(p1,p2);
        double h = SphericalUtil.computeHeading(p1, p2);

        //Midpoint position
        LatLng p = SphericalUtil.computeOffset(p1, d*0.5, h);

        //Apply some mathematics to calculate position of the circle center
        double x = (1-k*k)*d*0.5/(2*k);
        double r = (1+k*k)*d*0.5/(2*k);

        LatLng c = SphericalUtil.computeOffset(p, x, h + 90.0);

        //Calculate heading between circle center and two points
        double h1 = SphericalUtil.computeHeading(c, p1);
        double h2 = SphericalUtil.computeHeading(c, p2);

        //Calculate positions of points on circle border and add them to polyline options
        int numpoints = 1000;
        double step = (h2 -h1) / numpoints;

        RichPolylineOptions polylineOpts = new RichPolylineOptions(null)
                .zIndex(100) // zIndex represents the position of the polyline on the RichLayer
                .strokeWidth(w)
                .strokeColor(basecolor)
                .linearGradient(false);

        Point fromScreenPoint = null;
        Point toScreenPoint = null;
        int[] colors = new int[]{color1, color2};
        Projection projection = mMap.getProjection();

        for (int i=0; i < numpoints; i++) {
            LatLng pi = SphericalUtil.computeOffset(c, r, h1 + i * step);
            RichPoint rp = new RichPoint(pi);
            polylineOpts.add(rp);
            if (i == 0) {
                fromScreenPoint = projection.toScreenLocation(rp.getPosition());
            }
            if (i == numpoints -1) {
                toScreenPoint = projection.toScreenLocation(rp.getPosition());
            }
        }

        if (fromScreenPoint!=null && toScreenPoint!=null) {
            int fromX = fromScreenPoint.x ;
            int fromY = fromScreenPoint.y;
            int toX = toScreenPoint.x;
            int toY = toScreenPoint.y;

            polylineOpts.strokeShader(new LinearGradient(fromX, fromY, toX, toY,
                    colors, null, Shader.TileMode.CLAMP));
        }

        RichPolyline polyline = polylineOpts.build();
        richLayer.addShape(polyline);
    }
}

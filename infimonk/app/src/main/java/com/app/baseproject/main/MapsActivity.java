package com.app.baseproject.main;

import androidx.fragment.app.FragmentActivity;

import android.location.Location;
import android.os.Bundle;
import android.util.Log;

import com.app.baseproject.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private MapPresenter mapPresenter;
    ArrayList<Geofence> geofence_list = new ArrayList<>();
    ArrayList<Double> distance_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initObjects();
    }

    private void initObjects() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mapPresenter = new MapPresenter(this);
        mapPresenter.requestGetGeofence();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }

    void setMarker() {
        LatLng latLng = null;
        Geofence geofence = null;
        for (int i = 0; i < geofence_list.size(); i++) {
            geofence = geofence_list.get(i);
            latLng = new LatLng(Double.parseDouble(geofence.getLatitude()), Double.parseDouble(geofence.getLongitude()));
            mMap.addMarker(new MarkerOptions().position(latLng).title(geofence.getId()));

            if (i == 0) {
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng).tilt(60).zoom(15.0f).bearing(300).build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }

        findDistances();
    }

    private void findDistances() {
        Geofence geofence = null;
        LatLng latLng_1, latLng_2 = null;

        distance_list.clear();

        for (int i = 0; i < geofence_list.size(); i++) {
            geofence = geofence_list.get(i);
            latLng_1 = new LatLng(Double.parseDouble(geofence.getLatitude()), Double.parseDouble(geofence.getLongitude()));

            for (int j = i + 1; j < geofence_list.size(); j++) {
                geofence = geofence_list.get(j);
                latLng_2 = new LatLng(Double.parseDouble(geofence.getLatitude()), Double.parseDouble(geofence.getLongitude()));

                distance_list.add(CalculationByDistance(latLng_1, latLng_2));
            }
        }

        Log.d("1111", "distance_list: " + distance_list);
        findShortDistances();
    }


    private double CalculationByDistance(LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double lat1 = StartP.latitude;
        double lat2 = EndP.latitude;
        double lon1 = StartP.longitude;
        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));
        Log.i("1111", valueResult + "   " + kmInDec + "KM  " + meterInDec + " Meter");

        return Radius * c;
    }

    private void findShortDistances() {
        try {
            double minimum = distance_list.get(0);
            for (int i = 0; i < distance_list.size(); i++) {
                if (minimum > distance_list.get(0)) {
                    minimum = distance_list.get(0);
                }
            }
            Log.d("1111", "minimum: " + minimum);

            mapPresenter.requestPostGeofence(minimum);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

package com.example.how_about_camping;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    static final String mapKey = "Z8onKyMPwFKBJx9Suxobr1H%2BC0rIRFCP7HVCDG4RmEOtYTeBkdMUwNibwfGtog%2FOxr19CRgmC185F34U9AT2LQ%3D%3D";

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        //서울 북위 37.5642135° 동경 127.0016985°
        LatLng Korea = new LatLng(37.5642135, 127.0016985);
        mMap.addMarker(new MarkerOptions().position(Korea).title("Marker in Seoul"));//마커표시
        mMap.moveCamera(CameraUpdateFactory.newLatLng(Korea));
        mMap.moveCamera(CameraUpdateFactory.zoomTo(14));


    }



}
package com.example.rajankali.travel;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.example.rajankali.travel.databinding.ActivityMapsBinding;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener,MapInteractor {

    private static final int REQUEST_CHECK_SETTINGS = 16;
    private GoogleMap mMap;
    private boolean isActive = false;
    private LocationManager locationManager;
    private static final int LOCATION_REQUEST_CODE = 29;
    private ArrayList<LatLng> travelledLocations; //added
    private MapViewModel mapViewModel;
    private float totalDistanceCovered = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMapsBinding dataBinding = DataBindingUtil.setContentView(this, R.layout.activity_maps);
        mapViewModel = new MapViewModel(this);
        dataBinding.setMapData(mapViewModel);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        travelledLocations = new ArrayList<>();
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        isActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();
        isActive = false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}
                    , LOCATION_REQUEST_CODE);
        }else {
            setLastKnownLoc();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        LatLng latLng = new LatLng(latitude, longitude);
        if(travelledLocations.size() > 0){
            LatLng oldLocation = travelledLocations.get(travelledLocations.size() - 1);
            totalDistanceCovered += Util.calculateDistance(latLng,oldLocation);
        }
        //added
        mapViewModel.onDistanceChanged(Util.round(totalDistanceCovered));
        travelledLocations.add(latLng);
        redrawLine();
    }

    private void redrawLine(){
        PolylineOptions options = new PolylineOptions().width(15).color(Color.parseColor("#3377ff")).geodesic(true);
        for (int i = 0; i < travelledLocations.size(); i++) {
            LatLng point = travelledLocations.get(i);
            options.add(point);
        }
        if(isActive) {
            mMap.clear();
            mMap.addPolyline(options);
            animateCameraToLoc(travelledLocations.get(travelledLocations.size()-1));
        }
    }

    @Override
    public void onRideEnd() {
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException ignored) {
            Log.d("Error",ignored.getMessage());
        }
        travelledLocations.clear();
    }

    @Override
    public void onRideStart() {
        mMap.clear();
        travelledLocations.clear();
        totalDistanceCovered = 0F;
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        try {
            proceedFurther();
        } catch (SecurityException ignored) {
            Log.d("Error",ignored.getMessage());
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    displayLocationSettingsRequest(this);
                }
        }
    }

    private void proceedFurther() throws SecurityException{
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        String locationProvider = locationManager.getBestProvider(criteria, false);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
        locationManager.requestLocationUpdates(locationProvider, 0, 1, this);
    }

    private void setLastKnownLoc(){
        Location loc = getLastBestLocation();
        double currentLatitude = 17.473566F;
        double currentLongitude = 78.570368F;
        if (loc != null) {
            currentLatitude = loc.getLatitude();
            currentLongitude = loc.getLongitude();
        }
        animateCameraToLoc(new LatLng(currentLatitude,currentLongitude));
    }

    private void animateCameraToLoc(LatLng latLng){
        LatLng currentLocation = new LatLng(latLng.latitude, latLng.longitude);
        mMap.addMarker(new MarkerOptions().position(currentLocation).title("Source")
        .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLocation));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(16));
    }

    private Location getLastBestLocation() throws SecurityException {
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }
        long NetLocationTime = 0;
        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }
        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }


    //Enable Gps
    private void displayLocationSettingsRequest(final Context context) {
        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(context)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(10000L / 2);
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        resolutionRequired(status);
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                    default:
                        resolutionRequired(status);
                        break;
                }
            }
        });
    }

    private void resolutionRequired(Status status) {
        try {
            status.startResolutionForResult(this, REQUEST_CHECK_SETTINGS);
        } catch (IntentSender.SendIntentException e) {
            Log.d("error",e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode != 0) {
                Toast.makeText(this, "Location enabled", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}

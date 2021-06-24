package com.example.dogwalker;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.storage.FirebaseStorage;

public abstract class BackgroundAppCompatActivity extends AppCompatActivity {

    private static final int REQUEST_FOR_LOCATION = 0011;
    private static final long UPDATE_INTERVAL = 10000;      // 10s
    private static final long FASTEST_INTERVAL = 2000;      // 2s
    private static final float SMALLEST_DISPLACEMENT = 10;  // 10m
    private static final double QUERY_RADIUS = 30;          // 30km

    protected FirebaseAuth auth;
    protected FirebaseUser currentUser;
    protected FirebaseDatabase database;
    protected DatabaseReference userRef;
    protected FirebaseStorage storage;

    private DatabaseReference messageReference;
    private ChildEventListener messageListener;

    private GeoFire geoFire;
    protected GeoQuery geoQuery;

    private FusedLocationProviderClient fusedLocationClient;    // GMS reference
    private LocationRequest locationRequest;                    // Get location
    private LocationCallback locationCallback;                  // Get notified when location changes

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users/" + currentUser.getUid());
        storage = FirebaseStorage.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();

        geoFire = new GeoFire(database.getReference("GeoFire"));
        geoQuery = null;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                if (locationResult == null) return;
                Location lastLocation = locationResult.getLastLocation();
                newLocation(lastLocation);
            }
        };

        setRequestLocationUpdates();
    }

    private void setRequestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            Toast.makeText(this, "This app requires access to your location.", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION }, REQUEST_FOR_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length < 0 || grantResults[0] == PackageManager.PERMISSION_GRANTED || requestCode == REQUEST_FOR_LOCATION
                && ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } else {
            Toast.makeText(this, "To use this app, we must have access to your location. Please restart the app and enable location permissions.",
                    Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, SplashActivity.class);
            intent.putExtra("exit", true);
            startActivity(intent);
            finish();
        }
    }

    private void newLocation(Location lastLocation) {

        double latitude = lastLocation.getLatitude();
        double longitude = lastLocation.getLongitude();

        if (geoQuery != null)
            geoQuery.setCenter(new GeoLocation(latitude, longitude));
        else {
            geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), QUERY_RADIUS);
            setGeoQuery(lastLocation);
        }

        userRef.runTransaction(new Transaction.Handler() {
            @NonNull @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                currentData.child("latitude").setValue(String.valueOf(latitude));
                currentData.child("longitude").setValue(String.valueOf(longitude));
                return Transaction.success(currentData);
            }
            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (committed) {
                    geoFire.setLocation(currentUser.getUid(), new GeoLocation(latitude, longitude));
                    Log.d("location", "user location updated!");
                }
            }
        });
    }

    protected void setGeoQuery(Location lastLocation) {
        geoQuery = geoFire.queryAtLocation(new GeoLocation(lastLocation.getLatitude(), lastLocation.getLongitude()), QUERY_RADIUS);
    }
}

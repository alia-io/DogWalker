package com.example.dogwalker.messaging;

import com.example.dogwalker.BackgroundAppCompatActivity;
import com.example.dogwalker.CircleTransform;
import com.example.dogwalker.R;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class WalkActivity extends BackgroundAppCompatActivity implements OnMapReadyCallback {

    private boolean selfIsWalker;
    private String targetUserId;

    private MessageRecyclerAdapter messageRecyclerAdapter;
    private EditText newMessageText;

    private DatabaseReference targetLocationReference;
    private ValueEventListener targetLocationListener;

    private GoogleMap map;
    private Marker targetMarker;
    private boolean followTarget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walk);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView imageView = findViewById(R.id.profile_picture);
        TextView targetNameView = findViewById(R.id.profile_name);
        newMessageText = findViewById(R.id.new_message_text);

        database.getReference("Users/" + currentUser.getUid() + "/currentWalk").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null && !snapshot.getValue().toString().equals("NONE")) {
                    database.getReference("Walks/" + snapshot.getValue().toString()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot != null && snapshot.hasChild("walker") && snapshot.hasChild("owner")
                                    && snapshot.child("walker").getValue() != null && snapshot.child("owner") != null) {
                                if (snapshot.child("walker").getValue().toString().equals(currentUser.getUid())) {
                                    targetUserId = snapshot.child("owner").getValue().toString();
                                    selfIsWalker = true;
                                } else if (snapshot.child("owner").getValue().toString().equals(currentUser.getUid())) {
                                    targetUserId = snapshot.child("walker").getValue().toString();
                                    selfIsWalker = false;
                                } else {
                                    finish();
                                    return;
                                }
                                database.getReference("Users/" + targetUserId).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot != null) {
                                            if (snapshot.hasChild("profilePicture") && snapshot.child("profilePicture").getValue() != null) {
                                                Picasso.get().load(snapshot.child("profilePicture").getValue().toString())
                                                        .transform(new CircleTransform()).into(imageView);
                                            }
                                            if (snapshot.hasChild("profileName") && snapshot.child("profileName").getValue() != null)
                                                targetNameView.setText(snapshot.child("profileName").getValue().toString());
                                        }
                                    }
                                    @Override public void onCancelled(@NonNull DatabaseError error) { }
                                });
                                setRecyclerAdapter();
                                if (targetUserId != null && map != null) setTargetLocationListener();
                            } else finish();
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) { }
                    });
                } else finish();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setRecyclerAdapter() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        messageRecyclerAdapter = new MessageRecyclerAdapter(auth, currentUser, database, recyclerView, targetUserId);
        recyclerView.setAdapter(messageRecyclerAdapter);
    }

    @Override
    protected void setGeoQuery(double latitude, double longitude) {
        super.setGeoQuery(latitude, longitude);
        if (!followTarget) {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(12).build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        map = googleMap;
        if (!checkLocationPermission()) return;
        map.setMyLocationEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setMinZoomPreference(15);
        map.setMaxZoomPreference(30);

        map.setOnMyLocationButtonClickListener(() -> {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(new LatLng(location.getLatitude(), location.getLongitude())).zoom(12).build();
                        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        followTarget = false;
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(WalkActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            return false;
        });

        map.setOnMarkerClickListener(marker -> {
            CameraPosition cameraPosition = new CameraPosition.Builder().target(marker.getPosition()).zoom(12).build();
            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            followTarget = true;
            return true;
        });
    }

    private void setTargetLocationListener() {
        targetLocationReference = database.getReference("Users/" + targetUserId + "/location");
        targetLocationListener = targetLocationReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null && snapshot.hasChild("latitude") && snapshot.hasChild("longitude")
                        && snapshot.child("latitude").getValue() != null && snapshot.child("longitude").getValue() != null) {
                    double latitude = Double.parseDouble(snapshot.child("latitude").getValue().toString());
                    double longitude = Double.parseDouble(snapshot.child("longitude").getValue().toString());
                    if (targetMarker == null) {
                        BitmapDescriptor icon;
                        if (selfIsWalker) icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_human_marker);
                        else icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_dog_marker);
                        targetMarker = map.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).icon(icon));
                    } else {
                        targetMarker.setPosition(new LatLng(latitude, longitude));
                        if (followTarget) {
                            CameraPosition cameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(12).build();
                            map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        }
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    protected void setNotificationIcon() { notificationIcon = findViewById(R.id.action_notification); }

    @Override
    protected boolean isTargetChatOpen(String userId) {
        if (targetUserId == null) return false;
        return this.targetUserId.equals(userId);
    }

    public void send(View view) {
        String messageText = newMessageText.getText().toString();
        if (messageText.length() <= 0) {
            Toast.makeText(this, "Please write a message to send", Toast.LENGTH_SHORT).show();
            return;
        }
        messageRecyclerAdapter.onSendNewMessage(this, messageText);
        newMessageText.setText("");
    }

    private boolean checkLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onBackPressed() { finish(); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (targetLocationReference != null && targetLocationListener != null)
            targetLocationReference.removeEventListener(targetLocationListener);
        messageRecyclerAdapter.removeListener();
    }
}
package com.example.dogwalker;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.dogwalker.messaging.MessageActivity;
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
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class BackgroundAppCompatActivity extends AppCompatActivity {

    private static final int REQUEST_FOR_LOCATION = 0011;
    private static final long UPDATE_INTERVAL = 10000;      // 10s
    private static final long FASTEST_INTERVAL = 2000;      // 2s
    private static final float SMALLEST_DISPLACEMENT = 10;  // 10m
    private static final double QUERY_RADIUS = 30;          // 30km

    protected FirebaseAuth auth;
    protected FirebaseUser currentUser;
    protected FirebaseDatabase database;
    protected DatabaseReference currentUserReference;
    protected FirebaseStorage storage;

    private GeoFire geoFire;
    protected GeoQuery geoQuery;

    private FusedLocationProviderClient fusedLocationClient;    // GMS reference
    private LocationRequest locationRequest;                    // Get location
    private LocationCallback locationCallback;                  // Get notified when location changes

    private Query notificationQuery;
    private ChildEventListener notificationListener;
    protected ImageView notificationIcon;
    private List<String> notificationKeyList = new ArrayList<>();
    private Map<String, MessageNotification> notifications = new HashMap<>();
    private Map<String, MessageNotification> unViewedNotifications = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        currentUserReference = database.getReference("Users/" + currentUser.getUid());
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
        setUpUserNotifications();
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

        currentUserReference.runTransaction(new Transaction.Handler() {
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

    private void setUpUserNotifications() {
        setNotificationIcon();
        AnimatorSet animatorSet = getNotificationAnimation();
        setNotificationDatabaseListener(animatorSet);
        setNotificationClickListeners();
    }

    protected abstract void setNotificationIcon();

    private AnimatorSet getNotificationAnimation() {
        AnimatorSet ringer = new AnimatorSet();
        ValueAnimator firstAnim = ObjectAnimator.ofFloat(notificationIcon, "rotation", 0, -30);
        ValueAnimator mainAnim = ObjectAnimator.ofFloat(notificationIcon, "rotation", -30, 30);
        ValueAnimator lastAnim = ObjectAnimator.ofFloat(notificationIcon, "rotation", 30, 0);
        mainAnim.setRepeatMode(ValueAnimator.REVERSE);
        mainAnim.setRepeatCount(6);
        firstAnim.setDuration(10);
        mainAnim.setDuration(120);
        lastAnim.setDuration(10);
        ringer.play(firstAnim).before(mainAnim);
        ringer.play(lastAnim).after(mainAnim);
        return ringer;
    }

    private void setNotificationDatabaseListener(AnimatorSet ringer) {
        final AtomicBoolean notificationAlert = new AtomicBoolean(false);
        notificationQuery = currentUserReference.child("notifications").orderByKey();
        notificationListener = notificationQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot != null && snapshot.getKey() != null && snapshot.getValue() != null) {
                    String notificationKey = snapshot.getKey();
                    MessageNotification notification = snapshot.getValue(MessageNotification.class);
                    if (!isTargetChatOpen(notification.getUserId()) && !notifications.containsKey(notificationKey)) {
                        notificationKeyList.add(0, notificationKey);
                        notifications.put(notificationKey, notification);
                        if (!notification.isViewed() && !notificationAlert.get()) {
                            unViewedNotifications.put(notificationKey, notification);
                            notificationAlert.set(true);
                            notificationIcon.setImageResource(R.drawable.ic_notify_active);
                            new Thread(() -> {
                                while(notificationAlert.get()) {
                                    notificationIcon.post(ringer::start);
                                    try {
                                        Thread.sleep(5000);
                                    } catch (InterruptedException e) {
                                        Log.d("NotificationAlert", "Sleep Failure");
                                    }
                                }
                            }).start();
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getKey() != null && snapshot.getValue() != null) {
                    String notificationKey = snapshot.getKey();
                    if (notifications.containsKey(notificationKey)) {
                        unViewedNotifications.remove(notificationKey);
                        notificationKeyList.remove(notificationKey);
                        notifications.remove(notificationKey);
                        if (notificationAlert.get() && unViewedNotifications.isEmpty()) {
                            notificationAlert.set(false);
                            notificationIcon.post(() -> notificationIcon.setImageResource(R.drawable.ic_notify_inactive));
                        }
                    }
                }
            }

            @Override public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void setNotificationClickListeners() {
        notificationIcon.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(BackgroundAppCompatActivity.this, v);
            MenuInflater inflater = popup.getMenuInflater();
            Menu menu = popup.getMenu();
            inflater.inflate(R.menu.menu_notifications, menu);
            for (int i = 0; i < notificationKeyList.size(); i++) {
                MessageNotification notification = notifications.get(notificationKeyList.get(i));
                if (notification.getMessageType().equals("message"))
                    menu.add(0, i, Menu.NONE, getString(R.string.message_notification) + " " + notification.getUserName());
                else if (notification.getMessageType().equals("add_contact"))
                    menu.add(0, i, Menu.NONE, getString(R.string.contact_notification) + " " + notification.getUserName());
                else if (notification.getMessageType().equals("walk_request"))
                    menu.add(0, i, Menu.NONE, getString(R.string.walk_request_notification) + " " + notification.getUserName());
            }

            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    String notificationKey = notificationKeyList.get(item.getItemId());
                    MessageNotification notification = notifications.get(notificationKey);
                    String userId = notification.getUserId();
                    Intent intent = new Intent(BackgroundAppCompatActivity.this, MessageActivity.class);
                    intent.putExtra("user_id", userId);
                    if (notification.getMessageType().equals("message")) {
                        currentUserReference.child("notifications/" + notificationKey).setValue(null)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        startActivity(intent);
                                        if (!(BackgroundAppCompatActivity.this instanceof HomeActivity)) finish();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(BackgroundAppCompatActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else if (notification.getMessageType().equals("add_contact")) {
                        // TODO
                    } else if (notification.getMessageType().equals("walk_request")) {
                        intent.putExtra("show_request", notification.getMessageId());
                        startActivity(intent);
                        if (!(BackgroundAppCompatActivity.this instanceof HomeActivity)) finish();
                    }
                    return true;
                }
            });
            popup.show();
        });
    }

    protected boolean isTargetChatOpen(String userId) { return false; }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }
}

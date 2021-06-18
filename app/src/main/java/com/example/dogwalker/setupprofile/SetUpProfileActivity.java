package com.example.dogwalker.setupprofile;

import com.example.dogwalker.R;
import com.example.dogwalker.SplashActivity;
import com.example.dogwalker.User;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Date;
import java.util.UUID;

public class SetUpProfileActivity extends AppCompatActivity implements FragmentTracker, PopupMenu.OnMenuItemClickListener {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private FirebaseStorage storage;

    private User user;
    private Uri uri = null;

    private int currentFragment = 0;
    private Fragment1 fragment1;
    private Fragment2 fragment2;
    private Fragment3 fragment3;

    private ImageView leftArrow;
    private ImageView rightArrow;
    private GestureDetectorCompat gestureDetector;

    private static final int REQUEST_FOR_CAMERA = 0011;
    private static final int OPEN_FILE = 0012;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_up_profile);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users/" + currentUser.getUid());
        storage = FirebaseStorage.getInstance();

        leftArrow = findViewById(R.id.left_arrow);
        rightArrow = findViewById(R.id.right_arrow);

        leftArrow.setOnClickListener(v -> goPrevious());
        rightArrow.setOnClickListener(v -> goNext());
        gestureDetector = new GestureDetectorCompat(this, new GestureListener());

        fragment1 = new Fragment1();
        fragment2 = new Fragment2();
        fragment3 = new Fragment3();

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        goNext();
    }

    @Override
    public void goNext() {
        if (currentFragment == 0) {
            loadFragment(fragment1);
            rightArrow.setVisibility(View.VISIBLE);
        } else if (currentFragment == 1) {
            loadFragment(fragment2);
            leftArrow.setVisibility(View.VISIBLE);
        } else if (currentFragment == 2) {
            loadFragment(fragment3);
        } else {
            if (currentFragment == 3) finishProfileSetup();
            return;
        }
        currentFragment++;
    }

    @Override
    public void goPrevious() {
        if (currentFragment == 2) {
            loadFragment(fragment1);
            leftArrow.setVisibility(View.INVISIBLE);
        } else if (currentFragment == 3) {
            loadFragment(fragment2);
        } else return;
        currentFragment--;
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
        fragmentTransaction.commit();
    }

    @Override
    public void saveFragment1(boolean dogOwner, boolean dogWalker, int ownerY, int ownerM, int ownerD, int walkerY, int walkerM, int walkerD) {
        user.setDogOwner(dogOwner);
        user.setDogWalker(dogWalker);
        // TODO: validate year/month/day input: year <=50, month <=11, day <=30
        if (dogOwner) {
            long milliseconds = (((long) (30.4375 * ((12 * ownerY) + ownerM))) + ownerD) * 86400000;
            user.setDogOwnerExperience(new Date().getTime() - milliseconds);
        }
        if (dogWalker) {
            long milliseconds = (((long) (30.4375 * ((12 * walkerY) + walkerM))) + walkerD) * 86400000;
            user.setDogWalkerExperience(new Date().getTime() - milliseconds);
        }
    }

    @Override
    public void saveFragment2(Uri uri) {
        user.setProfilePicture(uri.toString());
    }

    @Override
    public void saveFragment3(String aboutMe) {
        user.setProfileAboutMe(aboutMe);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        final int takePicture = R.id.take_picture;
        final int uploadPicture = R.id.upload_picture;
        switch (item.getItemId()) {
            case takePicture:
                takeProfilePicture();
                return true;
            case uploadPicture:
                Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select File"), OPEN_FILE);
                return true;
            default: return false;
        }
    }

    public void takeProfilePicture() {
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "We need permission to access your camera and photos", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_FOR_CAMERA);
        } else takePhoto();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && requestCode == REQUEST_FOR_CAMERA) {
            if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                takePhoto();
        } else
            Toast.makeText(this, "We need permission to access your camera and photos", Toast.LENGTH_SHORT).show();
    }

    private void takePhoto() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        uri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        Intent chooser = Intent.createChooser(intent, "Select Camera App");
        if (intent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(chooser, REQUEST_FOR_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_FOR_CAMERA && resultCode == RESULT_OK) {
            if (uri == null)
                Toast.makeText(this, "An error occurred while taking your photo", Toast.LENGTH_SHORT).show();
            else {
                fragment2.setProfilePicturePreview(uri);
                uri = null;
            }
        } else if (requestCode == OPEN_FILE && resultCode == RESULT_OK) {
            fragment2.setProfilePicturePreview(data.getData());
        }
    }

    public void finishProfileSetup() {
        if (!user.isDogOwner() && !user.isDogWalker()) {
            Toast.makeText(this, "You must be either a dog owner or a dog walker to continue!", Toast.LENGTH_SHORT).show();
            return;
        }
        updateUser();
        startActivity(new Intent(this, SplashActivity.class));
        finish();
    }

    private void updateUser() {
        final StorageReference imageRef = storage.getReference("Images/" + UUID.randomUUID().toString() + ".jpg");
        imageRef.putFile(Uri.parse(user.getProfilePicture()))
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    user.setProfilePicture(uri.toString());
                                    userRef.setValue(user)
                                            .addOnSuccessListener(aVoid ->
                                                    Toast.makeText(SetUpProfileActivity.this, "Your profile as been set up!", Toast.LENGTH_SHORT).show())
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(SetUpProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(SetUpProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e ->
                        Toast.makeText(SetUpProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) {
            if (event1.getX() < event2.getX()) {
                goPrevious();
                return true;
            } else if (event1.getX() > event2.getX()) {
                goNext();
                return true;
            }
            return false;
        }
    }
}
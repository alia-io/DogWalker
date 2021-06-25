package com.example.dogwalker.editdogs;

import com.example.dogwalker.Dog;
import com.example.dogwalker.BackgroundAppCompatActivity;
import com.example.dogwalker.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import java.util.UUID;

public class EditDogsActivity extends BackgroundAppCompatActivity implements EditDogDialogListener, DogDetailItemListener {

    private ConstraintLayout activityLayout;
    private EditDogFragment editDogFragment;
    private Uri uri = null;

    private EditDogsRecyclerAdapter recyclerAdapter;

    private static final int REQUEST_FOR_CAMERA = 0012;
    private static final int REQUEST_FOR_FILE = 0013;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_dogs);

        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activityLayout = findViewById(R.id.layout_parent);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        recyclerAdapter = new EditDogsRecyclerAdapter(this, recyclerView, getResources());
        recyclerView.setAdapter(recyclerAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        recyclerAdapter.removeListener();
    }

    public void addDog(View view) { startEditDogFragment("", "add_dog"); }

    @Override
    public void startEditDogFragment(String key, String tag) {
        editDogFragment = EditDogFragment.newInstance(R.layout.fragment_edit_dog, key,
                activityLayout.getHeight() - (getSupportActionBar().getHeight() * 3));
        editDogFragment.show(getSupportFragmentManager(), tag);
    }

    @Override
    public void takeProfilePicture() {
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "We need permission to access your camera and photos", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_FOR_CAMERA);
        } else takePhoto();
    }

    @Override
    public void uploadProfilePicture() {
        Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_FOR_FILE);
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
                editDogFragment.setProfilePicturePreview(uri);
                uri = null;
            }
        } else if (requestCode == REQUEST_FOR_FILE && resultCode == RESULT_OK) {
            editDogFragment.setProfilePicturePreview(data.getData());
        }
    }

    @Override
    public void setDog(String dogKey, Dog dog, boolean isDogNew, boolean isProfilePictureNew) {
        if (isProfilePictureNew) uploadProfilePicture(dogKey, dog, isDogNew);
        else if (isDogNew) saveNewDogToDatabase(dog);
        else updateDogInDatabase(dogKey, dog);
    }

    private void uploadProfilePicture(String dogKey, Dog dog, boolean isDogNew) {
        final StorageReference imageRef = storage.getReference("Images/" + UUID.randomUUID().toString() + ".jpg");
        imageRef.putFile(Uri.parse(dog.getProfilePicture()))
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(uri -> {
                                    dog.setProfilePicture(uri.toString());
                                    if (isDogNew) saveNewDogToDatabase(dog);
                                    else updateDogInDatabase(dogKey, dog);
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(EditDogsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e ->
                        Toast.makeText(EditDogsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveNewDogToDatabase(Dog dog) {
        DatabaseReference userDogRef = currentUserRef.child("dogs").push();
        userDogRef.setValue(true)
                .addOnSuccessListener(aVoid ->
                        updateDogInDatabase(userDogRef.getKey(), dog))
                .addOnFailureListener(e ->
                        Toast.makeText(EditDogsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateDogInDatabase(String dogKey, Dog dog) {
        DatabaseReference dogRef = database.getReference("Dogs/" + dogKey);
        dogRef.setValue(dog)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(EditDogsActivity.this, "Your dog's profile was updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(EditDogsActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
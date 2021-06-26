package com.example.dogwalker;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.UUID;

public class EditProfileActivity extends BackgroundAppCompatActivity {

    private User user;
    private ImageView profilePicture;
    private EditText profileName;
    private EditText aboutMe;

    private Uri uri = null;
    private Uri profilePictureUri = null;

    private static final int REQUEST_FOR_CAMERA = 0012;
    private static final int REQUEST_FOR_FILE = 0013;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        profilePicture = findViewById(R.id.profile_picture);
        profileName = findViewById(R.id.user_name);
        aboutMe = findViewById(R.id.about);

        findViewById(R.id.add_profile_picture).setOnClickListener(this::displayProfilePicturePopupMenu);
        findViewById(R.id.save_user).setOnClickListener(v -> saveButton());

        getUserFromDatabase();
    }

    @Override
    protected void setNotificationIcon() { notificationIcon = findViewById(R.id.action_notification); }

    private void getUserFromDatabase() {
        currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                if (user != null) {
                    profileName.setText(user.getProfileName());
                    if (user.getProfileAboutMe() != null) aboutMe.setText(user.getProfileAboutMe());
                    if (user.getProfilePicture() != null)
                        Picasso.get().load(user.getProfilePicture()).transform(new CircleTransform()).into(profilePicture);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void displayProfilePicturePopupMenu(View v) {
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.menu_picture_popup, popupMenu.getMenu());
        popupMenu.show();
        popupMenu.setOnMenuItemClickListener(item -> {
            final int takePicture = R.id.action_take_picture;
            final int uploadPicture = R.id.action_upload_picture;
            switch (item.getItemId()) {
                case takePicture:
                    takeProfilePicture();
                    return true;
                case uploadPicture:
                    uploadProfilePicture();
                    return true;
                default: return false;
            }
        });
    }

    private void takeProfilePicture() {
        if (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "We need permission to access your camera and photos", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE }, REQUEST_FOR_CAMERA);
        } else takePhoto();
    }

    private void uploadProfilePicture() {
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
                setProfilePicturePreview(uri);
                uri = null;
            }
        } else if (requestCode == REQUEST_FOR_FILE && resultCode == RESULT_OK) {
            setProfilePicturePreview(data.getData());
        }
    }

    private void setProfilePicturePreview(Uri imageUri) {
        profilePictureUri = imageUri;
        Picasso.get().load(profilePictureUri.toString()).transform(new CircleTransform()).into(profilePicture);
    }

    private void saveButton() {
        if (profileName.getText().toString().equals(""))
            Toast.makeText(this, "You enter a name to display on your profile.", Toast.LENGTH_SHORT).show();
        else saveUser();
    }

    private void saveUser() {

        boolean newProfilePicture = false;
        String about = aboutMe.getText().toString();

        user.setProfileName(profileName.getText().toString());

        if (about != null) user.setProfileAboutMe(about);

        if (profilePictureUri != null) {
            user.setProfilePicture(profilePictureUri.toString());
            newProfilePicture = true;
        }

        setUser(newProfilePicture);
    }

    private void setUser(boolean isProfilePictureNew) {
        if (isProfilePictureNew) uploadProfilePictureToDatabase();
        else updateUserInDatabase();
    }

    private void uploadProfilePictureToDatabase() {
        final StorageReference imageRef = storage.getReference("Images/" + UUID.randomUUID().toString() + ".jpg");
        imageRef.putFile(Uri.parse(user.getProfilePicture()))
                .addOnSuccessListener(taskSnapshot ->
                        imageRef.getDownloadUrl()
                                .addOnSuccessListener(imageUri -> {
                                    user.setProfilePicture(imageUri.toString());
                                    updateUserInDatabase();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(EditProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show()))
                .addOnFailureListener(e ->
                        Toast.makeText(EditProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void updateUserInDatabase() {
        currentUserReference.setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(EditProfileActivity.this, "Your profile was updated!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(EditProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
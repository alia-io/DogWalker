package com.example.dogwalker;

import com.example.dogwalker.editdogs.EditDogsActivity;
import com.example.dogwalker.newwalk.NewWalkFragment;
import com.example.dogwalker.newwalk.NewWalkFragmentTracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class HomeActivity extends AppCompatActivity implements NewWalkFragmentTracker {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference userRef;

    private User user;

    private boolean findDogWalkers;
    private boolean fromContacts;

    private Menu actionBarMenu;
    private ImageView profilePicture;
    private TextView displayName;
    private CheckBox activeOwnerBox;
    private CheckBox activeWalkerBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSupportActionBar(findViewById(R.id.toolbar));

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users/" + currentUser.getUid());

        profilePicture = findViewById(R.id.user_profile_picture);
        displayName = findViewById(R.id.user_display_name);
        activeOwnerBox = findViewById(R.id.looking_for_walkers);
        activeWalkerBox = findViewById(R.id.looking_for_dogs);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                displayName.setText(user.getProfileName());
                if (user.getProfilePicture() != null) {
                    Picasso.get().load(user.getProfilePicture()).transform(new CircleTransform()).into(profilePicture);
                }
                if (user.isDogOwner()) {
                    if (actionBarMenu != null)
                        actionBarMenu.findItem(R.id.action_edit_dogs).setVisible(true);
                    if (user.getDogs().size() > 0) {
                        activeOwnerBox.setVisibility(View.VISIBLE);
                        activeOwnerBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                user.setDogOwnerActive(true);
                                userRef.child("dogOwnerActive").setValue(true)
                                        .addOnSuccessListener(aVoid ->
                                                Toast.makeText(HomeActivity.this, "You are now actively looking for dog walkers!", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e ->
                                                Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            } else {
                                user.setDogOwnerActive(false);
                                userRef.child("dogOwnerActive").setValue(false)
                                        .addOnSuccessListener(aVoid ->
                                                Toast.makeText(HomeActivity.this, "You stopped looking for dog walkers.", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e ->
                                                Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            }
                        });
                        if (user.isDogOwnerActive()) activeOwnerBox.setChecked(true);
                    } else {
                        Toast.makeText(HomeActivity.this, "Welcome! Please add your dogs to your profile to begin.", Toast.LENGTH_SHORT).show();

                    }
                }
                if (user.isDogWalker() && (!user.isDogOwner() || user.getDogs().size() > 0)) {
                    activeWalkerBox.setVisibility(View.VISIBLE);
                    activeWalkerBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        if (isChecked) {
                            user.setDogWalkerActive(true);
                            userRef.child("dogWalkerActive").setValue(true)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(HomeActivity.this, "You are now actively looking for dogs to walk!", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            user.setDogWalkerActive(false);
                            userRef.child("dogWalkerActive").setValue(false)
                                    .addOnSuccessListener(aVoid ->
                                            Toast.makeText(HomeActivity.this, "You stopped looking for dogs to walk.", Toast.LENGTH_SHORT).show())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        }
                    });
                    if (user.isDogWalkerActive()) activeWalkerBox.setChecked(true);
                }




            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void startNewWalk(View view) {
        if (user.isDogOwner() && user.isDogWalker()) {
            NewWalkFragment.newInstance(R.layout.fragment_new_walk, true, false).show(getSupportFragmentManager(), "new_walk");
            return;
        } else if (user.isDogOwner()) setFindDogWalkers(true);
        else setFindDogWalkers(false);
        NewWalkFragment.newInstance(R.layout.fragment_new_walk, false, findDogWalkers).show(getSupportFragmentManager(), "new_walk");
    }

    @Override
    public void setFindDogWalkers(boolean findDogWalkers) { this.findDogWalkers = findDogWalkers; }

    @Override
    public void setFromContacts(boolean fromContacts) { this.fromContacts = fromContacts; }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar_home, menu);
        actionBarMenu = menu;
        if (user != null && user.isDogOwner())
            actionBarMenu.findItem(R.id.action_edit_dogs).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        final int viewProfileId = R.id.action_view_profile;
        final int editProfileId = R.id.action_edit_profile;
        final int editDogsId = R.id.action_edit_dogs;
        final int searchUsersId = R.id.action_search_users;
        final int viewContactsId = R.id.action_view_contacts;
        final int viewLogId = R.id.action_view_log;
        final int logoutId = R.id.action_logout;

        switch (item.getItemId()) {
            case viewProfileId:

                return true;
            case editProfileId:

                return true;
            case editDogsId:
                startActivity(new Intent(this, EditDogsActivity.class));
                return true;
            case searchUsersId:

                return true;
            case viewContactsId:

                return true;
            case viewLogId:

                return true;
            case logoutId:

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
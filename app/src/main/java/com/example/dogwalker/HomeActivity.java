package com.example.dogwalker;

import com.example.dogwalker.editdogs.EditDogsActivity;
import com.example.dogwalker.newwalk.NewWalkFragment0;
import com.example.dogwalker.newwalk.NewWalkFragment1;
import com.example.dogwalker.newwalk.NewWalkFragmentTracker;

import androidx.annotation.NonNull;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.dogwalker.search.SearchUsersActivity;
import com.example.dogwalker.viewprofile.ViewProfileActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class HomeActivity extends BackgroundAppCompatActivity implements NewWalkFragmentTracker {

    private User user;
    private boolean findDogWalkers;
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

        profilePicture = findViewById(R.id.user_profile_picture);
        displayName = findViewById(R.id.user_display_name);
        activeOwnerBox = findViewById(R.id.looking_for_walkers);
        activeWalkerBox = findViewById(R.id.looking_for_dogs);
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
                displayName.setText(user.getProfileName());
                if (user.getProfilePicture() != null) {
                    Picasso.get().load(user.getProfilePicture()).transform(new CircleTransform()).into(profilePicture);
                }
                if (actionBarMenu != null) {
                    if (user.isDogOwner()) {
                        actionBarMenu.findItem(R.id.action_edit_dogs).setVisible(true);
                        if (user.getDogs().size() > 0) {
                            setUserEnabled();
                            setActiveOwnerOnClick();
                            activeOwnerBox.setVisibility(View.VISIBLE);
                            if (user.isDogOwnerActive()) activeOwnerBox.setChecked(true);
                        } else {
                            setUserNotEnabled();
                            Toast.makeText(HomeActivity.this, "Welcome! Please add your dogs to your profile to begin.", Toast.LENGTH_SHORT).show();
                        }
                    } else actionBarMenu.findItem(R.id.action_edit_dogs).setVisible(false);
                    if (user.isDogWalker() && (!user.isDogOwner() || user.getDogs().size() > 0)) {
                        setUserEnabled();
                        setActiveWalkerOnClick();
                        activeWalkerBox.setVisibility(View.VISIBLE);
                        if (user.isDogWalkerActive()) activeWalkerBox.setChecked(true);
                    }
                }

                // TODO: set up the rest of the home page

            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });


    }

    @Override
    protected void setNotificationIcon() { notificationIcon = findViewById(R.id.action_notification); }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar_home, menu);
        actionBarMenu = menu;
        if (user != null) {
            if (user.isDogOwner()) {
                actionBarMenu.findItem(R.id.action_edit_dogs).setVisible(true);
                if (user.getDogs().size() > 0) {
                    setUserEnabled();
                    setActiveOwnerOnClick();
                    activeOwnerBox.setVisibility(View.VISIBLE);
                    if (user.isDogOwnerActive()) activeOwnerBox.setChecked(true);
                } else {
                    setUserNotEnabled();
                    Toast.makeText(HomeActivity.this, "Welcome! Please add your dogs to your profile to begin.", Toast.LENGTH_SHORT).show();
                }
            } else actionBarMenu.findItem(R.id.action_edit_dogs).setVisible(false);
            if (user.isDogWalker() && (!user.isDogOwner() || user.getDogs().size() > 0)) {
                setUserEnabled();
                setActiveWalkerOnClick();
                activeWalkerBox.setVisibility(View.VISIBLE);
                if (user.isDogWalkerActive()) activeWalkerBox.setChecked(true);
            }
        }
        return true;
    }

    private void setUserEnabled() {
        actionBarMenu.findItem(R.id.action_view_messages).setVisible(true);
        actionBarMenu.findItem(R.id.action_search_users).setVisible(true);
        actionBarMenu.findItem(R.id.action_view_contacts).setVisible(true);
        actionBarMenu.findItem(R.id.action_view_log).setVisible(true);
        ((Button) findViewById(R.id.walk_button)).setEnabled(true);
    }

    private void setUserNotEnabled() {
        actionBarMenu.findItem(R.id.action_view_messages).setVisible(false);
        actionBarMenu.findItem(R.id.action_search_users).setVisible(false);
        actionBarMenu.findItem(R.id.action_view_contacts).setVisible(false);
        actionBarMenu.findItem(R.id.action_view_log).setVisible(false);
        ((Button) findViewById(R.id.walk_button)).setEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int viewProfileId = R.id.action_view_profile;
        final int viewMessagesId = R.id.action_view_messages;
        final int editProfileId = R.id.action_edit_profile;
        final int editDogsId = R.id.action_edit_dogs;
        final int searchUsersId = R.id.action_search_users;
        final int viewContactsId = R.id.action_view_contacts;
        final int viewLogId = R.id.action_view_log;
        final int logoutId = R.id.action_logout;
        Intent intent;

        switch (item.getItemId()) {
                //Toolbar toolbar = findViewById(R.id.toolbar);
                //View notificationIcon = toolbar.findViewById(R.id.action_notification);
                //notificationIcon.animate().setDuration(1000).alpha(0F);
            case viewProfileId:
                intent = new Intent(this, ViewProfileActivity.class);
                intent.putExtra("user_id", currentUser.getUid());
                startActivity(intent);
                return true;
            case viewMessagesId:
                // TODO
                return true;
            case editProfileId:
                // TODO
                return true;
            case editDogsId:
                startActivity(new Intent(this, EditDogsActivity.class));
                return true;
            case searchUsersId:
                intent = new Intent(this, SearchUsersActivity.class);
                intent.putExtra("find_walk", "none");
                startActivity(intent);
                return true;
            case viewContactsId:
                // TODO
                return true;
            case viewLogId:
                // TODO
                return true;
            case logoutId:
                auth.signOut();
                startActivity(new Intent(this, SplashActivity.class));
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void startNewWalk(View view) {
        if (user.isDogOwner() && user.isDogWalker()) {
            NewWalkFragment0.newInstance(R.layout.fragment_new_walk_0).show(getSupportFragmentManager(), "new_walk");
            return;
        } else findDogWalkers = user.isDogOwner();
        NewWalkFragment1.newInstance(R.layout.fragment_new_walk_1, findDogWalkers).show(getSupportFragmentManager(), "new_walk");
    }

    @Override
    public void setFindDogWalkers(boolean findDogWalkers) {
        this.findDogWalkers = findDogWalkers;
        NewWalkFragment1.newInstance(R.layout.fragment_new_walk_1, findDogWalkers).show(getSupportFragmentManager(), "new_walk");
    }

    @Override
    public void setFromContacts(boolean fromContacts) {
        if (fromContacts) {
            // TODO: start contacts activity - filter by dogWalkers or dogOwners based on "findDogWalkers"
        } else {
            Intent intent = new Intent(this, SearchUsersActivity.class);
            if (findDogWalkers) intent.putExtra("find_walk", "walkers");
            else intent.putExtra("find_walk", "owners");
            startActivity(intent);
        }
    }

    private void setActiveOwnerOnClick() {
        activeOwnerBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                user.setDogOwnerActive(true);
                currentUserReference.child("dogOwnerActive").setValue(true)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(HomeActivity.this, "You are now actively looking for dog walkers!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                user.setDogOwnerActive(false);
                currentUserReference.child("dogOwnerActive").setValue(false)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(HomeActivity.this, "You stopped looking for dog walkers.", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setActiveWalkerOnClick() {
        activeWalkerBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                user.setDogWalkerActive(true);
                currentUserReference.child("dogWalkerActive").setValue(true)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(HomeActivity.this, "You are now actively looking for dogs to walk!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } else {
                user.setDogWalkerActive(false);
                currentUserReference.child("dogWalkerActive").setValue(false)
                        .addOnSuccessListener(aVoid ->
                                Toast.makeText(HomeActivity.this, "You stopped looking for dogs to walk.", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(HomeActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }
}
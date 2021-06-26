package com.example.dogwalker.search;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogwalker.BackgroundAppCompatActivity;
import com.example.dogwalker.MessageNotification;
import com.example.dogwalker.R;
import com.example.dogwalker.User;
import com.example.dogwalker.message.WalkRequestMessage;
import com.example.dogwalker.viewprofile.ViewProfileActivity;
import com.example.dogwalker.walkrequest.SelectWalkRoleFragment;
import com.example.dogwalker.walkrequest.SendWalkRequestFragment;
import com.example.dogwalker.walkrequest.SendWalkRequestTracker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.UUID;

public class SearchUsersActivity extends BackgroundAppCompatActivity implements SearchUsersClickListener, SendWalkRequestTracker {

    private UserRecyclerAdapter userRecyclerAdapter;
    private String findWalk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_users);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        userRecyclerAdapter = new UserRecyclerAdapter(this, recyclerView, currentUser, database, storage, geoQuery);
        recyclerView.setAdapter(userRecyclerAdapter);

        ((RadioGroup) findViewById(R.id.user_type)).setOnCheckedChangeListener(this::setUserTypeOnCheckedChangeListener);
        ((RadioGroup) findViewById(R.id.user_distance)).setOnCheckedChangeListener(this::setUserDistanceOnCheckedChangeListener);
        ((CheckBox) findViewById(R.id.active_users)).setOnCheckedChangeListener(this::setActiveUsersOnCheckedChangeListener);

        findWalk = getIntent().getStringExtra("find_walk"); // "walkers", "owners", "none"
        if (findWalk.equals("walkers")) {
            ((RadioButton) findViewById(R.id.dog_walkers)).setChecked(true);
            ((RadioButton) findViewById(R.id.nearby)).setChecked(true);
            ((CheckBox) findViewById(R.id.active_users)).setChecked(true);
        } else if (findWalk.equals("owners")) {
            ((RadioButton) findViewById(R.id.dog_owners)).setChecked(true);
            ((RadioButton) findViewById(R.id.nearby)).setChecked(true);
            ((CheckBox) findViewById(R.id.active_users)).setChecked(true);
        } else {

        }
    }

    @Override
    protected void setGeoQuery(double latitude, double longitude) {
        super.setGeoQuery(latitude, longitude);
        userRecyclerAdapter.setLocationListener();
    }

    @Override
    protected void setNotificationIcon() { notificationIcon = findViewById(R.id.action_notification); }

    private void setUserTypeOnCheckedChangeListener(RadioGroup group, int checkedId) {
        final int dogOwnersId = R.id.dog_owners;
        final int dogWalkersId = R.id.dog_walkers;
        final int eitherId = R.id.either;
        switch (checkedId) {
            case dogOwnersId:
                Toast.makeText(this, "Dog Owners", Toast.LENGTH_SHORT).show();
                userRecyclerAdapter.setUserType(Filters.UserType.DOG_OWNERS);
                break;
            case dogWalkersId:
                Toast.makeText(this, "Dog Walkers", Toast.LENGTH_SHORT).show();
                userRecyclerAdapter.setUserType(Filters.UserType.DOG_WALKERS);
                break;
            case eitherId:
                Toast.makeText(this, "Either", Toast.LENGTH_SHORT).show();
                userRecyclerAdapter.setUserType(Filters.UserType.EITHER);
                break;
        }
    }

    private void setUserDistanceOnCheckedChangeListener(RadioGroup group, int checkedId) {
        final int nearby = R.id.nearby;
        final int anywhere = R.id.anywhere;
        switch (checkedId) {
            case nearby:
                Toast.makeText(this, "Nearby", Toast.LENGTH_SHORT).show();
                userRecyclerAdapter.setUserDistance(Filters.UserDistance.NEARBY);
                break;
            case anywhere:
                Toast.makeText(this, "Anywhere", Toast.LENGTH_SHORT).show();
                userRecyclerAdapter.setUserDistance(Filters.UserDistance.ANYWHERE);
                break;
        }
    }

    private void setActiveUsersOnCheckedChangeListener(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            Toast.makeText(this, "Active Users - true", Toast.LENGTH_SHORT).show();
            userRecyclerAdapter.setActiveUsers(Filters.ActiveUsers.ACTIVE_USERS);
        } else {
            Toast.makeText(this, "Active Users - false", Toast.LENGTH_SHORT).show();
            userRecyclerAdapter.setActiveUsers(Filters.ActiveUsers.ANY_USERS);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search_users);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                // TODO: filter current list
                return true;
            }
        });

        return true;
    }

    /* TODO:
        When user clicks another profile picture -> display the user's profile.
            - User profile contains all user & dog info, plus buttons to send message, add contact (with Snack to cancel),
              and request walk (with dialog to set time/Snack to cancel)
        When a user clicks on a "request walk" button -> request to start a walk with a Snack to cancel the action. */

    @Override
    public void onClickProfile(String targetUserId) {
        Intent intent = new Intent(this, ViewProfileActivity.class);
        intent.putExtra("user_id", targetUserId);
        startActivity(intent);
        finish();
    }

    @Override
    public void onClickRequestWalk(String targetUserId, String targetUserName, boolean targetIsOwner, boolean targetIsWalker) {
        if (targetIsOwner && !targetIsWalker) {
            currentUserReference.child("dogWalker").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot != null && snapshot.getValue() != null) {
                        if (Boolean.parseBoolean(snapshot.getValue().toString())) {
                            SendWalkRequestFragment.newInstance(R.layout.fragment_send_walk_request, targetIsWalker, targetUserId, targetUserName)
                                    .show(getSupportFragmentManager(), "request_walk");
                        }
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) { }
            });
        } else if (targetIsWalker && !targetIsOwner) {
            currentUserReference.child("dogOwner").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot != null && snapshot.getValue() != null) {
                        if (Boolean.parseBoolean(snapshot.getValue().toString())) {
                            SendWalkRequestFragment.newInstance(R.layout.fragment_send_walk_request, targetIsWalker, targetUserId, targetUserName)
                                    .show(getSupportFragmentManager(), "request_walk");
                        }
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) { }
            });
        } else {
            currentUserReference.child("dogOwner").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot != null && snapshot.getValue() != null) {
                        final boolean selfIsOwner;
                        if (Boolean.parseBoolean(snapshot.getValue().toString())) selfIsOwner = true;
                        else selfIsOwner = false;
                        currentUserReference.child("dogWalker").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot != null && snapshot.getValue() != null) {
                                    boolean selfIsWalker = false;
                                    if (Boolean.parseBoolean(snapshot.getValue().toString())) selfIsWalker = true;
                                    if (selfIsOwner && selfIsWalker) {
                                        SelectWalkRoleFragment.newInstance(R.layout.fragment_select_walk_role, targetUserId, targetUserName)
                                                .show(getSupportFragmentManager(), "request_walk");
                                    } else if (selfIsOwner || selfIsWalker) {
                                        SendWalkRequestFragment.newInstance(R.layout.fragment_send_walk_request, selfIsOwner, targetUserId, targetUserName)
                                                .show(getSupportFragmentManager(), "request_walk");
                                    }
                                }
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) { }
                        });
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    @Override
    public void setIsTargetWalker(String targetUserId, String targetUserName, boolean isTargetWalker) {
        SendWalkRequestFragment.newInstance(R.layout.fragment_send_walk_request, isTargetWalker, targetUserId, targetUserName)
                .show(getSupportFragmentManager(), "request_walk");
    }

    @Override
    public void setWalkRequest(String targetUserId, String targetUserName, boolean isTargetWalker, Map<String, String> dogs,
                               long walkTime, float payment, String currency, String message) {
        // TODO: prevent requesting walk with self or with others currently in a walk
        String selfUserId = currentUser.getUid();
        WalkRequestMessage walkRequestMessage = new WalkRequestMessage(selfUserId, targetUserId, isTargetWalker, dogs, walkTime, payment, currency, message);
        currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null) {
                    if (snapshot.hasChild("contacts") && snapshot.child("contacts").hasChild(targetUserId)
                            && snapshot.child("contacts").child(targetUserId).getValue() != null) {
                        String chatUUID = snapshot.child("contacts").child(targetUserId).getValue().toString();
                        sendMessage(selfUserId, targetUserId, walkRequestMessage, chatUUID);
                    } else if (snapshot.hasChild("otherUsers") && snapshot.child("otherUsers").hasChild(targetUserId)
                            && snapshot.child("otherUsers").child(targetUserId).getValue() != null) {
                        String chatUUID = snapshot.child("otherUsers").child(targetUserId).getValue().toString();
                        sendMessage(selfUserId, targetUserId, walkRequestMessage, chatUUID);
                    } else { // Chat does not exist yet
                        database.getReference("Users").runTransaction(new Transaction.Handler() {
                            @NonNull @Override
                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                User selfUser = currentData.child(selfUserId).getValue(User.class);
                                User targetUser = currentData.child(targetUserId).getValue(User.class);
                                if (selfUser == null || targetUser == null)
                                    return Transaction.success(currentData);
                                if (selfUser.getContacts().containsKey(targetUserId) && targetUser.getContacts().containsKey(selfUserId)) { // target is a contact
                                    String chatUUID = selfUser.getContacts().get(targetUserId);
                                    sendMessage(selfUserId, targetUserId, walkRequestMessage, chatUUID);
                                } else if (selfUser.getOtherUsers().containsKey(targetUserId) && targetUser.getOtherUsers().containsKey(selfUserId)) { // target has a chat
                                    String chatUUID = selfUser.getOtherUsers().get(targetUserId);
                                    sendMessage(selfUserId, targetUserId, walkRequestMessage, chatUUID);
                                } else { // create a new chat
                                    String newChatUUID = UUID.randomUUID().toString();
                                    selfUser.getOtherUsers().put(targetUserId, newChatUUID);
                                    targetUser.getOtherUsers().put(selfUserId, newChatUUID);
                                    currentData.child(selfUserId).setValue(selfUser);
                                    currentData.child(targetUserId).setValue(targetUser);
                                    sendMessage(selfUserId, targetUserId, walkRequestMessage, newChatUUID);
                                }
                                return Transaction.success(currentData);
                            }
                            @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) { }
                        });
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void sendMessage(String selfUserId, String targetUserId, WalkRequestMessage walkRequestMessage, String chatUUID) {
        DatabaseReference newMessage = database.getReference("Chats/" + chatUUID).push();
        String messageId = newMessage.getKey();
        newMessage.setValue(walkRequestMessage)
                .addOnSuccessListener(aVoid ->
                        currentUserReference.child("profileName").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                                if (snapshot1 != null && snapshot1.getValue() != null) {
                                    String selfUserName = snapshot1.getValue().toString();
                                    database.getReference("Users/" + targetUserId + "/notifications").runTransaction(new Transaction.Handler() {
                                        @NonNull @Override
                                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                            currentData.child(String.valueOf(walkRequestMessage.getTimestamp()))
                                                    .setValue(new MessageNotification("walk_request", selfUserId, selfUserName, messageId));
                                            return Transaction.success(currentData);
                                        }
                                        @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) { }
                                    });
                                }
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) { }
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(SearchUsersActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        userRecyclerAdapter.removeListeners();
    }
}
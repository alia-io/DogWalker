package com.example.dogwalker.viewprofile;

import com.example.dogwalker.BackgroundAppCompatActivity;
import com.example.dogwalker.CircleTransform;
import com.example.dogwalker.MessageNotification;
import com.example.dogwalker.R;
import com.example.dogwalker.User;
import com.example.dogwalker.message.WalkRequestMessage;
import com.example.dogwalker.messaging.MessageActivity;
import com.example.dogwalker.search.SearchUsersActivity;
import com.example.dogwalker.walkrequest.SelectWalkRoleFragment;
import com.example.dogwalker.walkrequest.SendWalkRequestFragment;
import com.example.dogwalker.walkrequest.SendWalkRequestTracker;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Map;
import java.util.UUID;

public class ViewProfileActivity extends BackgroundAppCompatActivity implements DogProfileCallback, SendWalkRequestTracker {

    private DogRecyclerAdapter dogRecyclerAdapter;
    private DatabaseReference userReference;
    private String userId;
    //private User user;

    private TextView userName;
    private ImageView userPicture;
    private FrameLayout userActive;
    private TextView userTypeOwner;
    private TextView userTypeWalker;
    private ImageView divider1;
    private TextView aboutMeTitle;
    private TextView aboutMe;
    private ImageView divider2;
    private TextView myDogsTitle;
    private RecyclerView dogRecyclerView;
    //private ImageView divider3;
    //private TextView myReviewsTitle;
    //private LinearLayout ratingLayout;
    //private RatingBar ratingBar;
    //private RecyclerView reviewsRecyclerView;

    private DatabaseReference userNameReference;
    private DatabaseReference profilePictureReference;
    private DatabaseReference ownerActiveReference;
    private DatabaseReference walkerActiveReference;
    private DatabaseReference dogOwnerReference;
    private DatabaseReference dogWalkerReference;
    private DatabaseReference userAboutMeReference;
    //private DatabaseReference userRatingReference;
    private ValueEventListener userNameListener;
    private ValueEventListener profilePictureListener;
    private ValueEventListener ownerActiveListener;
    private ValueEventListener walkerActiveListener;
    private ValueEventListener dogOwnerListener;
    private ValueEventListener dogWalkerListener;
    private ValueEventListener userAboutMeListener;
    //private ValueEventListener userRatingListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userId = getIntent().getStringExtra("user_id");
        if (userId == null) finish();

        userName = findViewById(R.id.user_display_name);
        userPicture = findViewById(R.id.user_profile_picture);
        userActive = findViewById(R.id.active_user);
        userTypeOwner = findViewById(R.id.dog_owner);
        userTypeWalker = findViewById(R.id.dog_walker);
        divider1 = findViewById(R.id.divider_1);
        aboutMeTitle = findViewById(R.id.about_me_title);
        aboutMe = findViewById(R.id.about_me);
        divider2 = findViewById(R.id.divider_2);
        myDogsTitle = findViewById(R.id.my_dogs_title);
        dogRecyclerView = findViewById(R.id.dogs_recycler_view);
        //divider3 = findViewById(R.id.divider_3);
        //myReviewsTitle = findViewById(R.id.my_reviews_title);
        //ratingLayout = findViewById(R.id.rating_layout);
        //ratingBar = findViewById(R.id.rating_bar);
        //reviewsRecyclerView = findViewById(R.id.reviews_recycler_view);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.scrollToPosition(0);
        dogRecyclerView.setLayoutManager(layoutManager);
        dogRecyclerAdapter = new DogRecyclerAdapter(this, dogRecyclerView, getResources(), userId);
        dogRecyclerView.setAdapter(dogRecyclerAdapter);

        findViewById(R.id.request_walk_button).setOnClickListener(v -> requestWalk());

        userReference = database.getReference("Users/" + userId);
        setProfileListeners();
    }

    @Override
    protected void setNotificationIcon() { notificationIcon = findViewById(R.id.action_notification); }

    private void setProfileListeners() {

        userNameReference = userReference.child("profileName");
        userNameListener = userNameReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null)
                    userName.setText(snapshot.getValue().toString());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        profilePictureReference = userReference.child("profilePicture");
        profilePictureListener = profilePictureReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null)
                    Picasso.get().load(snapshot.getValue().toString()).transform(new CircleTransform()).into(userPicture);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        ownerActiveReference = userReference.child("dogOwnerActive");
        ownerActiveListener = ownerActiveReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                if (snapshot1 != null && snapshot1.getValue() != null) {
                    userReference.child("dogWalkerActive").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                            if (snapshot2 != null && snapshot2.getValue() != null) {
                                if (!Boolean.parseBoolean(snapshot1.getValue().toString()) && !Boolean.parseBoolean(snapshot2.getValue().toString()))
                                    userActive.setVisibility(View.INVISIBLE);
                                else userActive.setVisibility(View.VISIBLE);
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error2) { }
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error1) { }
        });

        walkerActiveReference = userReference.child("dogWalkerActive");
        walkerActiveListener = walkerActiveReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot1) {
                if (snapshot1 != null && snapshot1.getValue() != null) {
                    userReference.child("dogOwnerActive").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot2) {
                            if (snapshot2 != null && snapshot2.getValue() != null) {
                                if (!Boolean.parseBoolean(snapshot1.getValue().toString()) && !Boolean.parseBoolean(snapshot2.getValue().toString()))
                                    userActive.setVisibility(View.INVISIBLE);
                                else userActive.setVisibility(View.VISIBLE);
                            }
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error2) { }
                    });
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error1) { }
        });

        dogOwnerReference = userReference.child("dogOwner");
        dogOwnerListener = dogOwnerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null) {
                    if (Boolean.parseBoolean(snapshot.getValue().toString()))
                        userTypeOwner.setVisibility(View.VISIBLE);
                    else userTypeOwner.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        dogWalkerReference = userReference.child("dogWalker");
        dogWalkerListener = dogWalkerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) { if (snapshot != null && snapshot.getValue() != null) {
                    if (Boolean.parseBoolean(snapshot.getValue().toString()))
                        userTypeWalker.setVisibility(View.VISIBLE);
                    else userTypeWalker.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        userAboutMeReference = userReference.child("profileAboutMe");
        userAboutMeListener = userAboutMeReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null && snapshot.getValue().toString().length() > 0) {
                    divider1.setVisibility(View.VISIBLE);
                    aboutMeTitle.setVisibility(View.VISIBLE);
                    aboutMe.setVisibility(View.VISIBLE);
                    aboutMe.setText(snapshot.getValue().toString());
                } else {
                    divider1.setVisibility(View.GONE);
                    aboutMeTitle.setVisibility(View.GONE);
                    aboutMe.setVisibility(View.GONE);
                    aboutMe.setText("");
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public void toggleDogsVisibility(boolean showDogs) {
        if (showDogs) {
            divider2.setVisibility(View.VISIBLE);
            myDogsTitle.setVisibility(View.VISIBLE);
            dogRecyclerView.setVisibility(View.VISIBLE);
        } else {
            divider2.setVisibility(View.GONE);
            myDogsTitle.setVisibility(View.GONE);
            dogRecyclerView.setVisibility(View.GONE);
        }
    }

    public void addContact(View view) {

    }

    public void sendMessage(View view) {
        Intent intent = new Intent(this, MessageActivity.class);
        intent.putExtra("user_id", userId);
        startActivity(intent);
        finish();
    }

    private void requestWalk() {
        String targetUserId = userId;
        String targetUserName = userName.getText().toString();
        boolean targetIsOwner = userTypeOwner.getVisibility() == View.VISIBLE;
        boolean targetIsWalker = userTypeWalker.getVisibility() == View.VISIBLE;
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
                        Toast.makeText(ViewProfileActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dogRecyclerAdapter.removeListener();
        if (userNameReference != null && userNameListener != null)
            userNameReference.removeEventListener(userNameListener);
        if (profilePictureReference != null && profilePictureListener != null)
            profilePictureReference.removeEventListener(profilePictureListener);
        if (ownerActiveReference != null && ownerActiveListener != null)
            ownerActiveReference.removeEventListener(ownerActiveListener);
        if (walkerActiveReference != null && walkerActiveListener != null)
            walkerActiveReference.removeEventListener(walkerActiveListener);
        if (dogOwnerReference != null && dogOwnerListener != null)
            dogOwnerReference.removeEventListener(dogOwnerListener);
        if (dogWalkerReference != null && dogWalkerListener != null)
            dogWalkerReference.removeEventListener(dogWalkerListener);
        if (userAboutMeReference != null && userAboutMeListener != null)
            userAboutMeReference.removeEventListener(userAboutMeListener);
    }
}
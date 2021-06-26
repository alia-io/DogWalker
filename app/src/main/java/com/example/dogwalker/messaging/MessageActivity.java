package com.example.dogwalker.messaging;

import com.example.dogwalker.BackgroundAppCompatActivity;
import com.example.dogwalker.CircleTransform;
import com.example.dogwalker.MessageNotification;
import com.example.dogwalker.R;
import com.example.dogwalker.User;
import com.example.dogwalker.Walk;
import com.example.dogwalker.message.WalkRequestMessage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class MessageActivity extends BackgroundAppCompatActivity implements ReceiveWalkRequestListener {

    private MessageRecyclerAdapter messageRecyclerAdapter;
    private EditText newMessageText;
    private String targetUserId;
    private String currentNotificationKey;
    private MessageNotification currentNotification;
    private SimpleDateFormat localDateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView imageView = findViewById(R.id.profile_picture);
        TextView targetNameView = findViewById(R.id.profile_name);
        newMessageText = findViewById(R.id.new_message_text);

        targetUserId = getIntent().getStringExtra("user_id");
        currentNotificationKey = getIntent().getStringExtra("show_request");

        // Get other user's profile picture and display name
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

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.scrollToPosition(0);
        recyclerView.setLayoutManager(layoutManager);
        messageRecyclerAdapter = new MessageRecyclerAdapter(auth, currentUser, database, recyclerView, targetUserId);
        recyclerView.setAdapter(messageRecyclerAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentNotificationKey != null && !currentNotificationKey.equals("")) {
            currentUserReference.child("notifications/" + currentNotificationKey).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (snapshot != null && snapshot.getValue() != null) {
                        currentNotification = snapshot.getValue(MessageNotification.class);
                        if (currentNotification != null) {
                            String senderName = currentNotification.getUserName();
                            String messageId = currentNotification.getReferenceKey();
                            if (currentNotification.getNotificationType().equals("walk_request")) {
                                currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot != null && snapshot.getValue() != null) {
                                            if (snapshot.hasChild("contacts") && snapshot.child("contacts").hasChild(targetUserId)) {
                                                String chatId = snapshot.child("contacts").child(targetUserId).getValue().toString();
                                                setWalkRequestMessagePopup(senderName, chatId, messageId);
                                            } else if (snapshot.hasChild("otherUsers") && snapshot.child("otherUsers").hasChild(targetUserId)) {
                                                String chatId = snapshot.child("otherUsers").child(targetUserId).getValue().toString();
                                                setWalkRequestMessagePopup(senderName, chatId, messageId);
                                            }
                                        }
                                    }
                                    @Override public void onCancelled(@NonNull DatabaseError error) { }
                                });
                            }
                        }
                    }
                }
                @Override public void onCancelled(@NonNull DatabaseError error) { }
            });
        }
    }

    private void setWalkRequestMessagePopup(String senderName, String chatId, String messageId) {
        database.getReference("Chats/" + chatId + "/" + messageId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null) {
                    WalkRequestMessage walkRequestMessage = snapshot.getValue(WalkRequestMessage.class);
                    if (walkRequestMessage != null) {
                        StringBuilder dogs = new StringBuilder();
                        for (String dogName : walkRequestMessage.getDogs().values()) {
                            if (dogs.length() != 0) dogs.append(", ");
                            dogs.append(dogName);
                        }
                        String message = walkRequestMessage.getMessage();
                        if (message == null) message = "";
                        ReceiveWalkRequestFragment.newInstance(R.layout.fragment_receive_walk_request, currentNotificationKey, senderName,
                                walkRequestMessage.getWalker(), dogs.toString(), localDateFormat.format(new Date(walkRequestMessage.getWalkTime())),
                                walkRequestMessage.getCurrency() + walkRequestMessage.getPaymentAmount(), message)
                                .show(getSupportFragmentManager(), "walk_request");
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

    @Override
    public void acceptWalkRequest(String notificationKey) {
        if (notificationKey.equals(currentNotificationKey) && currentNotification != null) {
            String messageId = currentNotification.getReferenceKey();
            currentUserReference.child("notifications/" + currentNotificationKey).setValue(null)
                    .addOnSuccessListener(aVoid ->
                            currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot != null && snapshot.getValue() != null) {
                                        if (snapshot.hasChild("contacts") && snapshot.child("contacts").hasChild(targetUserId)) {
                                            String chatId = snapshot.child("contacts").child(targetUserId).getValue().toString();
                                            readWalkRequestMessage(chatId, messageId);
                                        } else if (snapshot.hasChild("otherUsers") && snapshot.child("otherUsers").hasChild(targetUserId)) {
                                            String chatId = snapshot.child("otherUsers").child(targetUserId).getValue().toString();
                                            readWalkRequestMessage(chatId, messageId);
                                        }
                                    }
                                }
                                @Override public void onCancelled(@NonNull DatabaseError error) { }
                            }))
                    .addOnFailureListener(e ->
                            Toast.makeText(MessageActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    private void readWalkRequestMessage(String chatId, String messageId) {
        database.getReference("Chats/" + chatId + "/" + messageId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null) {
                    WalkRequestMessage walkRequestMessage = snapshot.getValue(WalkRequestMessage.class);
                    if (walkRequestMessage != null) {
                        final long walkTime;
                        if (walkRequestMessage.getWalkTime() == 0) walkTime = Calendar.getInstance().getTimeInMillis();
                        else walkTime = walkRequestMessage.getWalkTime();
                        Walk newWalk = new Walk(walkRequestMessage.getOwner(), walkRequestMessage.getWalker(), walkRequestMessage.getWalkTime(),
                                null, walkRequestMessage.getPaymentAmount(), walkRequestMessage.getCurrency(), null,
                                walkRequestMessage.getDogs(), 0);
                        walkRequestMessage.setAccepted(true);
                        String newWalkUUID = UUID.randomUUID().toString();
                        database.getReference("Walks/" + newWalkUUID).setValue(newWalk)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        database.getReference("Users").runTransaction(new Transaction.Handler() {
                                            @NonNull @Override
                                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                                User selfUser = currentData.child(currentUser.getUid()).getValue(User.class);
                                                User targetUser = currentData.child(targetUserId).getValue(User.class);
                                                if (selfUser != null && targetUser != null) {
                                                    // TODO: functionality for a walk that does not start ASAP
                                                    selfUser.getDogWalkingLog().add(0, newWalkUUID);
                                                    selfUser.setCurrentWalk(newWalkUUID);
                                                    targetUser.getDogWalkingLog().add(0, newWalkUUID);
                                                    targetUser.setCurrentWalk(newWalkUUID);
                                                    targetUser.getNotifications().put(String.valueOf(walkTime), new MessageNotification("walk_request_accept",
                                                            currentUser.getUid(), selfUser.getProfileName(), null));
                                                    currentData.child(currentUser.getUid()).setValue(selfUser);
                                                    currentData.child(targetUserId).setValue(targetUser);
                                                }
                                                return Transaction.success(currentData);
                                            }
                                            @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) { }
                                        });
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(MessageActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @Override
    public void declineWalkRequest(String notificationKey) {
        if (notificationKey.equals(currentNotificationKey)) {
            // TODO
        }
    }

    @Override
    public void onBackPressed() { finish(); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messageRecyclerAdapter.removeListener();
    }
}
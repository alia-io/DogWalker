package com.example.dogwalker.messaging;

import com.example.dogwalker.MessageNotification;
import com.example.dogwalker.message.ChatMessage;
import com.example.dogwalker.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogwalker.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class MessageRecyclerAdapter extends RecyclerView.Adapter<MessageRecyclerAdapter.MessageViewHolder> {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference allUsersReference;
    private DatabaseReference currentUserReference;
    private DatabaseReference currentChatReference;
    private ChildEventListener currentChatListener;

    private SimpleDateFormat localDateFormat = new SimpleDateFormat("MMM dd, yyyy HH:mm:ss");

    private String selfUserId;
    private String targetUserId;
    private List<MessageModel> messageList;
    private RecyclerView recyclerView;

    public MessageRecyclerAdapter(FirebaseAuth auth, FirebaseUser currentUser, FirebaseDatabase database,
                                  RecyclerView recyclerView, String targetUserId) {
        this.auth = auth;
        this.currentUser = currentUser;
        this.database = database;
        this.selfUserId = currentUser.getUid();
        this.targetUserId = targetUserId;
        this.allUsersReference = database.getReference("Users");
        this.currentUserReference = allUsersReference.child(selfUserId);
        this.messageList = new ArrayList<>();
        this.recyclerView = recyclerView;

        currentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null) {
                    if (snapshot.hasChild("contacts") && snapshot.child("contacts").hasChild(targetUserId)
                            && snapshot.child("contacts").child(targetUserId).getValue() != null) {
                        String chatUUID = snapshot.child("contacts").child(targetUserId).getValue().toString();
                        currentChatReference = database.getReference("Chats/" + chatUUID);
                        setChatListener();
                    } else if (snapshot.hasChild("otherUsers") && snapshot.child("otherUsers").hasChild(targetUserId)
                            && snapshot.child("otherUsers").child(targetUserId).getValue() != null) {
                        String chatUUID = snapshot.child("otherUsers").child(targetUserId).getValue().toString();
                        currentChatReference = database.getReference("Chats/" + chatUUID);
                        setChatListener();
                    } else { // Chat does not exist yet
                        allUsersReference.runTransaction(new Transaction.Handler() {
                            @NonNull @Override
                            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                User selfUser = currentData.child(selfUserId).getValue(User.class);
                                User targetUser = currentData.child(targetUserId).getValue(User.class);
                                if (selfUser == null || targetUser == null)
                                    return Transaction.success(currentData);
                                if (selfUser.getContacts().containsKey(targetUserId) && targetUser.getContacts().containsKey(selfUserId)) { // target is a contact
                                    String chatUUID = selfUser.getContacts().get(targetUserId);
                                    currentChatReference = database.getReference("Chats/" + chatUUID);
                                } else if (selfUser.getOtherUsers().containsKey(targetUserId) && targetUser.getOtherUsers().containsKey(selfUserId)) { // target has a chat
                                    String chatUUID = selfUser.getOtherUsers().get(targetUserId);
                                    currentChatReference = database.getReference("Chats/" + chatUUID);
                                } else { // create a new chat
                                    String newChatUUID = UUID.randomUUID().toString();
                                    selfUser.getOtherUsers().put(targetUserId, newChatUUID);
                                    targetUser.getOtherUsers().put(selfUserId, newChatUUID);
                                    currentData.child(selfUserId).setValue(selfUser);
                                    currentData.child(targetUserId).setValue(targetUser);
                                    currentChatReference = database.getReference("Chats/" + newChatUUID);
                                }
                                setChatListener();
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

    private void setChatListener() {
        currentChatListener = currentChatReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                MessageModel messageModel = new MessageModel(snapshot.getKey(), snapshot.child("type").getValue().toString(),
                        snapshot.child("sender").getValue().toString(), snapshot.child("receiver").getValue().toString(),
                        snapshot.child("message").getValue().toString(),
                        localDateFormat.format(new Date(Long.parseLong(snapshot.child("timestamp").getValue().toString()))));
                messageList.add(messageModel);
                notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // TODO
            }

            @Override public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    @NonNull @Override
    public MessageRecyclerAdapter.MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_chat_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull MessageRecyclerAdapter.MessageViewHolder holder, int position) {

        MessageModel messageModel = messageList.get(position);
        String sender = messageModel.getSender();
        String receiver = messageModel.getReceiver();

        // Show date if new
        if (position == 0 || !messageList.get(position - 1).getDate().equals(messageModel.getDate())) {
            holder.dateView.setText(messageModel.getDate());
            holder.dateView.setVisibility(View.VISIBLE);
            holder.dateView.setPadding(0, 0, 0, 10);
            holder.constraintSet.connect(holder.messageLayoutId, ConstraintSet.TOP, holder.dateViewId, ConstraintSet.BOTTOM);
        } else {
            holder.dateView.setVisibility(View.GONE);
            holder.dateView.setPadding(0, 0, 0, 0);
            holder.constraintSet.connect(holder.messageLayoutId, ConstraintSet.TOP, holder.parentLayoutId, ConstraintSet.TOP);
        }

        holder.messageView.setText(messageModel.getMessage());
        holder.timeView.setText(messageModel.getTime());

        if (sender.equals(selfUserId) && receiver.equals(targetUserId)) { // Sent message
            holder.messageLayout.setBackgroundResource(R.drawable.sent_message_background);
            holder.constraintSet.clear(holder.messageLayoutId, ConstraintSet.LEFT);
            holder.constraintSet.connect(holder.messageLayoutId, ConstraintSet.RIGHT, holder.parentLayoutId, ConstraintSet.RIGHT);
        } else if (sender.equals(targetUserId) && receiver.equals(selfUserId)) { // Received message
            holder.messageLayout.setBackgroundResource(R.drawable.received_message_background);
            holder.constraintSet.clear(holder.messageLayoutId, ConstraintSet.RIGHT);
            holder.constraintSet.connect(holder.messageLayoutId, ConstraintSet.LEFT, holder.parentLayoutId, ConstraintSet.LEFT);
        }
        holder.constraintSet.applyTo(holder.parentLayout);
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    public void removeListener() {
        if (currentChatReference != null && currentChatListener != null)
            currentChatReference.removeEventListener(currentChatListener);
    }

    public void onSendNewMessage(Context context, String messageText) {
        ChatMessage chatMessage = new ChatMessage(selfUserId, targetUserId, messageText);
        DatabaseReference newMessage = currentChatReference.push();
        String messageId = newMessage.getKey();
        newMessage.setValue(chatMessage)
                .addOnSuccessListener(aVoid ->
                        database.getReference("Users/" + selfUserId + "/profileName").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot != null && snapshot.getValue() != null) {
                                    String selfUserName = snapshot.getValue().toString();
                                    database.getReference("Users/" + targetUserId + "/notifications").runTransaction(new Transaction.Handler() {
                                        @NonNull @Override
                                        public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                                            currentData.child(String.valueOf(chatMessage.getTimestamp()))
                                                    .setValue(new MessageNotification("message", selfUserId, selfUserName, messageId));
                                            return Transaction.success(currentData);
                                        }
                                        @Override public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) { }
                                    });
                                }
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) { }
                        }))
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public ConstraintSet constraintSet;
        public ConstraintLayout parentLayout;
        public LinearLayout messageLayout;
        public FrameLayout walkRequestHolder;
        public Button walkRequestButton;
        public TextView messageView;
        public TextView dateView;
        public TextView timeView;
        public int parentLayoutId;
        public int messageLayoutId;
        public int dateViewId;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            parentLayoutId = R.id.message_layout;
            messageLayoutId = R.id.message_body;
            dateViewId = R.id.message_date;
            parentLayout = itemView.findViewById(parentLayoutId);
            messageLayout = itemView.findViewById(messageLayoutId);
            walkRequestHolder = itemView.findViewById(R.id.walk_request_holder);
            walkRequestButton = itemView.findViewById(R.id.walk_request_button);
            messageView = itemView.findViewById(R.id.message_text);
            dateView = itemView.findViewById(dateViewId);
            timeView = itemView.findViewById(R.id.message_time);
            constraintSet = new ConstraintSet();
            constraintSet.clone(parentLayout);
        }
    }
}

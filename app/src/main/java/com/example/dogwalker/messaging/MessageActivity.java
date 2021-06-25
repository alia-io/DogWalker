package com.example.dogwalker.messaging;

import com.example.dogwalker.BackgroundAppCompatActivity;
import com.example.dogwalker.CircleTransform;
import com.example.dogwalker.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class MessageActivity extends BackgroundAppCompatActivity {

    private MessageRecyclerAdapter messageRecyclerAdapter;
    private EditText newMessageText;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView imageView = findViewById(R.id.profile_picture);
        TextView targetNameView = findViewById(R.id.profile_name);
        newMessageText = findViewById(R.id.new_message_text);

        userId = getIntent().getStringExtra("user_id");

        // Get other user's profile picture and display name
        database.getReference("Users/" + userId).addListenerForSingleValueEvent(new ValueEventListener() {
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
        messageRecyclerAdapter = new MessageRecyclerAdapter(auth, currentUser, database, recyclerView, userId);
        recyclerView.setAdapter(messageRecyclerAdapter);
    }

    public void send(View view) {
        String messageText = newMessageText.getText().toString();
        if (messageText.length() <= 0) {
            Toast.makeText(this, "Please write a message to send", Toast.LENGTH_SHORT).show();
            return;
        }
        messageRecyclerAdapter.onSendNewMessage(messageText);
        newMessageText.setText("");
    }

    @Override
    public void onBackPressed() { finish(); }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        messageRecyclerAdapter.removeListener();
    }
}
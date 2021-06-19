package com.example.dogwalker.editdogs;

import com.example.dogwalker.Dog;
import com.example.dogwalker.R;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class EditDogsRecyclerAdapter extends RecyclerView.Adapter<EditDogsRecyclerAdapter.EditDogViewHolder> {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private DatabaseReference userRef;
    private DatabaseReference myDogsRef;
    private ChildEventListener myDogsEventListener;

    List<Dog> dogList;

    public EditDogsRecyclerAdapter() {

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        userRef = database.getReference("Users/" + currentUser.getUid());
        myDogsRef = database.getReference("Users/" + currentUser.getUid() + "/dogs");

        dogList = new ArrayList<Dog>();

        myDogsEventListener = myDogsRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @NonNull @Override
    public EditDogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull EditDogViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public static class EditDogViewHolder extends RecyclerView.ViewHolder {

        public ImageView profilePicture;
        public ImageView addProfilePicture;

        public EditDogViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.profile_picture);
            addProfilePicture = itemView.findViewById(R.id.add_profile_picture);
        }
    }
}

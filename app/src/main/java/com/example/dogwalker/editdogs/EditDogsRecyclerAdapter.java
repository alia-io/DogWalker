package com.example.dogwalker.editdogs;

import com.example.dogwalker.Dog;
import com.example.dogwalker.R;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class EditDogsRecyclerAdapter extends RecyclerView.Adapter<EditDogsRecyclerAdapter.EditDogViewHolder> {

    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private FirebaseDatabase database;

    List<Dog> dogList;

    public EditDogsRecyclerAdapter() {


        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();

        dogList = new ArrayList<Dog>();
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

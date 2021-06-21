package com.example.dogwalker.search;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder> {

    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    private RecyclerView recyclerView;

    private List<String> keyList;
    private Map<String, UserModel> keyToUser;

    public UserRecyclerAdapter(RecyclerView recyclerView, FirebaseUser currentUser, FirebaseDatabase database, FirebaseStorage storage) {
        this.recyclerView = recyclerView;
        this.currentUser = currentUser;
        this.database = database;
        this.storage = storage;
        keyList = new ArrayList<>();
        keyToUser = new HashMap<>();
    }

    public void setLocationListener(GeoQuery geoQuery) {
        geoQuery.addGeoQueryDataEventListener(new GeoQueryDataEventListener() {

            @Override
            public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                if (passesOtherFilters()) {
                    final String userKey = dataSnapshot.getKey();
                    if (keyToUser.containsKey(userKey) || currentUser.getUid().equals(userKey)) return;
                    database.getReference("Users/" + userKey).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            UserModel userModel = new UserModel(userKey,
                                    snapshot.child("profileName").getValue().toString(),
                                    Boolean.parseBoolean(snapshot.child("dogOwner").getValue().toString()),
                                    Boolean.parseBoolean(snapshot.child("dogWalker").getValue().toString()),
                                    Boolean.parseBoolean(snapshot.child("dogOwnerActive").getValue().toString()),
                                    Boolean.parseBoolean(snapshot.child("dogWalkerActive").getValue().toString()));
                            if (snapshot.hasChild("profilePicture"))
                                userModel.setProfilePicture(snapshot.child("profilePicture").getValue().toString());
                            if (snapshot.hasChild("dogs")) {
                                for (DataSnapshot dogSnapshot : snapshot.child("dogs").getChildren()) {
                                    if (Boolean.parseBoolean(dogSnapshot.getValue().toString())) {
                                        userModel.getDogs().add(dogSnapshot.getKey());
                                    }
                                }
                            }
                            keyList.add(userKey);
                            keyToUser.put(userKey, userModel);
                            notifyItemInserted(keyList.size() - 1);
                            recyclerView.scrollToPosition(keyList.size() - 1);
                        }
                        @Override public void onCancelled(@NonNull DatabaseError error) { }
                    });
                }
            }

            @Override
            public void onDataExited(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) {

            }

            @Override public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) { }
            @Override public void onGeoQueryReady() { }
            @Override public void onGeoQueryError(DatabaseError error) { }
        });
    }

    // TODO: implement this for each type of filter
    private boolean passesOtherFilters() {
        return true;
    }

    @NonNull @Override
    public UserRecyclerAdapter.UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull UserRecyclerAdapter.UserViewHolder holder, int position) {

    }

    public void removeListener() {

    }

    @Override
    public int getItemCount() { return keyList.size(); }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

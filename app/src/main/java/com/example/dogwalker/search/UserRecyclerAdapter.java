package com.example.dogwalker.search;

import com.example.dogwalker.R;
import com.example.dogwalker.CircleTransform;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryDataEventListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.UserViewHolder> {

    private FirebaseUser currentUser;
    private FirebaseDatabase database;
    private FirebaseStorage storage;

    private DatabaseReference allUsersRef;
    private ChildEventListener allUsersListener;

    private SearchUsersClickListener clickListener;
    private RecyclerView recyclerView;

    private GeoQuery geoQuery;
    private GeoQueryDataEventListener locationListener;

    private final List<String> keyList;
    private final Map<String, UserModel> keyToUser;           // User list after searchText filter
    private final Map<String, UserModel> keyToUserMaster;     // Full user list without searchText

    private Filters.UserType userType;
    private Filters.UserDistance userDistance;
    private Filters.ActiveUsers activeUsers;
    private String searchText;
    private boolean firstSearch;

    private static final int MAX_DOGS = 5;

    public UserRecyclerAdapter(SearchUsersClickListener clickListener, RecyclerView recyclerView, FirebaseUser currentUser,
                               FirebaseDatabase database, FirebaseStorage storage, GeoQuery geoQuery) {

        this.clickListener = clickListener;
        this.recyclerView = recyclerView;
        this.currentUser = currentUser;
        this.database = database;
        this.storage = storage;
        this.allUsersRef = database.getReference("Users");
        this.geoQuery = geoQuery;
        this.keyList = new ArrayList<>();
        this.keyToUser = new HashMap<>();
        this.keyToUserMaster = new HashMap<>();
        this.userType = Filters.UserType.EITHER;
        this.userDistance = Filters.UserDistance.ANYWHERE;
        this.activeUsers = Filters.ActiveUsers.ANY_USERS;
        this.firstSearch = true;

        allUsersListener = allUsersRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot != null && snapshot.getValue() != null) {
                    String userKey = snapshot.getKey();
                    if (userKey != null && !keyToUserMaster.containsKey(userKey) && snapshot.getValue() != null) {
                        UserModel userModel = new UserModel(userKey,
                                snapshot.child("profileName").getValue().toString(),
                                Boolean.parseBoolean(snapshot.child("dogOwner").getValue().toString()),
                                Boolean.parseBoolean(snapshot.child("dogWalker").getValue().toString()),
                                Boolean.parseBoolean(snapshot.child("dogOwnerActive").getValue().toString()),
                                Boolean.parseBoolean(snapshot.child("dogWalkerActive").getValue().toString()));
                        if (snapshot.hasChild("profilePicture"))
                            userModel.setProfilePicture(snapshot.child("profilePicture").getValue().toString());
                        if (snapshot.hasChild("dogs") && snapshot.child("dogs").hasChildren()) {
                            Iterable<DataSnapshot> dogSnapshots = snapshot.child("dogs").getChildren();
                            for (DataSnapshot dogSnapshot : dogSnapshots) {
                                if (dogSnapshot != null && dogSnapshot.getKey() != null && dogSnapshot.getValue() != null
                                        && Boolean.parseBoolean(dogSnapshot.getValue().toString())) {
                                    String dogKey = dogSnapshot.getKey();
                                    database.getReference("Dogs/" + dogKey + "profilePicture").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot != null && snapshot.getValue() != null) {
                                                userModel.getDogs().add(dogKey);
                                                userModel.getDogPictures().put(dogKey, snapshot.getValue().toString());
                                            }
                                        }
                                        @Override public void onCancelled(@NonNull DatabaseError error) { }
                                    });

                                }
                            }
                        }
                        keyToUserMaster.put(userKey, userModel);
                        if (!keyList.contains(userKey)) {
                            keyList.add(userKey);
                            keyToUser.put(userKey, userModel);
                            notifyItemInserted(keyList.size() - 1);
                        }
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                if (snapshot != null && snapshot.getValue() != null) {
                    String userKey = snapshot.getKey();
                    if (userKey != null && keyToUserMaster.containsKey(userKey)) {
                        UserModel userModel = new UserModel(userKey,
                                snapshot.child("profileName").getValue().toString(),
                                Boolean.parseBoolean(snapshot.child("dogOwner").getValue().toString()),
                                Boolean.parseBoolean(snapshot.child("dogWalker").getValue().toString()),
                                Boolean.parseBoolean(snapshot.child("dogOwnerActive").getValue().toString()),
                                Boolean.parseBoolean(snapshot.child("dogWalkerActive").getValue().toString()));
                        if (snapshot.hasChild("profilePicture"))
                            userModel.setProfilePicture(snapshot.child("profilePicture").getValue().toString());
                        if (snapshot.hasChild("dogs") && snapshot.child("dogs").hasChildren()) {
                            Iterable<DataSnapshot> dogSnapshots = snapshot.child("dogs").getChildren();
                            for (DataSnapshot dogSnapshot : dogSnapshots) {
                                if (dogSnapshot != null && dogSnapshot.getKey() != null && dogSnapshot.getValue() != null
                                        && Boolean.parseBoolean(dogSnapshot.getValue().toString())) {
                                    String dogKey = dogSnapshot.getKey();
                                    database.getReference("Dogs/" + dogKey + "profilePicture").addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot != null && snapshot.getValue() != null) {
                                                userModel.getDogs().add(dogKey);
                                                userModel.getDogPictures().put(dogKey, snapshot.getValue().toString());
                                            }
                                        }
                                        @Override public void onCancelled(@NonNull DatabaseError error) { }
                                    });

                                }
                            }
                        }
                        keyToUserMaster.remove(userKey);
                        keyToUserMaster.put(userKey, userModel);
                        int position = keyList.indexOf(userKey);
                        if (position != -1) {
                            keyList.remove(position);
                            keyList.add(position, userKey);
                            keyToUser.remove(userKey);
                            keyToUser.put(userKey, userModel);
                            notifyItemChanged(position);
                        }
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null) {
                    String userKey = snapshot.getKey();
                    if (userKey != null) {
                        keyToUserMaster.remove(userKey);
                        int position = keyList.indexOf(userKey);
                        if (position != -1) {
                            keyList.remove(position);
                            keyToUser.remove(userKey);
                            notifyItemRemoved(position);
                        }
                    }
                }
            }

            @Override public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void setLocationListener() {
        /*if (locationListener == null) {
            locationListener = new GeoQueryDataEventListener() {

                @Override
                public void onDataEntered(DataSnapshot dataSnapshot, GeoLocation location) {
                    if (passesOtherFilters() && userDistance == Filters.UserDistance.NEARBY) {
                        final String userKey = dataSnapshot.getKey();
                        if (keyToUserMaster.containsKey(userKey) || currentUser.getUid().equals(userKey)) return;
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
                                keyList.add(userKey);
                                keyToUser.put(userKey, userModel);
                                keyToUserMaster.put(userKey, userModel);
                                notifyItemInserted(keyList.size() - 1);
                                //recyclerView.scrollToPosition(keyList.size() - 1);
                            }
                            @Override public void onCancelled(@NonNull DatabaseError error) { }
                        });
                    }
                }

                @Override
                public void onDataExited(DataSnapshot dataSnapshot) {
                    if (passesOtherFilters()  && userDistance == Filters.UserDistance.NEARBY) {
                        final String userKey = dataSnapshot.getKey();
                        int position = keyList.indexOf(userKey);
                        keyList.remove(position);
                        keyToUser.remove(userKey);
                        keyToUserMaster.remove(userKey);
                        notifyItemRemoved(position);
                        //recyclerView.scrollToPosition(position);
                    }
                }

                @Override public void onDataChanged(DataSnapshot dataSnapshot, GeoLocation location) { }
                @Override public void onDataMoved(DataSnapshot dataSnapshot, GeoLocation location) { }
                @Override public void onGeoQueryReady() { }
                @Override public void onGeoQueryError(DatabaseError error) { }
            };

            //geoQuery.addGeoQueryDataEventListener(locationListener);
        }*/
    }

    public void setUserType(Filters.UserType userType) {
        this.userType = userType;
        /*if (userType != Filters.UserType.EITHER) {
            for (UserModel user : keyToUserMaster.values()) {
                if ((userType == Filters.UserType.DOG_WALKERS && !user.isDogWalker())
                        || (userType == Filters.UserType.DOG_OWNERS && !user.isDogOwner())) {
                    int position = keyList.indexOf(user.getUserId());
                    keyList.remove(position);
                    keyToUser.remove(user.getUserId());
                    keyToUserMaster.remove(user.getUserId());
                    notifyItemRemoved(position);
                    //recyclerView.scrollToPosition(position);
                }
            }
        }
        // TODO: query database to add items based on change
        if (userType == Filters.UserType.DOG_WALKERS) {
            Query query = database.getReference("Users").orderByChild("dogWalker").startAt(true);
            ChildEventListener queryListener = new ChildEventListener() {
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
            };
            query.addChildEventListener(queryListener);

        } else if (userType == Filters.UserType.DOG_OWNERS) {

        } else if (userType == Filters.UserType.EITHER) {

        }*/
    }

    public void setUserDistance(Filters.UserDistance userDistance) {
        this.userDistance = userDistance;
        /*if (geoQuery != null && locationListener != null) {
            if (userDistance == Filters.UserDistance.NEARBY)
                geoQuery.addGeoQueryDataEventListener(locationListener);
            else if (userDistance == Filters.UserDistance.ANYWHERE)
                geoQuery.removeGeoQueryEventListener(locationListener);
        }*/
        // TODO: update list based on change
    }

    public void setActiveUsers(Filters.ActiveUsers activeUsers) {
        this.activeUsers = activeUsers;
        // TODO: update list based on change
    }

    public void setSearchText(String searchText) {
        String lastSearchText = this.searchText;
        this.searchText = searchText;
        // TODO: update list based on change
    }

    // TODO: implement this for each type of filter
    private boolean passesOtherFilters() {
        return true;
    }

    private void queryDatabase() {

    }

    @NonNull @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.card_user_summary, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        final UserModel userModel = keyToUser.get(keyList.get(position));
        String userId = userModel.getUserId();
        DatabaseReference currentUserRef = allUsersRef.child(userId);

        // Reset the ViewHolder
        holder.userPicture.setBackgroundResource(R.drawable.user_default_picture);
        holder.userActive.setVisibility(View.INVISIBLE);
        holder.userName.setText("");
        holder.dogOwner.setVisibility(View.GONE);
        holder.dogWalker.setVisibility(View.GONE);

        holder.userPicture.setOnClickListener(v -> clickListener.onClickProfile(userId));
        holder.requestWalkButton.setOnClickListener(v ->
                clickListener.onClickRequestWalk(userId, userModel.getUserName(), userModel.isDogOwner(), userModel.isDogWalker()));

        if (holder.userPictureRef != null && holder.userPictureListener != null)
            holder.userPictureRef.removeEventListener(holder.userPictureListener);
        if (holder.ownerActiveRef != null && holder.ownerActiveListener != null)
            holder.ownerActiveRef.removeEventListener(holder.ownerActiveListener);
        if (holder.walkerActiveRef != null && holder.walkerActiveListener != null)
            holder.walkerActiveRef.removeEventListener(holder.walkerActiveListener);
        if (holder.userNameRef != null && holder.userNameListener != null)
            holder.userNameRef.removeEventListener(holder.userNameListener);
        if (holder.dogOwnerRef != null && holder.dogOwnerListener != null)
            holder.dogOwnerRef.removeEventListener(holder.dogOwnerListener);
        if (holder.dogWalkerRef != null && holder.dogWalkerListener != null)
            holder.dogWalkerRef.removeEventListener(holder.dogWalkerListener);

        // TODO: change to use userModel instead of snapshot to update view

        holder.userPictureRef = currentUserRef.child("profilePicture");
        holder.userPictureListener = holder.userPictureRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null)
                    Picasso.get().load(snapshot.getValue().toString()).transform(new CircleTransform()).into(holder.userPicture);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.ownerActiveRef = currentUserRef.child("dogOwnerActive");
        holder.ownerActiveListener = holder.ownerActiveRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null) {
                    if (Boolean.parseBoolean(snapshot.getValue().toString()))
                        holder.userActive.setVisibility(View.VISIBLE);
                    else if (!userModel.isDogWalkerActive())
                        holder.userActive.setVisibility(View.INVISIBLE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.walkerActiveRef = currentUserRef.child("dogWalkerActive");
        holder.walkerActiveListener = holder.walkerActiveRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null) {
                    if (Boolean.parseBoolean(snapshot.getValue().toString()))
                        holder.userActive.setVisibility(View.VISIBLE);
                    else if (!userModel.isDogOwnerActive())
                        holder.userActive.setVisibility(View.INVISIBLE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.userNameRef = currentUserRef.child("profileName");
        holder.userNameListener = holder.userNameRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null)
                    holder.userName.setText(snapshot.getValue().toString());
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.dogOwnerRef = currentUserRef.child("dogOwner");
        holder.dogOwnerListener = holder.dogOwnerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null) {
                    if (Boolean.parseBoolean(snapshot.getValue().toString()))
                        holder.dogOwner.setVisibility(View.VISIBLE);
                    else holder.dogOwner.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });

        holder.dogWalkerRef = currentUserRef.child("dogWalker");
        holder.dogWalkerListener = holder.dogWalkerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot != null && snapshot.getValue() != null) {
                    if (Boolean.parseBoolean(snapshot.getValue().toString()))
                        holder.dogWalker.setVisibility(View.VISIBLE);
                    else holder.dogWalker.setVisibility(View.GONE);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void removeListeners() {
        if (allUsersRef != null && allUsersListener != null)
            allUsersRef.removeEventListener(allUsersListener);
        if (geoQuery != null && locationListener != null && userDistance == Filters.UserDistance.NEARBY)
            geoQuery.removeGeoQueryEventListener(locationListener);
    }

    @Override
    public int getItemCount() { return keyToUser.size(); }

    public static class UserViewHolder extends RecyclerView.ViewHolder {

        public ImageView requestWalkButton;
        public ImageView userPicture;
        public FrameLayout userActive;
        public TextView userName;
        public TextView dogOwner;
        public TextView dogWalker;

        public DatabaseReference userPictureRef;
        public DatabaseReference ownerActiveRef;
        public DatabaseReference walkerActiveRef;
        public DatabaseReference userNameRef;
        public DatabaseReference dogOwnerRef;
        public DatabaseReference dogWalkerRef;

        public ValueEventListener userPictureListener;
        public ValueEventListener ownerActiveListener;
        public ValueEventListener walkerActiveListener;
        public ValueEventListener userNameListener;
        public ValueEventListener dogOwnerListener;
        public ValueEventListener dogWalkerListener;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            userPicture = itemView.findViewById(R.id.user_profile_picture);
            userActive = itemView.findViewById(R.id.active_user);
            requestWalkButton = itemView.findViewById(R.id.request_walk_button);
            userName = itemView.findViewById(R.id.user_name);
            dogOwner = itemView.findViewById(R.id.dog_owner);
            dogWalker = itemView.findViewById(R.id.dog_walker);
        }
    }
}

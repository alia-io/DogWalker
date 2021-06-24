package com.example.dogwalker.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@NoArgsConstructor @Getter @Setter
public class UserModel {
    private String userId;
    private String userName;
    private String profilePicture;
    private boolean dogOwner;
    private boolean dogWalker;
    private boolean dogOwnerActive;
    private boolean dogWalkerActive;
    private List<String> dogs;                  // index : dogId
    private Map<String, String> dogPictures;    // dogId : imageUri

    public UserModel(String userId, String userName, boolean dogOwner,
                     boolean dogWalker, boolean dogOwnerActive, boolean dogWalkerActive) {
        this.userId = userId;
        this.userName = userName;
        this.dogOwner = dogOwner;
        this.dogWalker = dogWalker;
        this.dogOwnerActive = dogOwnerActive;
        this.dogWalkerActive = dogWalkerActive;
        this.dogs = new ArrayList<>();
        this.dogPictures = new HashMap<>();
    }
}

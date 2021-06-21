package com.example.dogwalker.search;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@NoArgsConstructor @Getter @Setter
public class UserModel {
    private String userId;
    private String userName;
    private String profilePicture;
    private List<String> dogs;
    private List<String> dogPictures;
    private boolean dogOwner;
    private boolean dogWalker;
    private boolean dogOwnerActive;
    private boolean dogWalkerActive;

    public UserModel(String userId, String userName, boolean dogOwner, boolean dogWalker, boolean dogOwnerActive, boolean dogWalkerActive) {
        this.userId = userId;
        this.userName = userName;
        this.dogOwner = dogOwner;
        this.dogWalker = dogWalker;
        this.dogOwnerActive = dogOwnerActive;
        this.dogWalkerActive = dogWalkerActive;
        dogs = new ArrayList<>();
        dogPictures = new ArrayList<>();
    }
}

package com.example.dogwalker;

import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import lombok.NoArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter
public class User {

    private String profileName;
    private String phoneNumber;
    private String emailAddress;
    private String profilePicture;
    private String profileAboutMe;
    private Object timestamp;

    private boolean dogOwner;
    private Object dogOwnerExperience;  // Length of time user has owned dogs
    private int numberOfDogs;
    private Map<String, Boolean> dogs = new HashMap<>();

    private boolean dogWalker;
    private Object dogWalkerExperience; // Length of time user has walked dogs
    private int dogWalkerRating;
    private int numberOfRatings;

    private Map<String, String> contacts = new HashMap<>();     // userID : chatID
    private Map<String, String> otherUsers = new HashMap<>();   // userID : chatID
    private String dogWalkingLog;

    private boolean dogOwnerActive;     // Currently looking for walkers
    private boolean dogWalkerActive;    // Currently looking for walks
    private boolean currentWalk;        // Currently on a walk

    public User(String profileName, String phoneNumber, String emailAddress) {
        this.profileName = profileName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.timestamp = ServerValue.TIMESTAMP;
    }
}

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

    private boolean dogOwner = false;
    private Object dogOwnerExperience;  // Length of time user has owned dogs
    private Map<String, Boolean> dogs = new HashMap<>();    // dogID : true

    private boolean dogWalker = false;
    private Object dogWalkerExperience; // Length of time user has walked dogs
    private int dogWalkerRating = 0;
    private int numberOfRatings = 0;

    private Map<String, String> contacts = new HashMap<>();     // userID : chatID
    private Map<String, String> otherUsers = new HashMap<>();   // userID : chatID
    private String dogWalkingLog;

    private boolean dogOwnerActive = false;     // Currently looking for walkers
    private boolean dogWalkerActive = false;    // Currently looking for walks
    private boolean currentWalk = false;        // Currently on a walk

    private String latitude;
    private String longitude;

    public User(String profileName, String phoneNumber, String emailAddress) {
        this.profileName = profileName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.timestamp = ServerValue.TIMESTAMP;
    }
}

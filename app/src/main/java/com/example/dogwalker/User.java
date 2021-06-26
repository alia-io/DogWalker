package com.example.dogwalker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
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
    private Long timestamp;

    private boolean dogOwner = false;
    private Object dogOwnerExperience;  // Length of time user has owned dogs
    private Map<String, Boolean> dogs = new HashMap<>();    // dogID : true

    private boolean dogWalker = false;
    private Object dogWalkerExperience; // Length of time user has walked dogs
    private int dogWalkerRating = 0;
    private int numberOfRatings = 0;

    private Map<String, String> contacts = new HashMap<>();     // userID : chatID
    private Map<String, String> otherUsers = new HashMap<>();   // userID : chatID
    private List<String> dogWalkingLog = new ArrayList<>();     // walkID

    private boolean dogOwnerActive = false;     // Currently looking for walkers
    private boolean dogWalkerActive = false;    // Currently looking for walks
    private String currentWalk = "NONE";        // Currently on a walk

    private Map<String, String> location = new HashMap<>();         // latitude, longitude
    private Map<String, Object> notifications = new HashMap<>();

    public User(String profileName, String phoneNumber, String emailAddress) {
        this.profileName = profileName;
        this.phoneNumber = phoneNumber;
        this.emailAddress = emailAddress;
        this.timestamp = Calendar.getInstance().getTimeInMillis();
    }
}

package com.example.dogwalker;

import com.google.firebase.database.ServerValue;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter
public class ChatMessage {

    private String sender;
    private String receiver;
    private String type;
    private String message;
    private Object timestamp;

    private String owner;
    private String walker;
    private Map<String, String> dogs;   // dogId : dogName
    private String walkTime;
    private float paymentAmount;
    private String currency;

    public ChatMessage(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.type = "message";
        this.message = message;
        this.timestamp = ServerValue.TIMESTAMP;
        dogs = new HashMap<>();
    }
}

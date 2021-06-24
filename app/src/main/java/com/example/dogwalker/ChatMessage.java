package com.example.dogwalker;

import com.google.firebase.database.ServerValue;

import java.util.List;

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
    private List<String> dogs;
    private String walkTime;
    private float paymentAmount;
    private String currency;

    public ChatMessage(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.type = "message";
        this.message = message;
        this.timestamp = ServerValue.TIMESTAMP;
    }
}

package com.example.dogwalker.message;

import com.google.firebase.database.ServerValue;

import java.util.Calendar;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter
public class ChatMessage {

    private String sender;
    private String receiver;
    private String type;
    private String message;
    private Long timestamp;

    public ChatMessage(String sender, String receiver, String message) {
        this.sender = sender;
        this.receiver = receiver;
        this.type = "message";
        this.message = message;
        this.timestamp = Calendar.getInstance().getTimeInMillis();
    }
}

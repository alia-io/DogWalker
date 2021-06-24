package com.example.dogwalker.messaging;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class MessageModel {

    private String messageKey;
    private String messageType;
    private String sender;
    private String receiver;
    private String message;
    private String date;
    private String time;

    public MessageModel(String messageKey, String messageType, String sender, String receiver, String message, String timestamp) {
        this.messageKey = messageKey;
        this.messageType = messageType;
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        int index = timestamp.lastIndexOf(" ");
        this.date = timestamp.substring(0, index);
        this.time = timestamp.substring(index + 1);
    }
}

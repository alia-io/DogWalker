package com.example.dogwalker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class MessageNotification {

    private String messageType;
    private String userId;
    private String userName;
    private String messageId;
    private boolean viewed;

    public MessageNotification(String messageType, String userId, String userName, String messageId) {
        this.messageType = messageType;
        this.userId = userId;
        this.userName = userName;
        this.messageId = messageId;
        this.viewed = false;
    }
}

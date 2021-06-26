package com.example.dogwalker;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class MessageNotification {

    private String notificationType;
    private String userId;
    private String userName;
    private String referenceKey;
    private boolean viewed;

    public MessageNotification(String notificationType, String userId, String userName, String referenceKey) {
        this.notificationType = notificationType;
        this.userId = userId;
        this.userName = userName;
        this.referenceKey = referenceKey;
        this.viewed = false;
    }
}

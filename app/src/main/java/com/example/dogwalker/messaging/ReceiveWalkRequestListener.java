package com.example.dogwalker.messaging;

public interface ReceiveWalkRequestListener {
    void acceptWalkRequest(String notificationKey);
    void declineWalkRequest(String notificationKey);
}

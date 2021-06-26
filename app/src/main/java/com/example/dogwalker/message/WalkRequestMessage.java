package com.example.dogwalker.message;

import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class WalkRequestMessage extends ChatMessage {

    private String owner;
    private String walker;
    private Map<String, String> dogs;   // dogId : dogName
    private Long walkTime;
    private Float paymentAmount;
    private String currency;
    private boolean accepted;
    private boolean declined;

    public WalkRequestMessage(String sender, String receiver, boolean senderIsOwner, Map<String, String> dogs,
                              long walkTime, float paymentAmount, String currency, String message) {
        super(sender, receiver, message);
        if (senderIsOwner) {
            owner = sender;
            walker = receiver;
        } else {
            owner = receiver;
            walker = sender;
        }
        this.dogs = dogs;
        this.walkTime = walkTime;
        this.paymentAmount = paymentAmount;
        this.currency = currency;
        this.accepted = false;
        this.declined = false;
    }
}

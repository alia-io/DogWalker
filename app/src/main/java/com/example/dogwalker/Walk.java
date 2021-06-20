package com.example.dogwalker;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter
public class Walk {
    private String owner;
    private String walker;
    private Object startTime;
    private Object endTime;
    private float paymentAmount;
    private String currency;
    private float rating;
    private Map<String, Boolean> dogs = new HashMap<>();
}

package com.example.dogwalker;

import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @AllArgsConstructor @Getter @Setter
public class Walk {
    private String owner;
    private String walker;
    private Long startTime;
    private Long endTime;
    private Float paymentAmount;
    private String currency;
    private Float rating;
    private Map<String, String> dogs = new HashMap<>(); // dogId : dogName
    private int currentPhase;   // before walk = 0, traveling = 1, walking = 2, wrapping up = 3
}

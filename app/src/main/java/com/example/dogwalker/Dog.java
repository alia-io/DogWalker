package com.example.dogwalker;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor @Getter @Setter
public class Dog {
    private String name;
    private String owner;
    private String breed;
    private String profilePicture;
    private String profileAboutMe;
    private Object birthDate;
    private String averageWalkLength;
    private String trainingLevel;
    private Map<Integer, String> infoAndHealthNeeds = new HashMap<>();
    private Map<Integer, String> walkerRequirements = new HashMap<>();
}

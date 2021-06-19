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
    private String trainingLevel;
    private String averageWalkLength;
    private Map<String, String> infoAndHealthNeeds = new HashMap<>();   // index : text
    private Map<String, String> walkerRequirements = new HashMap<>();   // index : text
}

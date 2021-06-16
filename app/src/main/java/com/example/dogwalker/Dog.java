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
    private String profileAboutMe;
    private Object birthDate;

    private String trainingLevel;
    private Map<String, Boolean> infoAndHealthNeeds = new HashMap<String, Boolean>();

    private int averageWalkLength;
    private String walkLengthUnits;
    private Map<String, Boolean> walkerRequirements = new HashMap<String, Boolean>();

    public Dog(String name, String owner, String breed, Object birthDate) {
        this.name = name;
        this.owner = owner;
        this.breed = breed;
        this.birthDate = birthDate;
    }
}

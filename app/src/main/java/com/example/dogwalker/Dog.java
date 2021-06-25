package com.example.dogwalker;

import java.util.ArrayList;
import java.util.List;

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
    private Long birthDate;
    private String trainingLevel;
    private String averageWalkLength;
    private List<String> infoAndHealthNeeds = new ArrayList<>();
    private List<String> walkerRequirements = new ArrayList<>();
}

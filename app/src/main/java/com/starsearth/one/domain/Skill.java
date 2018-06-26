package com.starsearth.one.domain;

public class Skill {

    public String firstName;
    public String lastName;
    public String email;
    public String skill;

    public Skill() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Skill(String firstName, String lastName, String email, String skill) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.skill = skill;
    }

}

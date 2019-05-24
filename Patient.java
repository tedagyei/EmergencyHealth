package com.newproject.ted.emergencyhealth;

public class Patient {

    public String name;
    public String email;
    public String userId;


    //constructor with no values
    public Patient(){
        // Default constructor required for calls to DataSnapshot.getValue(Patient.class)

    }

    public Patient(String name, String email , String userId){
        this.name = name;
        this.email = email;
        this.userId = userId;


    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

package com.newproject.ted.emergencyhealth;

public class PatientDetails {

    public String bloodtype;
    public String medicalcondition;
    public String allergies;
    public String userId;
    public String phonenumber;
    public String name;
    public String age;
    public String gender;


    //constructor with no values
    public PatientDetails(){
        // Default constructor required for calls to DataSnapshot.getValue(Patient.class)

    }

    public PatientDetails(String name, String bloodtype, String medicalcondition , String allergies,  String userId, String phonenumber, String age,String gender){
        this.bloodtype = bloodtype;
        this.medicalcondition = medicalcondition;
        this.allergies = allergies;
        this.userId = userId;
        this.phonenumber = phonenumber;
        this.name = name;
        this.age = age;
        this.gender = gender;

    }


}

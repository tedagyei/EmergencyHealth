package com.newproject.ted.emergencyhealth;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {


    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;


    private static final String TAG = RegisterActivity.class.getName();
    private AutoCompleteTextView emailview;
    private AutoCompleteTextView nameview;
    private EditText passwordview;
    private EditText confirmpasswordview;
    private EditText conditionsview;
    private EditText allergiesview;
    private EditText bloodtypeview;
    private EditText phoneview;
    private EditText ageview;


    //variables used in newaccounthandler
    String email;
    String password;
    String confirmpassword;
    String name;
    String userId;
    String bloodtype;
    String mediccal_condition;
    String allergies;
    String phonenumber;
    String age;
    String gender;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        emailview = findViewById(R.id.register_email);
        passwordview = findViewById(R.id.register_password);
        confirmpasswordview = findViewById(R.id.confirm_password);
        nameview = findViewById(R.id.register_name);
        conditionsview = findViewById(R.id.medical_conditions);
        allergiesview = findViewById(R.id.allergies);
        bloodtypeview = findViewById(R.id.blood_type);
        phoneview = findViewById(R.id.phone_number);
        ageview = findViewById(R.id.age);


        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();


    }

    public void newaccountClickHandler(View view) {


        //Changing all the values of the views to strings
         email = emailview.getText().toString();
         password = passwordview.getText().toString();
        confirmpassword = confirmpasswordview.getText().toString();
        name = nameview.getText().toString();
        //userId = mAuth.getUid();
        bloodtype  = bloodtypeview.getText().toString();
         mediccal_condition= conditionsview.getText().toString();
         allergies = allergiesview.getText().toString();
         phonenumber =  phoneview.getText().toString();
         age = ageview.getText().toString();

        //Checking to see if  the  name field is empty
        if (TextUtils.isEmpty(name)) {
            nameview.setError("This field is required");

        }

        //Checking to see if  the  email field is empty
        if (TextUtils.isEmpty(email)) {
            emailview.setError("This field is required");

        }

        //Checking to see if  the  password field is empty
        if (TextUtils.isEmpty(password)) {
            passwordview.setError("This field is required");

        }

        //Checking to see if  the  confirmpassword field is empty
        if (TextUtils.isEmpty(confirmpassword)) {
            confirmpasswordview.setError("This field is required");

        }


        //checking if the email is valid
        if (!isEmailValid(email)) {
            emailview.setError("This email is invalid");
        }


        //checking if the passwords match
        if (!confirmPassword(password, confirmpassword)) {
            confirmpasswordview.setError("The passwords do not match");
            passwordview.setError("The passwords do not match");
        }


        //Create a new account and log the patient in
        createAccount(email, password);


        //Working with the patient class
        //Patient patient = new Patient(name, email, userId);

        //putting the values into the database
        //addUser( userId, patient);


        //Adding the patient details
        //PatientDetails details = new PatientDetails(bloodtype,mediccal_condition,allergies,userId);

        //addDetails(userId,details);

    }


    //Method to store the patient's info in the
    private void addUser( String userId, Patient patient) {

        mDatabase.child("users").child("patient").child(userId).setValue(patient);

    }


    private void addDetails(String userId, PatientDetails details){
        mDatabase.child("patientdetails").child(userId).setValue(details);
    }


    public boolean isEmailValid(String email) {
        if (email.contains("@")) {
            return true;
        } else {
            return false;
        }

    }

    public boolean confirmPassword(String password, String passwordconfirm) {

        if (password.equals(passwordconfirm))
            return true;
        else {
            return false;
        }

    }


    //method to create an account
    private void createAccount(final String email, String password) {
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {

            Toast.makeText(getApplicationContext(), "Please Fill all fields", Toast.LENGTH_SHORT).show();
        } else {

            //Creating an account for the patient
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        //Sign in success, updateUI with the signed-in patient's information
                        Log.e(TAG, "createUserWithEmail:success");
                        FirebaseUser user = mAuth.getCurrentUser();

                        //creating an account and putting the values in the database.
                        userId = user.getUid();
                        //Working with the patient class
                        Patient patient = new Patient(name, email, userId);

                        //putting the values into the database
                        addUser( userId, patient);

                        //Adding the patient details
                        PatientDetails details = new PatientDetails(name, bloodtype,mediccal_condition,allergies,userId,phonenumber,age,gender);

                        addDetails(userId,details);




                        updateUI(user);
                    } else {
                        //If sign in fails, display a message to the patient
                        Log.e(TAG, "createUserWithEmail:failure", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                        updateUI(null);
                    }
                }

            });

        }

    }


    //method from starkoverflow
    private void updateUI(FirebaseUser user) {

        if (user != null) {
            Intent intent = new Intent(this, MapsActivity.class);
            startActivity(intent);


            Toast.makeText(RegisterActivity.this, "The patient is signed in", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RegisterActivity.this, "The patient is not signed in ", Toast.LENGTH_SHORT).show();
        }

    }


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.radiomale:
                if (checked)
                    // Pirates are the best
                    gender = "Male";
                    break;
            case R.id.radiofemale:
                if (checked)
                    gender = "Female";
                    break;
        }
    }



}

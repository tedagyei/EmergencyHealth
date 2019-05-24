package com.newproject.ted.emergencyhealth;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private DatabaseReference mCustomerDatabase;

    private String userID;

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
    String medical_condition;
    String allergies;
    String phonenumber;
    String age;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);



        nameview = findViewById(R.id.newregister_name);
        conditionsview = findViewById(R.id.newmedical_conditions);
        allergiesview = findViewById(R.id.newallergies);
        bloodtypeview = findViewById(R.id.newblood_type);
        phoneview = findViewById(R.id.newphone_number);
        ageview = findViewById(R.id.newage);


        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        getUserInfo();



    }

    private void getUserInfo(){
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("patientdetails").child(userID);
        mCustomerDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0){
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if(map.get("name")!=null){
                        name = map.get("name").toString();
                        nameview.setText(name);
                    }
                    if(map.get("phonenumber")!=null){
                        phonenumber = map.get("phonenumber").toString();
                        phoneview.setText(phonenumber);
                    }
                    if(map.get("age")!=null){
                        age = map.get("age").toString();
                        ageview.setText(age);
                    }
                    if(map.get("bloodtype")!=null){
                        bloodtype = map.get("bloodtype").toString();
                        bloodtypeview.setText(bloodtype);
                    }
                    if(map.get("medicalcondition")!=null){
                        medical_condition = map.get("medicalcondition").toString();
                        conditionsview.setText(medical_condition);
                    }

                    if(map.get("allergies")!=null){
                        allergies = map.get("allergies").toString();
                        allergiesview.setText(allergies);
                    }

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private void saveuserinformation(){
        mCustomerDatabase = FirebaseDatabase.getInstance().getReference().child("patientdetails").child(userID);


        if(nameview != null ) {
            name= nameview.getText().toString();
        }
        if(bloodtypeview != null ) {
            bloodtype= bloodtypeview.getText().toString();
        }
        if(conditionsview != null ) {
            medical_condition= conditionsview.getText().toString();
        }
        if(allergiesview != null ) {
            allergies= allergiesview.getText().toString();
        }
        if(phoneview != null ) {
            phonenumber= phoneview.getText().toString();
        }
        if(ageview != null){
            age= ageview.getText().toString();
        }


        Map userInfo = new HashMap();

        if(name!= null) {
            userInfo.put("name", name);
            mCustomerDatabase.updateChildren(userInfo);
        }

        if(phonenumber!= null) {
            userInfo.put("phonenumber", phonenumber);
            mCustomerDatabase.updateChildren(userInfo);
        }
        if(allergies!= null) {
            userInfo.put("allergies", allergies);
            mCustomerDatabase.updateChildren(userInfo);
        }
        if(medical_condition!= null) {
            userInfo.put("medicalcondition", medical_condition);
            mCustomerDatabase.updateChildren(userInfo);
        }
        if(bloodtype!= null) {
            userInfo.put("bloodtype", bloodtype);
            mCustomerDatabase.updateChildren(userInfo);
        }

        if (age!= null){
            userInfo.put("age", age);
            mCustomerDatabase.updateChildren(userInfo);
        }




    }

    public void updateClickHandler(View view) {
        saveuserinformation();
        Toast.makeText(ProfileActivity.this,"Your profile has been updated",Toast.LENGTH_LONG).show();

    }
}

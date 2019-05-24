package com.newproject.ted.emergencyhealth;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;


public class LoginActivity extends AppCompatActivity {

private FirebaseAuth mAuth;

private static String TAG = LoginActivity.class.getName();

    private AutoCompleteTextView emailview;
    private EditText passwordview;
    private ProgressBar spinner;
    private View mLoginFormView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        spinner = (ProgressBar)findViewById(R.id.login_progress);
        spinner.setVisibility(View.GONE);

        emailview = findViewById(R.id.email);
        passwordview = findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
    }


    public void SigninOnClickHandler(View view) {

            String email = emailview.getText().toString();
            String password = passwordview.getText().toString();
            validateForm(email, password);
            login(email, password);

    }

    private void login(String email, String password) {

        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            Toast.makeText(getApplicationContext(),"Please Fill all fields",Toast.LENGTH_SHORT).show();
        }else {
            mLoginFormView.setVisibility(View.GONE);
            spinner.setVisibility(View.VISIBLE);
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {


                        Log.e(TAG, "signIn: Success!");

                        //Update UI with the information of the current patient
                        FirebaseUser user = mAuth.getCurrentUser();
                        updateUI(user);
                    } else {
                        Log.e(TAG, "signIn: Failed", task.getException());
                        Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_SHORT).show();

                        updateUI(null);

                    }

                }
            });

        }


    }


    private void validateForm(String email, String password){
        if(!email.contains("@")){
            emailview.setError("Enter correct email address");

        }


        if(TextUtils.isEmpty(email)){
            emailview.setError("Enter email address");

        }
        if(TextUtils.isEmpty(password)){
            passwordview.setError("Enter password");

        }



    }




    private void updateUI(FirebaseUser user){

        //Testing to see if the homepage activity works
        if (user != null) {

            Intent intent = new Intent(this,MapsActivity.class);
            startActivity(intent);



            Toast.makeText(LoginActivity.this, "The patient is signed in",Toast.LENGTH_SHORT).show();
        } else {

            mLoginFormView.setVisibility(View.VISIBLE);
            spinner.setVisibility(View.GONE);

            Toast.makeText(LoginActivity.this,"The patient is not signed in ",Toast.LENGTH_SHORT).show();
        }

    }






}


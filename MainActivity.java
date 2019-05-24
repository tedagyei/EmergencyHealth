package com.newproject.ted.emergencyhealth;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class MainActivity extends AppCompatActivity {


    private LocationRequest locationRequest;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int REQUEST_CHECK_SETTINGS = 600;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference myref = database.getReference("patient");

    //Testing to see where to put this code
   // LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(location);

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        enablegps();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        }



    public void loginClickHandler(View view) {
        Intent intent = new Intent(this,LoginActivity.class);
        startActivity(intent);
    }

    public void resiterClickHandler(View view) {
        Intent intent = new Intent(this,RegisterActivity.class);
        startActivity(intent);
    }



     protected void requestLocation() {
        locationRequest = LocationRequest.create();
         locationRequest.setInterval(1000);
         locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
         locationRequest.setFastestInterval(5000);


     }



    //checking whether the GPS has been enabled
    public void enablegps(){

        requestLocation();
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task <LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //Toast.makeText(MainActivity.this, "Location is enabled", Toast.LENGTH_SHORT).show();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if(e instanceof ResolvableApiException){
                    //Prompt the user to turn on gps
                    try{
                        //Use startResolutionForResult() to show the user a dialogue
                        //Use onActivityResult() to check the answer to the dialogue

                        ResolvableApiException resolve = (ResolvableApiException) e;
                        resolve.startResolutionForResult(MainActivity.this,REQUEST_CHECK_SETTINGS);

                    }catch(IntentSender.SendIntentException p ){
                        //ignore the exception

                    }

                }

            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch(requestCode){
            case REQUEST_CHECK_SETTINGS:
                switch(resultCode){
                    case MainActivity.RESULT_OK:
                        Toast.makeText(MainActivity.this, "Location is enabled", Toast.LENGTH_SHORT).show();
                    break;
                    case MainActivity.RESULT_CANCELED:
                        Toast.makeText(MainActivity.this, "Location has not been enabled", Toast.LENGTH_SHORT).show();
                    break;

                    default:
                        break;
                }
                break;

        }
    }
}



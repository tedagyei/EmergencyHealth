package com.newproject.ted.emergencyhealth;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private String globalvariable;
    ValueEventListener listener;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference driverlocation;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleMap mMap;
    private boolean mLocationPermissionGranted;
    private Location mLastKnownLocation;
    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //variables to test reading from the database
    //Variable to store username
    private String username;
    public Patient patient;
    private double latitude;
    private double longitude;
    private String userId;
    private String status;

    //Variables to be used to find driver
    private String Driverid;

    private LatLng patientlocation;

    private Button requestbutton;

    private TextView notify;

    private CardView patientnotify;

    private TextView driverdetail;
    private LocationRequest locationRequest;

    private static final int DEFAULT_ZOOM = 15;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    private CameraPosition mCameraPosition;


    private static final String TAG = MapsActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }
        setContentView(R.layout.activity_maps);



        //connecting to the firebase database
        mDatabase = FirebaseDatabase.getInstance().getReference();


        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        //Request button
        requestbutton = findViewById(R.id.request_button);

        //Textview to keep patient updated when he or she makes a request
        notify = findViewById(R.id.patient_text);

        //Textview to show driver details
        driverdetail = findViewById(R.id.driver_number);

        //Cardview to show the patient updated information
        patientnotify = findViewById(R.id.card);


        //hiding the card view
        patientnotify.setVisibility(View.GONE);


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        requestLocation();


        /*
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }



        mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);




        if (mFusedLocationProviderClient != null) {
            mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }

        */


        // Prompt the patient for permission.
        getLocationPermission();

        //Get the current location of the device and set the position of the map.
        getDeviceLocation();


        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();


        //if (mFusedLocationProviderClient != null) {
          //  mFusedLocationProviderClient.removeLocationUpdates(mLocationCallback);
        //}





    }

    LocationCallback mLocationCallback = new LocationCallback(){
        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }

            for(Location location : locationResult.getLocations()){
                if(location !=null){
                    mLastKnownLocation = location;

                    LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                   // mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));


                }
            }
        }
    };


    protected void requestLocation() {
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setFastestInterval(5000);


    }


    //Method to get permission from the patient to access location
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

    }


    //Method to handle the result of the permission request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permission[], @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                //If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
            updateLocationUI();
        }
    }

    public void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }

        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            //Set the map's camera position to the current location of the device
                            mLastKnownLocation = task.getResult();
                            //Just added this code. comment out if it does not work
                            if(mLastKnownLocation == null){
                                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, Looper.myLooper());
                            }else {


                                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults");
                            Log.e(TAG, "Exception: &s,", task.getException());
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        } catch (NullPointerException e) {
            Log.e("Exception", e.getMessage());

        }


    }

    //Method to store the location of the patient in the database
    private void writeDatabase(String userId, Userlocation userlocation) {
        mDatabase.child("patientrequest").child(userId).setValue(userlocation);

    }


    private void writeRedundant(String userId, Userlocation userlocation) {
        mDatabase.child("extrapatientrequest").child(userId).setValue(userlocation);

    }

    //For now I want to just store the patient location in the database
    public void requestAmbulanceClickHandler(View view) {
        try {


            //making the card view visible
            patientnotify.setVisibility(View.VISIBLE);
            //showing informatation in the textview
            notify.setText(R.string.getdriver);

            //making the driverdetail textview invisible
            driverdetail.setVisibility(View.GONE);

            //making the request button invisible
            requestbutton.setVisibility(View.GONE);

            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            //getting the userid
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            longitude = mLastKnownLocation.getLongitude();
            latitude = mLastKnownLocation.getLatitude();
            patientlocation = new LatLng(latitude, longitude);
            status = "pending";

            //Storing the location of the patient in the database
            Userlocation userlocation = new Userlocation(latitude, longitude, status);
            writeDatabase(userId, userlocation);
            writeRedundant(userId, userlocation);






            //Try to create a redundant userlocation that you can delete when the driver accepts a request
            //RedundantUserlocation redundantuserlocation = new RedundantUserlocation(latitude,longitude,username);
            //writeRedundant(userId,redundantuserlocation);


            //commenting this code
            //mMap.addMarker(new MarkerOptions().position(patientlocation).title("My Location"));


           // checkrequest();

            Log.d("WTF", "This is the globalvariabe outside ondatachange " + globalvariable);


        } catch (NullPointerException e) {
            Log.e(TAG, "Displayname could not be shown");
        }


        closestdriverhospital();
    }


    //Checking to see if driver has accepted a request
    private void checkrequest() {

        final String value = "accepted";
        DatabaseReference request = mDatabase.child("patientrequest").child(userId);
        request.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {

                    // globalvariable = dataSnapshot.child("status").getValue().toString();
                    //Log.d("WTF", "This is the globalvariabe in ondatachange " + globalvariable);

                    String newstatus = dataSnapshot.child("status").getValue().toString();

                    if (value.equals(newstatus)) {
                        notify.setText("A driver has accepted your request");
                        //findDriver();
                        //getdriverinfo();
                       // findClosestDriver(closestdriverid);
                        getdclosestriverinfo(closestdriverid);
                        getdriverlivelocation(closestdriverid);
                    } else {
                        //assignDriver();
                        assignPatientToClosestDriver(closestdriverid);

                    }

                }

                //remove value eventlistener

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    //Testing to see if I can get marker for driver
    private LatLng DriverLocation;
    private double newlatitude;
    private double newlongitude;


    //original find driver function
    private void findDriver() {
        DatabaseReference ref = mDatabase.child("driversavailable");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot test : dataSnapshot.getChildren()) {
                    Driverid = test.getKey();
                    newlatitude = (double) test.child("latitude").getValue();
                    newlongitude = (double) test.child("longitude").getValue();


                }

                //commenting this code out to see if the driver accepting a request is shown to the patient
                //if(!(Driverid.isEmpty())){
                //    notify.setText("A Driver has been found and is on his way to pick you up.");
                //}
                //Code to test if the driver's position appears
                DriverLocation = new LatLng(newlatitude, newlongitude);

                mMap.addMarker(new MarkerOptions().position(DriverLocation).title("Driver Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.ambulancemarker)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DriverLocation, DEFAULT_ZOOM));

                Log.d("WTF", "This is the driverid " + Driverid);
                //THis is to add patient id to the driver who has been selected
                //assignPatientToDriver();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    //This code will become redundant
    //A copy of the findriver function
    private void assignDriver() {
        DatabaseReference ref = mDatabase.child("driversavailable");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot test : dataSnapshot.getChildren()) {
                    Driverid = test.getKey();
                    newlatitude = (double) test.child("latitude").getValue();
                    newlongitude = (double) test.child("longitude").getValue();


                }

                //commenting this code out to see if the driver accepting a request is shown to the patient
                //if(!(Driverid.isEmpty())){
                //    notify.setText("A Driver has been found and is on his way to pick you up.");
                //}
                //Code to test if the driver's position appears
                //DriverLocation = new LatLng(newlatitude, newlongitude);

                //mMap.addMarker(new MarkerOptions().position(DriverLocation).title("Driver Location"));
                //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DriverLocation,DEFAULT_ZOOM));

                Log.d("WTF", "This is the driverid " + Driverid);
                //THis is to add patient id to the driver who has been selected
                assignPatientToDriver();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    //Variables to store the driver details
    private String drivername;
    private String drivernumber;

    //Get the details of the driver
    private void getdriverinfo() {

        DatabaseReference driverinfo = mDatabase.child("users").child("driver");
        driverinfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String key = FirebaseAuth.getInstance().getCurrentUser().getUid();
                if (dataSnapshot.exists()) {
                    for (DataSnapshot test : dataSnapshot.getChildren()) {
                        String testkey = test.child("patientid").getValue().toString();
                        if (testkey.equals(key)) {
                            drivername = test.child("name").getValue().toString();
                            drivernumber = test.child("phonenumber").getValue().toString();
                            driverdetail.setVisibility(View.VISIBLE);
                            notify.setText("Driver name: " + drivername);
                            driverdetail.setText("Phone number: " + drivernumber);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //String to compare driverid
    private String drivercompare;

    public void assignPatientToDriver() {

        //Log.d("WTF", "THe inner part of the assignPatientToDriver is working ");
        //adding a listener for a singlevalue event instead of addvalueeventlistener

        //listener just added
        final DatabaseReference driverref = mDatabase.child("users").child("driver");
         driverref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot test : dataSnapshot.getChildren()) {
                    Log.d("WTF", "THe inner part of the assignPatientToDriver is working ");
                    drivercompare = test.getKey();

                    if (Driverid.equals(drivercompare)) {
                        //Creating a database reference to point to the current driver
                        DatabaseReference testdriver = mDatabase.child("users").child("driver").child(drivercompare);


                        //Create a hasmap
                        Map<String, Object> map = new HashMap<>();
                        map.put("patientid", userId);

                        //putting the value of the HashMap into the database
                        testdriver.updateChildren(map);

                        //Debugging
                        Log.d("WTF", "THe deep part of the assignPatientToDriver is working ");
                        Log.d("WTF", "Driverid " + Driverid);
                        Log.d("WTF", "Current driver's id " + drivercompare);
                    }
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void LogoutOnClickHandler(View view) {
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void SettingsOnClickHandler(View view) {

        Intent intent = new Intent(this, ProfileActivity.class);
        startActivity(intent);
    }

    String closestdriverid;

    String compareid;
    double comparelatitude;
    double comparelongitude;
    String testid;
    float distance;

    //Trying to get the closest driver
    private void closestdriverhospital() {
        //Compare the driver location to the mlastknownlocation(Patient's location)
        testid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        Log.d("MTN", "closestdriverhospital ");
        Location loc2;


        DatabaseReference driverhospital = mDatabase.child("driverhospitallocation");
        driverhospital.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map<String, Float> map = new HashMap<>();
                for (DataSnapshot test : dataSnapshot.getChildren()) {
                    compareid = test.getKey();
                    comparelatitude = (double) test.child("latitude").getValue();
                    comparelongitude = (double) test.child("longitude").getValue();


                    Location loc2 = new Location("");
                    loc2.setLatitude(comparelatitude);
                    loc2.setLongitude(comparelongitude);

                    distance = mLastKnownLocation.distanceTo(loc2);
                    Log.d("WTF", "This is a location of one driver: " + String.valueOf(distance));


                    map.put(compareid, distance);


                }
                Map<String, Float> newmap = sortByValues((HashMap) map);

                //Testing the value of the driver id
                 closestdriverid = (String) newmap.keySet().toArray()[0];

                checkrequest();



                Log.d("WTF", "THe closestdriver id is  " + closestdriverid);

                //Testing to see if the map has been sorted
                Set set = newmap.entrySet();
                Iterator iterator = set.iterator();
                while (iterator.hasNext()) {
                    Map.Entry me = (Map.Entry) iterator.next();
                    Log.d("WTF", me.getKey().toString() + " ");
                    Log.d("WTF", me.getValue().toString() + " ");
                }

                //float  newkey = newmap.get(newmap.keySet().toArray()[0]);


            }


            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    //Function to sort the map
    // public Map<String,Float> sortmap(Map<String,Float> themap){
    //   LinkedHashMap<String,Float> sortedmap = new LinkedHashMap<>();
    //}

    //Sorting the map
    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }


    //Closest driver functions
    public void assignPatientToClosestDriver(final String driverid) {

        //Log.d("WTF", "THe inner part of the assignPatientToDriver is working ");
        final String patientrequest="requestmade";

        final DatabaseReference driverref = mDatabase.child("users").child("driver");
        listener = driverref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot test : dataSnapshot.getChildren()) {
                    Log.d("WTF", "THe inner part of the assignPatientToDriver is working ");
                    drivercompare = test.getKey();

                    if (driverid.equals(drivercompare)) {
                        //Creating a database reference to point to the current driver
                        DatabaseReference testdriver = mDatabase.child("users").child("driver").child(drivercompare);


                        //Create a hasmap
                        Map<String, Object> map = new HashMap<>();
                        map.put("patientid", userId);
                        map.put("patientrequest",patientrequest);

                        //putting the value of the HashMap into the database
                        testdriver.updateChildren(map);

                        //Debugging
                        Log.d("WTF", "THe deep part of the assignPatientToDriver is working ");
                        Log.d("WTF", "Driverid " + Driverid);
                        Log.d("WTF", "Current driver's id " + drivercompare);
                    }
                }

                driverref.removeEventListener(listener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    //Closest driver functions
    private void findClosestDriver(final String driverid) {

        DatabaseReference ref = mDatabase.child("driverhospitallocation");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot test : dataSnapshot.getChildren()) {
                    Driverid = test.getKey();

                    if (driverid.equals(Driverid)) {
                        newlatitude = (double) test.child("latitude").getValue();
                        newlongitude = (double) test.child("longitude").getValue();
                    }

                }

                //commenting this code out to see if the driver accepting a request is shown to the patient
                //if(!(Driverid.isEmpty())){
                //    notify.setText("A Driver has been found and is on his way to pick you up.");
                //}
                //Code to test if the driver's position appears
                DriverLocation = new LatLng(newlatitude, newlongitude);

                mMap.addMarker(new MarkerOptions().position(DriverLocation).title("Driver Location").icon(BitmapDescriptorFactory.fromResource(R.drawable.ambulancemarker)));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DriverLocation, DEFAULT_ZOOM));

                Log.d("WTF", "This is the driverid " + Driverid);
                //THis is to add patient id to the driver who has been selected
                //assignPatientToDriver();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private Marker mDriverMarker;
    private void getdriverlivelocation(final String livedriverid){
    DatabaseReference livelocation = mDatabase.child("currentdriverlocation").child(livedriverid).child("l");
    livelocation.addValueEventListener(new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(dataSnapshot.exists()) {
                List<Object> map = (List<Object>) dataSnapshot.getValue();
                double locationLat = 0;
                double locationLng = 0;
                if (map.get(0) != null) {
                    locationLat = Double.parseDouble(map.get(0).toString());
                }
                if (map.get(1) != null) {
                    locationLng = Double.parseDouble(map.get(1).toString());
                }
                LatLng driverLatLng = new LatLng(locationLat, locationLng);
                if (mDriverMarker != null) {
                    mDriverMarker.remove();
                }
                Location loc1 = new Location("");
                loc1.setLatitude(mLastKnownLocation.getLatitude());
                loc1.setLongitude(mLastKnownLocation.getLongitude());

                Location loc2 = new Location("");
                loc2.setLatitude(driverLatLng.latitude);
                loc2.setLongitude(driverLatLng.longitude);

                float distance = loc1.distanceTo(loc2);

                if (distance < 900) {
                    driverdetail.setText("Ambulance Driver has arrived");
                } else {
                    driverdetail.setText("Driver Found and is  " + String.valueOf(distance) +" meters away ");
                }
                mDriverMarker = mMap.addMarker(new MarkerOptions().position(driverLatLng).title("your driver").icon(BitmapDescriptorFactory.fromResource(R.drawable.ambulancemarker)));
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    });


    }


    private void getdclosestriverinfo(final String driverid) {

        DatabaseReference driverinfo = mDatabase.child("users").child("driver");
        driverinfo.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    for (DataSnapshot test : dataSnapshot.getChildren()) {
                        String testkey = test.getKey();
                        if (testkey.equals(driverid)) {
                            drivername = test.child("name").getValue().toString();
                            drivernumber = test.child("phonenumber").getValue().toString();
                            driverdetail.setVisibility(View.VISIBLE);
                            notify.setText("Driver name: " + drivername);
                            driverdetail.setText("Phone number: " + drivernumber);
                        }

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

//get current token from the app


}
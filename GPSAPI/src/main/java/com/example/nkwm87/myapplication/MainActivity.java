package com.example.nkwm87.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;
//import android.widget.Toast;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;

public class MainActivity extends AppCompatActivity implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "Location Activity";
    private static final long INTERVAL = 1000 * 10;
    private static final long FASTEST_INTERVAL = 1000 * 5;
    LocationRequest mLocationRequest;
    GoogleApiClient mGoogleApiClient;
    Location mCurrentLocation;
    private Runnable finalUpdater;
    private Handler timerHandler;

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /** Check for the availability of Google Play Services **/
        if (!isGooglePlayServicesAvailable()) {
            Log.d(TAG, "Google PLay Service Not Available");
        }

        Log.d(TAG, "Started.");
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).
                addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();   //adding new API for the location request
        setContentView(R.layout.activity_main);
        Log.d(TAG, "Waiting to be Clicked.");

        Button locationButton = (Button) findViewById(R.id.LocationButton);
        locationButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               final EditText UpdateTime = (EditText) findViewById(R.id.UpdateTime);
               String updateTime = UpdateTime.getText().toString(); //obtain the update time from the user input
               final long time = Long.parseLong(updateTime);
               Log.d(TAG, "Time:" +time);

               /** Adding some delay to the update according to the time input **/
               timerHandler = new Handler();
               finalUpdater = new Runnable() {
                   @Override
                   public void run() {
                       updateUI();
                       Log.d(TAG, "Delayed...");
                       timerHandler.postDelayed(finalUpdater, time*1000);   //update the location every few second depends on the user input
                   }
               };
               timerHandler.post(finalUpdater);

           }
       });

        Button stopButton = (Button) findViewById(R.id.stopButton);
        stopButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                timerHandler.removeCallbacks(finalUpdater); //stop update location
                Log.d(TAG, "Stop Constantly Update UI");
            }
        });

        ImageButton MapButton = (ImageButton)findViewById(R.id.imageButton);
        MapButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Log.d(TAG, "Connecting to Google Map.");
                Intent intent = new Intent(getContext(), MapsActivity.class);

                intent.putExtra("latitude", mCurrentLocation.getLatitude());
                intent.putExtra("longitude", mCurrentLocation.getLongitude());
                startActivity(intent);  //sending the value of longitude and latitude to the MapsActivity
            }
        });
    }

    private Context getContext() {
        return this;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            Log.d(TAG, "New Location..");
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onStop(){
        super.onStop();
        Log.d(TAG,"Stop All Activity...");
        //mGoogleApiClient.disconnect();
        //timerHandler.removeCallbacks(finalUpdater);
    }

    private boolean isGooglePlayServicesAvailable() {
        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int status = apiAvailability.isGooglePlayServicesAvailable(this);
        if (status == ConnectionResult.SUCCESS) {
            Log.d(TAG, "Google Play Services is Available and SUCCESS.");
            return true;

        } else {
            apiAvailability.getErrorDialog(this, status, 0).show();
            return false;
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "Connecting...");
        startLocationUpdates();
    }

    /** Update the location using the GPS of the phone **/
    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            Log.d(TAG, "Permission is not Granted..");
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.
                requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        Log.d(TAG, "Location is Updated...");
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult){}

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        Log.d(TAG, "Location Changed New");

    }

    /** Displaying the info of longitude and latitude of the location **/
    private void updateUI(){
        if (null != mCurrentLocation) {

            String latitude = String.valueOf(mCurrentLocation.getLatitude());
            String longitude = String.valueOf(mCurrentLocation.getLongitude());

            Log.d(TAG, "Showing Coordinate");
            TextView coordinate = (TextView) findViewById(R.id.coordinate);
            //Toast.makeText(this, "Latitude: \t" +latitude+ "\nLongitude: \t" + longitude, Toast.LENGTH_LONG).show();
            coordinate.setText("Latitude:" + latitude + "\nLongitude:" + longitude);

        } else {
            Log.d(TAG, "Location is null.....");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

}

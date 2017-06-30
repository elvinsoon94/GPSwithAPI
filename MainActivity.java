package com.example.nkwm87.myapplication;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Button;
import android.view.View;

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


    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isGooglePlayServicesAvailable()) {

            Log.d(TAG, "Google PLay Service Not Available");
        } else
            Log.d(TAG, "Started.");
            createLocationRequest();
            mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
            setContentView(R.layout.activity_main);
            Button locationButton = (Button) findViewById(R.id.LocationButton);
            locationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUI();
                }
            });
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
        mGoogleApiClient.disconnect();
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

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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

    private void updateUI(){
        if (null != mCurrentLocation) {
            String latitude = String.valueOf(mCurrentLocation.getLatitude());
            String longitude = String.valueOf(mCurrentLocation.getLongitude());

            Log.d(TAG, "Showing Coordinate");

            TextView coordinate = (TextView) findViewById(R.id.coordinate);
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

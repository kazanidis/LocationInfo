package gr.ikazanidis.locationinfo;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.Locale;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private String TAG = "Test";

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    protected boolean mRequestingLocationUpdates;
    protected LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;
    private TextView tv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get permissions
        permissionsRequest();

        tv = findViewById(R.id.latlon);
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update UI with location data
                    mLastLocation = location;
                    tv.setText(String.format(Locale.getDefault(), "%f,%f", mLastLocation.getLatitude(), mLastLocation.getLongitude()));
                }
            };
        };


        //Location
        mRequestingLocationUpdates = true;
        buildGoogleApiClient();
    }

    // Requests app permissions
    public void permissionsRequest() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //request permissions
            ActivityCompat.requestPermissions(this,
                    new String[]{ Manifest.permission.ACCESS_FINE_LOCATION }, 111);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 111: {
                if (grantResults.length > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED ) {
                    //uf permissions granted
                } else {
                    //permissions not granted alert
                    AlertDialog.Builder builder = new AlertDialog.Builder (this);
                    builder.setTitle("Permissions required");
                    builder.setMessage("Permissions required");
                    builder.setNeutralButton("Cancel", new DialogInterface.OnClickListener () {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel();
                            System.exit(1);
                        }
                    });
                    AlertDialog noInternet = builder.create();
                    noInternet.show();
                }
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(this, "Failed to connect!", Toast.LENGTH_SHORT).show();
        Log.e(TAG, "Client NOT connected");

    }

    @Override
    public void onConnected(Bundle arg0) {
        Log.e(TAG, "Client connected");
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }
        else {
            //mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                mLastLocation = location;
                                tv.setText(String.format(Locale.getDefault(), "Last location %f,%f", mLastLocation.getLatitude(), mLastLocation.getLongitude()));

                            }
                        }
                    });

            if (mRequestingLocationUpdates) {
                startLocationUpdates();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Toast.makeText(this, "Connection suspended...", Toast.LENGTH_SHORT).show();

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        createLocationRequest();
    }

    protected void startLocationUpdates() {
        if ( Build.VERSION.SDK_INT >= 23 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
            return  ;
        }
        else {
                        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Log.e(TAG, "onStart");
        //mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    protected void stopLocationUpdates() {
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }


    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();

        super.onStop();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }
}
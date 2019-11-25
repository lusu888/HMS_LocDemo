package com.wz.android.zzlocademo;

import android.Manifest;
import android.app.FragmentTransaction;
import android.app.IntentService;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.core.app.ActivityCompat;

import com.wz.android.zzlocademo.logger.LocationLog;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.common.ResolvableApiException;
import com.huawei.hms.location.FusedLocationProviderClient;
import com.huawei.hms.location.LocationAvailability;
import com.huawei.hms.location.LocationCallback;
import com.huawei.hms.location.LocationRequest;
import com.huawei.hms.location.LocationResult;
import com.huawei.hms.location.LocationServices;
import com.huawei.hms.location.LocationSettingsRequest;
import com.huawei.hms.location.LocationSettingsResponse;
import com.huawei.hms.location.LocationSettingsStatusCodes;
import com.huawei.hms.location.SettingsClient;
import android.view.View.OnClickListener;

import java.util.List;

public class MainActivity extends BaseActivity  implements OnClickListener {

    public static final String TAG = "LocationUpdatesCallback";
    LocationCallback mLocationCallback;
    LocationRequest mLocationRequest;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private SettingsClient settingsClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.findViewById(R.id.location_requestLocationUpdatesWithCallback).setOnClickListener(this);
        this.findViewById(R.id.location_removeLocationUpdatesWithCallback).setOnClickListener(this);

        addLogFragment();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        settingsClient = LocationServices.getSettingsClient(this);
        mLocationRequest = new LocationRequest();

        mLocationRequest.setInterval(10000);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        if (null == mLocationCallback) {
            mLocationCallback = new LocationCallback() {
                @Override
                public void  onLocationResult(LocationResult locationResult){
                    if (locationResult != null) {
                        List<Location> locations = locationResult.getLocations();

                        if(!locations.isEmpty()){
                            for(Location location: locations) {
                                LocationLog.i(TAG,
                                        "onLocationResult location[Longitude,Latitude,Accuracy]:" + location.getLongitude()
                                                + "," + location.getLatitude() + "," + location.getAccuracy()
                                );
                            }
                        }
                    }
                }

                @Override
                public  void onLocationAvailability(LocationAvailability locationAvailability){
                    if (locationAvailability != null){
                        boolean flag = locationAvailability.isLocationAvailable();
                        LocationLog.i(TAG, "onLocationAvailability isLocationAvailable:" + flag);
                    }
                }
            };
        }




        // check location permisiion
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            Log.i(TAG, "sdk < 28 Q");
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                String[] strings =
                        {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
                ActivityCompat.requestPermissions(this, strings, 1);
            }
        } else {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    "android.permission.ACCESS_BACKGROUND_LOCATION") != PackageManager.PERMISSION_GRANTED) {
                String[] strings = {android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        "android.permission.ACCESS_BACKGROUND_LOCATION"};
                ActivityCompat.requestPermissions(this, strings, 2);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_FINE_LOCATION successful");
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_FINE_LOCATION failed");
            }

            if (grantResults.length > 0 && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_COARSE_LOCATION successful");
            } else {
                Log.i(TAG, "onRequestPermissionsResult: apply ACCESS_COARSE_LOCATION failed");
            }
        }
    }

    @Override
    public void onClick(View v) {
        try {
            switch (v.getId()) {
                case R.id.location_requestLocationUpdatesWithCallback:
                    requestLocationUpdatesWithCallback();
                    break;
                case R.id.location_removeLocationUpdatesWithCallback:
                    removeLocationUpdatesWithCallback();
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "RequestLocationUpdatesWithCallbackActivity Exception:" + e);
        }
    }
    @Override
    protected void onDestroy() {
        // don't need to receive callback
        removeLocationUpdatesWithCallback();
        super.onDestroy();
    }


    private  void requestLocationUpdatesWithCallback(){
        try {
            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();

            builder.addLocationRequest(mLocationRequest);
            LocationSettingsRequest locationSettingsRequest = builder.build();

            settingsClient.checkLocationSettings(locationSettingsRequest)
                    .addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
                        @Override
                        public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                            Log.i(TAG,"Check location setting ok");

                            fusedLocationProviderClient
                                    .requestLocationUpdates(mLocationRequest,mLocationCallback, Looper.getMainLooper())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            LocationLog.i(TAG,"requestLocationUpdatesWithCallback OK");
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(Exception e) {
                                            LocationLog.i(TAG,
                                                    "requestLocationUpdatesWithCallback onFailure:" + e.getMessage()
                                            );
                                        }
                                    });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            LocationLog.e(TAG, "checkLocationSetting onFailure" + e.getMessage());

                            int statusCode = ((ApiException)e).getStatusCode();

                            switch (statusCode) {
                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        ResolvableApiException rae =(ResolvableApiException) e;
                                        rae.startResolutionForResult(MainActivity.this, 0);
                                    } catch (IntentSender.SendIntentException sie){
                                        Log.e(TAG,  "PendingIntent unable to execute request.");
                                    }
                                    break;
                            }
                        }
                    });



        } catch (Exception e) {
            LocationLog.e(TAG, "requestLocationUpdatesWithCallback exception:" + e.getMessage());
        }

    }

    private void removeLocationUpdatesWithCallback(){
        try {
            fusedLocationProviderClient.removeLocationUpdates(mLocationCallback)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            LocationLog.i(TAG, "removeLocationUpdatesWithCallback onSuccess");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            LocationLog.e(TAG, "removeLocationUpdatesWithCallback onFailure:" + e.getMessage());
                        }
                    });

        } catch (Exception e) {
            LocationLog.e(TAG, "removeLocationUpdatesWithCallback exception:" + e.getMessage());

        }
    }




}

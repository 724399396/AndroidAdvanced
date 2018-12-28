/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.android.walkmyandroid;

import android.Manifest;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnTaskCompleted {

    private static final int REQUEST_LOCATION_PERMISSIONS = 1;
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String TRACKING_LOCATION_KEY = "tracking location";

    private FusedLocationProviderClient mFusedLocationClient;
    private TextView mLocationTextView;
    private ImageView mAndroidImageView;
    private AnimatorSet mRotateAnim;
    private Button mLocationButton;
    private LocationCallback mLocationCallback;
    private boolean mTrackingLocation = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            mTrackingLocation = savedInstanceState.getBoolean(TRACKING_LOCATION_KEY);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationTextView = findViewById(R.id.textview_location);
        mAndroidImageView = findViewById(R.id.imageview_android);
        mRotateAnim = (AnimatorSet) AnimatorInflater.loadAnimator(this,
                R.animator.rotate);
        mRotateAnim.setTarget(mAndroidImageView);
        mLocationButton = findViewById(R.id.button_location);
        mLocationButton.setOnClickListener(
                new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        if (!mTrackingLocation) {
                            startTrackingLocation();
                        } else {
                            stopTrackingLocation();
                        }
                    }
                }
        );
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (mTrackingLocation)
                    new FetchAddressTask(MainActivity.this, MainActivity.this)
                            .execute(locationResult.getLastLocation());
            }
        };
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTrackingLocation) {
            stopTrackingLocation();
            mTrackingLocation = true;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mTrackingLocation) {
            startTrackingLocation();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(TRACKING_LOCATION_KEY, mTrackingLocation);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSIONS:
                // if the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTrackingLocation();
                } else {
                    Toast.makeText(this,
                            R.string.location_permission_denied,
                            Toast.LENGTH_SHORT).show();
                }
        }
    }

    private void getLastLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS);
        } else {
            mFusedLocationClient.getLastLocation().addOnSuccessListener(
                    new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                new FetchAddressTask(MainActivity.this,
                                        MainActivity.this).execute(location);
                                mLocationTextView.setText(getString(R.string.address_text,
                                        getString(R.string.loading),
                                        System.currentTimeMillis()));
                            } else {
                                mLocationTextView.setText(R.string.no_location);
                            }
                        }
                    }
            );
        }
    }

    private void startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_PERMISSIONS);
        } else {
            mFusedLocationClient.requestLocationUpdates(getLocationRequest(), mLocationCallback,
                    null);
        }
        mRotateAnim.start();
        mTrackingLocation = true;
        mLocationButton.setText(getString(R.string.stop_tracking));
    }

    private void stopTrackingLocation() {
        if (mTrackingLocation) {
            mTrackingLocation = false;
            mLocationButton.setText(R.string.start_tracking_location);
            mLocationTextView.setText(R.string.textview_hint);
            mRotateAnim.end();
            mFusedLocationClient.removeLocationUpdates(mLocationCallback);
        }
    }

    private LocationRequest getLocationRequest() {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    @Override
    public void onTaskCompleted(String result) {
        mLocationTextView.setText(getString(R.string.address_text,
                result, System.currentTimeMillis()));
    }

    private class FetchAddressTask extends AsyncTask<Location, Void, String> {
        private final String TAG = FetchAddressTask.class.getSimpleName();
        private Context mContex;
        private OnTaskCompleted mListener;

        public FetchAddressTask(Context applicationContext, OnTaskCompleted listener) {
            mContex = applicationContext;
            mListener = listener;
        }

        @Override
        protected String doInBackground(Location... locations) {
            Geocoder geocoder = new Geocoder(mContex,
                    Locale.getDefault());
            Location location = locations[0];
            List<Address> addresses = null;
            String resultMessage = "";
            try {
                addresses = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        // IN the sample ,get just a single address
                        1);
                if (addresses == null || addresses.size() == 0) {
                    if (resultMessage.isEmpty()) {
                        resultMessage = mContex
                                .getString(R.string.no_address_found);
                        Log.e(TAG, resultMessage);
                    }
                } else {
                    // If an address is found, read it into resultMessage
                    Address address = addresses.get(0);
                    ArrayList<String> addressParts = new ArrayList<>();
                    // Fetch the address lines using getAddressLine,
                    // join them, and send them to the thread
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        addressParts.add(address.getAddressLine(i));
                    }

                    resultMessage = TextUtils.join("\n", addressParts);
                }
            } catch (IOException e) {
                resultMessage = mContex
                        .getString(R.string.service_not_available);
                Log.e(TAG, resultMessage, e);
            } catch (IllegalArgumentException e) {
                resultMessage = mContex
                        .getString(R.string.invalid_lat_long_used);
                Log.e(TAG, resultMessage + ". " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " + location.getLongitude(), e);
            }
            return resultMessage;
        }

        @Override
        protected void onPostExecute(String address) {
            mListener.onTaskCompleted(address);
            super.onPostExecute(address);
        }

    }

}

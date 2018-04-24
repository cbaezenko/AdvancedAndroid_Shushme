package com.example.android.shushme;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.SyncStateContract;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

public class Geofencing implements ResultCallback<Status> {

    Context mContext;
    GoogleApiClient mGoogleApiClient;
    List<Geofence> mGeofencesList;
    PendingIntent mPendingIntent;

    private static final String TAG = "Geofencing";

    public Geofencing(Context context, GoogleApiClient googleApiClient) {
        mContext = context;
        mGoogleApiClient = googleApiClient;
        mGeofencesList = new ArrayList<>();
    }

    public void updateGeofencesList(PlaceBuffer placeBuffer) {
        if (placeBuffer == null || placeBuffer.getCount() == 0) return;
        for (int i = 0; i < placeBuffer.getCount(); i++) {
            String id = placeBuffer.get(i).getId();
            double latitude = placeBuffer.get(i).getLatLng().latitude;
            double longitude = placeBuffer.get(i).getLatLng().longitude;

            Geofence geofence = new Geofence.Builder().setRequestId(id)
                    .setCircularRegion(latitude, longitude, 50)
                    .setExpirationDuration(100000L)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeofencesList.add(geofence);
        }
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .addGeofences(mGeofencesList)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
        return geofencingRequest;
    }

    private PendingIntent getGeofencePendingIntent() {

        if (mPendingIntent != null) return mPendingIntent;
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        return mPendingIntent = PendingIntent.getBroadcast(
                mContext,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void registerAllGeofences() {

        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected() ||
                mGeofencesList == null || mGeofencesList.size() == 0)
            return;
        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(mGoogleApiClient, getGeofencingRequest(), getGeofencePendingIntent())
                    .setResultCallback(this);
        }catch (SecurityException securityException){
            Log.e(TAG, securityException.getMessage());
        }

    }

    public void unRegisterAllGeofences(){
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected()) return;
        else {
         try {
             LocationServices.GeofencingApi.removeGeofences(mGoogleApiClient,
                     getGeofencePendingIntent());
         } catch (SecurityException securityExeption){
             Log.e(TAG, securityExeption.getMessage());
         }

        }
        }

    @Override
    public void onResult(@NonNull Status status) {
    Log.e(TAG, "error adding/removing geofence "+status.getStatus().toString());
    }
}

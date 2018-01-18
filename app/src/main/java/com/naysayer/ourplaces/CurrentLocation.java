package com.naysayer.ourplaces;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

class CurrentLocation {
    private static final int REQUEST_LOCATION = 1;
    private LatLng mLatLng;

    CurrentLocation() {
    }

    LatLng getCurrentLocation(Context context, Activity activity) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        assert locationManager != null;

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps(context, activity);
        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            } else {
                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

                double lat;
                double lng;
                if (location != null) {
                    lat = location.getLatitude();
                    lng = location.getLongitude();
                    mLatLng = new LatLng(lat, lng);

                } else if (location1 != null) {
                    lat = location1.getLatitude();
                    lng = location1.getLongitude();
                    mLatLng = new LatLng(lat, lng);

                } else if (location2 != null) {
                    lat = location2.getLatitude();
                    lng = location2.getLongitude();
                    mLatLng = new LatLng(lat, lng);

                } else {
                    Toast.makeText(context, "Unable trace your location", Toast.LENGTH_SHORT).show();
                }
            }
        }
        return mLatLng;
    }

    //todo не показывает диалог, если отключено местоположение
    private void buildAlertMessageNoGps(final Context context, final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(R.string.alert_dialog_no_gps)
                .setCancelable(false)
                .setPositiveButton(R.string.turn_on, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        activity.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }
}

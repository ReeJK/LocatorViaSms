package com.wnezros.locatorviasms;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;

import java.util.Date;
import java.util.Locale;

public abstract class LocationSender {
    protected final Context _context;
    private final Handler _handler = new Handler();

    public LocationSender(Context context) {
        if(context == null)
            throw new IllegalArgumentException("context is null");
        _context = context;
    }

    protected abstract void sendMessage(String message);

    public void requestLocation() {
        if (ContextCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(_context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            sendMessage(R.string.sms_no_permissions);
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
        LocationManager locationManager = (LocationManager) _context.getSystemService(Context.LOCATION_SERVICE);

        if(!requestGpsLocation(locationManager, prefs))
            if(!requestNetworkLocation(locationManager, prefs))
                if(!sendCellsMessage(prefs))
                    requestLastLocation(locationManager, prefs);
    }

    protected boolean requestGpsLocation(final LocationManager locationManager, final SharedPreferences prefs) throws SecurityException {
        if(!Settings.getUseGps(prefs))
            return false;
        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            return false;

        final SmsLocationListener listener = new SmsLocationListener(LocationType.GPS);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() throws SecurityException {
                locationManager.removeUpdates(listener);
                if(!requestNetworkLocation(locationManager, prefs))
                    requestLastLocation(locationManager, prefs);
            }
        };

        listener.setRunnable(runnable);
        locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, listener, null);

        int timeout = Settings.getGpsTimeout(prefs) * 1000;
        _handler.postDelayed(runnable, timeout);
        return true;
    }

    protected boolean requestNetworkLocation(final LocationManager locationManager, final SharedPreferences prefs) throws SecurityException {
        if(!locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER))
            return false;

        final SmsLocationListener listener = new SmsLocationListener(LocationType.Network);
        final Runnable runnable = new Runnable() {
            @Override
            public void run() throws SecurityException {
                locationManager.removeUpdates(listener);
                requestLastLocation(locationManager, prefs);
            }
        };

        listener.setRunnable(runnable);
        locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, listener, null);

        int timeout = Settings.getLocationTimeout(prefs) * 1000;
        _handler.postDelayed(runnable, timeout);
        return true;
    }

    protected void requestLastLocation(LocationManager locationManager, SharedPreferences prefs) throws SecurityException {
        Location location = null;
        LocationType type = LocationType.GPS;
        if(Settings.getUseGps(prefs))
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if(location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            type = LocationType.Network;
        }

        if(location == null)
            sendMessage(R.string.sms_location_not_found);
        else
            sendMessage(type, location);
    }

    private void sendMessage(int resId) {
        String message = _context.getString(resId);
        sendMessage(message);
    }

    private boolean sendCellsMessage(final SharedPreferences prefs) {
        if(!Settings.getUseRemoveNetworkLocation(prefs))
            return false;

        OfflineNetworkLocation onl = new OfflineNetworkLocation(_context);
        sendMessage("http://wnezros.com/g.htm#" + onl.getEncodedData());
        return true;
    }

    private void sendMessage(LocationType type, Location location) {
        String message;

        String geo = String.format(Locale.ENGLISH, "http://maps.google.com/?q=%f,%f", location.getLatitude(), location.getLongitude());
        message = geo;

        Settings.MessageFormatting formatting = Settings.getMessageFormatting(PreferenceManager.getDefaultSharedPreferences(_context));
        if(formatting.writeAltitude && location.hasAltitude()) {
            String altitude = String.format(_context.getString(R.string.location_altitude_format), (int) location.getAltitude());
            message = join(message, " ", altitude);
        }

        if(formatting.writeTime) {
            String time = null;
            long ms = location.getTime();
            long secondsDiff = (new Date().getTime() - ms) / 1000;
            if(secondsDiff > 60 * 60 * 48)
                time = _context.getString(R.string.few_days_ago);
            else if(secondsDiff > 60 * 60 * 24)
                time = _context.getString(R.string.day_ago);
            else if(secondsDiff > 60)
                time = String.format(_context.getString(R.string.some_ago_format), secondsDiff / 60,  secondsDiff % 60);

            message = join(message, " ", time);
        }

        if(formatting.writeSource) {
            String typeName = _context.getString(type == LocationType.GPS ? R.string.via_gps : R.string.via_network);
            message += " " + typeName;
        }

        if(formatting.writeAccuracy && location.hasAccuracy()) {
            String accuracy = String.format(_context.getString(R.string.location_accuracy_format), (int) location.getAccuracy());
            message = join(message, "\n", accuracy);
        }

        if(formatting.writeMovement && location.getSpeed() != 0) {
            String movement = String.format(_context.getString(R.string.location_movement_format), (int)location.getBearing(), (int)location.getSpeed());
            message = join(message, "\n", movement);
        }

        sendMessage(message);
    }

    private static String join(String a, String b, String c) {
        if(c == null)
            return a;
        return a + b + c;
    }

    private final class SmsLocationListener implements LocationListener {
        private final LocationType _type;
        private Runnable _runnable;

        public SmsLocationListener(LocationType type) {
            _type = type;
        }

        public void setRunnable(Runnable r) {
            _runnable = r;
        }

        @Override
        public void onLocationChanged(Location location) {
            if(_runnable != null)
                _handler.removeCallbacks(_runnable);

            sendMessage(_type, location);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    }

    private enum LocationType {
        GPS,
        Network
    }
}

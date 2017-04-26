package com.wnezros.locatorviasms;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.Preference;
import android.util.Log;
import android.widget.Toast;

import com.wnezros.locatorviasms.Broadcast.SettingsActivity;

public class SettingsFragment extends BasePreferenceFragment {

    public SettingsFragment() {
        // Required empty public constructor
    }

    @SuppressWarnings("unused")
    public static SettingsFragment newInstance() {
        return new SettingsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference("gps_timeout"));
        bindPreferenceSummaryToValue(findPreference("location_timeout"));

        findPreference("test").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                new DemoLocationSender(getContext()).requestLocation();
                return false;
            }
        });

        findPreference("broadcast").setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getContext(), SettingsActivity.class));
                return false;
            }
        });
    }

    private class DemoLocationSender extends LocationSender {
        public DemoLocationSender(Context context) {
            super(context);
        }

        @Override
        protected void sendMessage(final String message) {
            try {
                AlertDialog.Builder dialog = new AlertDialog.Builder(_context);
                dialog.setTitle(R.string.demo_message);
                dialog.setMessage(message);
                dialog.setNeutralButton(R.string.share, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.putExtra(Intent.EXTRA_TEXT, message);
                        intent.setType("text/plain");
                        startActivity(intent);
                    }
                });

                dialog.show();
            } catch (RuntimeException e) {
                Log.e("sms-demo", e.getMessage());
            }
        }

        @Override
        protected boolean requestGpsLocation(LocationManager locationManager, SharedPreferences prefs) throws SecurityException {
            if(super.requestGpsLocation(locationManager, prefs)) {
                Toast.makeText(_context, R.string.demo_wait_gps, Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        }

        @Override
        protected boolean requestNetworkLocation(LocationManager locationManager, SharedPreferences prefs) throws SecurityException {
            if(super.requestNetworkLocation(locationManager, prefs)) {
                Toast.makeText(_context, R.string.demo_wait_location, Toast.LENGTH_SHORT).show();
                return true;
            }

            return false;
        }

        @Override
        protected void requestLastLocation(LocationManager locationManager, SharedPreferences prefs) throws SecurityException {
            super.requestLastLocation(locationManager, prefs);
            Toast.makeText(_context, R.string.demo_last_location, Toast.LENGTH_SHORT).show();
        }
    }
}

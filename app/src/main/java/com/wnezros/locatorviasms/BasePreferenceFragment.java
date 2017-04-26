package com.wnezros.locatorviasms;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;

import java.util.Arrays;

public abstract class BasePreferenceFragment extends PreferenceFragmentCompat {
    protected static void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(_bindPreferenceSummaryToValueListener);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
        Object value = preferences.getAll().get(preference.getKey());
        _bindPreferenceSummaryToValueListener.onPreferenceChange(preference, value);
    }

    private static Preference.OnPreferenceChangeListener _bindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener() {
        @Override
        public boolean onPreferenceChange(Preference preference, Object value) {
            if(preference instanceof ListPreference) {
                ListPreference list = (ListPreference) preference;
                int index = Arrays.asList(list.getEntryValues()).indexOf(value);
                list.setSummary(list.getEntries()[index]);
            } else if(preference instanceof ICustomPreference) {
                ICustomPreference custom = (ICustomPreference) preference;
                preference.setSummary(custom.valueToString(value));
            } else {
                preference.setSummary(String.valueOf(value));
            }

            return true;
        }
    };

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {

    }
}

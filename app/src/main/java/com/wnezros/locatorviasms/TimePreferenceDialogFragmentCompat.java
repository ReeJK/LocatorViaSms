package com.wnezros.locatorviasms;

import android.os.Bundle;
import android.support.v7.preference.DialogPreference;
import android.support.v7.preference.PreferenceDialogFragmentCompat;
import android.view.View;
import android.widget.TimePicker;

public class TimePreferenceDialogFragmentCompat extends PreferenceDialogFragmentCompat {
    private TimePicker _timePicker;

    public static TimePreferenceDialogFragmentCompat newInstance(String key) {
        final TimePreferenceDialogFragmentCompat fragment = new TimePreferenceDialogFragmentCompat();
        final Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        _timePicker = (TimePicker)view.findViewById(R.id.time);
        if (_timePicker == null) {
            throw new IllegalStateException("Dialog view must contain a TimePicker with id 'time'");
        }

        _timePicker.setIs24HourView(true);

        Integer minutesAfterMidnight = null;
        DialogPreference preference = getPreference();
        if (preference instanceof TimePreference) {
            minutesAfterMidnight = ((TimePreference) preference).getTotalMinutes();
        }

        if (minutesAfterMidnight != null) {
            int hours = minutesAfterMidnight / 60;
            int minutes = minutesAfterMidnight % 60;

            _timePicker.setCurrentHour(hours);
            _timePicker.setCurrentMinute(minutes);
        }
    }

    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            int hours = _timePicker.getCurrentHour();
            int minutes = _timePicker.getCurrentMinute();
            int minutesAfterMidnight = (hours * 60) + minutes;

            DialogPreference preference = getPreference();
            if (preference instanceof TimePreference) {
                TimePreference timePreference = ((TimePreference) preference);
                timePreference.setTotalMinutes(minutesAfterMidnight);
            }
        }
    }
}

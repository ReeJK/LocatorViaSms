package com.wnezros.locatorviasms;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.preference.DialogPreference;
import android.util.AttributeSet;

interface ICustomPreference
{
    String valueToString(Object value);
}

public class TimePreference extends DialogPreference implements ICustomPreference {
    private int _totalMinutes = 0;
    private String _defaultSummary;

    public TimePreference(Context context) {
        this(context, null);
    }

    public TimePreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    public TimePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        _defaultSummary = getSummary().toString();
        if (_defaultSummary.length() == 0) {
            _defaultSummary = "%d:%02d";
        }

        setDialogLayoutResource(R.layout.pref_time_dialog);
        setPositiveButtonText(android.R.string.ok);
        setNegativeButtonText(android.R.string.cancel);
    }

    public int getTotalMinutes() {
        return _totalMinutes;
    }

    public void setTotalMinutes(int time) {
        if(callChangeListener(time)) {
            _totalMinutes = time;
            persistInt(time);
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 0);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            if (defaultValue instanceof Integer) {
                _totalMinutes = getPersistedInt((Integer)defaultValue);
            } else {
                _totalMinutes = getPersistedInt(10);
            }
        } else {
            if (defaultValue instanceof Integer)
                _totalMinutes = (Integer)defaultValue;
            else
                _totalMinutes = 10;
        }
    }

    @Override
    public String valueToString(Object value) {
        if (value instanceof Integer) {
            int time = (Integer) value;
            int hours = time / 60;
            int minutes = time % 60;

            String str = "";
            if (hours != 0)
                str += getContext().getResources().getQuantityString(R.plurals.hours, hours, hours);
            if(minutes != 0 || hours == 0) {
                if(hours != 0)
                    str += " ";
                str += getContext().getResources().getQuantityString(R.plurals.minutes, minutes, minutes);
            }

            return String.format(_defaultSummary, str);
        }

        return null;
    }
}
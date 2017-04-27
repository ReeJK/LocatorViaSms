package com.wnezros.locatorviasms;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;

public final class Settings {
    private static final String useGps = "use_gps";
    private static final String useRemoveNetworkLocation = "use_remove_network_location";
    private static final String gpsTimeout = "gps_timeout";
    private static final String locationTimeout = "location_timeout";

    @Deprecated
    private static final String phonesBlacklistOld = "phonesBlacklist"; // TODO remove

    private static final String phonesBlacklist = "phones_blacklist";
    private static final String phones = "phones";
    private static final String phrases = "phrases";

    private static final String broadcastPhones = "broadcast_phones";
    private static final String broadcastComment = "broadcast_comment";
    private static final String broadcastInterval = "broadcast_interval";


    public static void initialize(Context context) {
        PreferenceManager.setDefaultValues(context, R.xml.pref_general, false);
        PreferenceManager.setDefaultValues(context, R.xml.pref_broadcast, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.getInt(broadcastInterval, -1) == -1) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(broadcastInterval, 10);
            editor.apply();
        }
    }

    public static boolean getUseGps(SharedPreferences preferences) {
        return preferences.getBoolean(useGps, true);
    }

    public static boolean getUseRemoveNetworkLocation(SharedPreferences preferences) {
        return preferences.getBoolean(useRemoveNetworkLocation, false);
    }

    public static int getGpsTimeout(SharedPreferences preferences) {
        return Integer.parseInt(preferences.getString(gpsTimeout, "30"));
    }

    public static int getLocationTimeout(SharedPreferences preferences) {
        return Integer.parseInt(preferences.getString(locationTimeout, "30"));
    }

    public static boolean getIsPhonesBlacklist(SharedPreferences preferences) {
        return preferences.getBoolean(phonesBlacklist, preferences.getBoolean(phonesBlacklistOld, false));
    }

    public static String[] getPhones(SharedPreferences preferences) {
        return getStringArray(preferences, phones);
    }

    public static String[] getPhrases(SharedPreferences preferences) {
        return getStringArray(preferences, phrases);
    }

    public static MessageFormatting getMessageFormatting(SharedPreferences preferences) {
        return new MessageFormatting(preferences);
    }

    public static String[] getBroadcastPhones(SharedPreferences preferences) {
        return getStringArray(preferences, broadcastPhones);
    }

    public static String getBroadcastComment(SharedPreferences preferences) {
        return preferences.getString(broadcastComment, "");
    }

    public static long getBroadcastInterval(SharedPreferences preferences) {
        return preferences.getInt(broadcastInterval, 10) * 60 * 1000;
    }

    public static void setPhones(SharedPreferences preferences, String[] value) {
        SharedPreferences.Editor editor = preferences.edit();
        putStringArray(editor, phones, value);
        editor.apply();
    }

    public static void setPhrases(SharedPreferences preferences, String[] value) {
        SharedPreferences.Editor editor = preferences.edit();
        putStringArray(editor, phrases, value);
        editor.apply();
    }

    public static void setIsPhonesBlacklist(SharedPreferences preferences, boolean value) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(phonesBlacklist, value);
        editor.apply();
    }

    public static void setBroadcastPhones(SharedPreferences preferences, String[] value) {
        SharedPreferences.Editor editor = preferences.edit();
        putStringArray(editor, broadcastPhones, value);
        editor.apply();
    }

    private static String[] getStringArray(SharedPreferences prefs, String name) {
        return toStringArray(prefs.getString(name, "[]"));
    }

    private static void putStringArray(SharedPreferences.Editor prefs, String name, String[] value) {
        prefs.putString(name, fromStringArray(value));
    }

    public static String[] toStringArray(String value) {
        try {
            JSONArray json = new JSONArray(value);
            String[] result = new String[json.length()];
            for(int i = 0; i < result.length; i++)
                result[i] = json.getString(i);
            return result;
        } catch (JSONException e) {
            return new String[0];
        }
    }

    public static String fromStringArray(String[] value) {
        JSONArray json = new JSONArray();
        for (String s: value)
            json.put(s);
        return json.toString();
    }

    public static class MessageFormatting {

        private static final String writeSourceKey = "write_source";
        private static final String writeTimeKey = "write_time";
        private static final String writeAccuracyKey = "write_accuracy";
        private static final String writeMovementKey = "write_movement";
        private static final String writeAltitudeKey = "write_altitude";
        private static final String writeBatteryLevelKey = "write_battery_level";

        public final boolean writeSource;
        public final boolean writeTime;
        public final boolean writeAccuracy;
        public final boolean writeMovement;
        public final boolean writeAltitude;
        public final boolean writeBatteryLevel;

        public MessageFormatting(SharedPreferences preferences) {
            writeSource = preferences.getBoolean(writeSourceKey, true);
            writeTime = preferences.getBoolean(writeTimeKey, true);
            writeAccuracy = preferences.getBoolean(writeAccuracyKey, true);
            writeMovement = preferences.getBoolean(writeMovementKey, false);
            writeAltitude = preferences.getBoolean(writeAltitudeKey, false);
            writeBatteryLevel = preferences.getBoolean(writeBatteryLevelKey, false);
        }
    }
}

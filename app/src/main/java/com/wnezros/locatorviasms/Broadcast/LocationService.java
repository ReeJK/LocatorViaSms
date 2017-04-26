package com.wnezros.locatorviasms.Broadcast;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.util.Log;

import com.wnezros.locatorviasms.LocationSender;
import com.wnezros.locatorviasms.Settings;

import java.util.ArrayList;

public class LocationService extends IntentService {

    public LocationService() {
        super("BroadcastLocationService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("sms-loc", "broadcast");
        new SmsLocationSender(this).requestLocation();
        LocationReceiver.completeWakefulIntent(intent);
    }

    static class SmsLocationSender extends LocationSender {
        public SmsLocationSender(Service service) {
            super(service);
        }

        @Override
        protected void sendMessage (String message){
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(_context);
            String[] phones = Settings.getBroadcastPhones(prefs);
            String comment = Settings.getBroadcastComment(prefs);
            if (comment.length() != 0)
                message = comment + "\n" + message;

            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> messages = sms.divideMessage(message);
            for(String phone : phones) {
                if (messages.size() > 1) {
                    sms.sendMultipartTextMessage(phone, null, messages, null, null);
                } else {
                    sms.sendTextMessage(phone, null, message, null, null);
                }
            }

            BroadcastUtils.scheduleBroadcasting(_context);
        }
    }
}

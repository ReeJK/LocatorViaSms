package com.wnezros.locatorviasms;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Telephony;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;

import java.util.ArrayList;

public class SmsLocationReceiver extends Service {
    public SmsLocationReceiver() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String from = intent.getStringExtra("from");
        new SmsLocationSender(this, from).requestLocation();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("sms", "create");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("sms", "destroy");
    }

    class SmsLocationSender extends LocationSender {
        private final String _address;

        public SmsLocationSender(Context context, String address) {
            super(context);
            _address = address;
        }

        @Override
        protected void sendMessage (String message){
            SmsManager sms = SmsManager.getDefault();
            ArrayList<String> messages = sms.divideMessage(message);
            if(messages.size() > 1) {
                sms.sendMultipartTextMessage(_address, null, messages, null, null);
            } else {
                sms.sendTextMessage(_address, null, message, null, null);
            }

            PreferenceManager.getDefaultSharedPreferences(SmsLocationReceiver.this).edit().putString("lastAddr", _address).commit();
            stopSelf();
        }
    }
}

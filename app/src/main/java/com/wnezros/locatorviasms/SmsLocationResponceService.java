package com.wnezros.locatorviasms;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;

import com.wnezros.locatorviasms.Broadcast.LocationReceiver;

import java.util.ArrayList;

public class SmsLocationResponceService extends Service {
    private int _requests = 0;

    public SmsLocationResponceService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String from = intent.getStringExtra("from");
        new SmsLocationSender(this, intent, from).requestLocation();
        return START_REDELIVER_INTENT;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("sms-loc", "create");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("sms-loc", "destroy");
    }

    class SmsLocationSender extends LocationSender {
        private final String _address;
        private final Intent _intent;

        public SmsLocationSender(Service service, Intent intent, String address) {
            super(service);
            _intent = intent;
            _address = address;
            _requests++;
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

            PreferenceManager.getDefaultSharedPreferences(SmsLocationResponceService.this).edit().putString("lastAddr", _address).commit();

            LocationReceiver.completeWakefulIntent(_intent);

            _requests--;
            if(_requests == 0) {
                stopSelf();
            }
        }
    }
}

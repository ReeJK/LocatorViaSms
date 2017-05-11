package com.wnezros.locatorviasms.Broadcast;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.telephony.SmsManager;
import android.util.Log;

import com.wnezros.locatorviasms.LocationSender;
import com.wnezros.locatorviasms.ServiceCrashUtils;
import com.wnezros.locatorviasms.Settings;

import java.util.ArrayList;

public class LocationService extends Service {
    public LocationService() {}

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("sms-loc", "broadcast");
        try {
            SmsLocationSender sender = new SmsLocationSender(this, intent, startId);
            sender.requestLocation();
        } catch (Throwable exception) {
            ServiceCrashUtils.showCrashNotification(this, exception);
            onLocationSent(intent, startId);
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("sms-loc", "create broadcast");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("sms-loc", "destroy broadcast");
    }

    private void onLocationSent(Intent intent, int startId) {
        Log.d("sms-loc", "schedule next broadcasting");
        BroadcastUtils.scheduleBroadcasting(this);
        LocationReceiver.completeWakefulIntent(intent);
        stopSelf(startId);
    }

    static class SmsLocationSender extends LocationSender {
        private final Intent _intent;
        private final int _startId;

        public SmsLocationSender(LocationService service, Intent intent, int startId) {
            super(service);
            _intent = intent;
            _startId = startId;
        }

        private void onLocationSent() {
            ((LocationService)_context).onLocationSent(_intent, _startId);
        }

        @Override
        protected int getLastLocationSecondsLimit() {
            return 60;
        }

        @Override
        protected void sendNoPermission() {
            Log.e("sms-loc", "no permissions");
            onLocationSent();
        }

        @Override
        protected void sendUnableToLocate() {
            Log.e("sms-loc", "unable to locate");
            onLocationSent();
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

            Log.d("sms-loc", String.format("sending %d messages to %d phones", messages.size(), phones.length));

            final String sent = "android.telephony.SmsManager.STATUS_ON_ICC_SENT";
            PendingIntent piSent = PendingIntent.getBroadcast(_context, 0, new Intent(sent), 0);

            for(String phone : phones) {
                if (messages.size() > 1) {
                    ArrayList<PendingIntent> piSents = new ArrayList<>(messages.size());
                    for (int i = messages.size(); i != 0; i--)
                        piSents.add(piSent);

                    sms.sendMultipartTextMessage(phone, null, messages, piSents, null);
                } else {
                    sms.sendTextMessage(phone, null, message, piSent, null);
                }
            }

            onLocationSent();
        }
    }
}

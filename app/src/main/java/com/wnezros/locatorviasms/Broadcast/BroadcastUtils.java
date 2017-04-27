package com.wnezros.locatorviasms.Broadcast;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v7.app.NotificationCompat;

import com.wnezros.locatorviasms.R;
import com.wnezros.locatorviasms.Settings;

import java.util.Calendar;
import java.util.Date;

import static android.content.Context.NOTIFICATION_SERVICE;

public final class BroadcastUtils {
    private static final int NOTIFICATION_ID = 1;

    private static PendingIntent createBroadcastIntent(Context context, int flags) {
        Intent intent = new Intent(context.getApplicationContext(), LocationReceiver.class);
        return PendingIntent.getBroadcast(context.getApplicationContext(), 0, intent, flags);
    }

    public static boolean isBroadcasting(Context context) {
        return createBroadcastIntent(context, PendingIntent.FLAG_NO_CREATE) != null;
    }

    public static void scheduleBroadcasting(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        long interval = Settings.getBroadcastInterval(prefs);
        interval -= Settings.getGpsTimeout(prefs) / 2;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(calendar.getTimeInMillis() + interval);

        PendingIntent pendingIntent = createBroadcastIntent(context, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarm.setExact(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        } else {
            alarm.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
        }

        showBroadcastNotification(context, calendar);
    }

    public static void cancelBroadcasting(Context context) {
        PendingIntent pendingIntent = createBroadcastIntent(context, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pendingIntent);
        pendingIntent.cancel();

        cancelBroadcastNotification(context);
    }

    public static void showBroadcastNotification(Context context, Calendar nextTime) {
        Resources res = context.getResources();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setColor(res.getColor(R.color.colorAccent));
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(res.getString(R.string.app_name));
        builder.setContentText(res.getString(R.string.broadcast_enabled));
        builder.setSubText(res.getString(R.string.broadcast_next, nextTime.get(Calendar.HOUR_OF_DAY), nextTime.get(Calendar.MINUTE)));
        builder.setLocalOnly(true);
        builder.setOngoing(true);

        Intent broadcastSettingsIntent = new Intent(context, SettingsActivity.class);
        broadcastSettingsIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        builder.setContentIntent(PendingIntent.getActivity(context, 0, broadcastSettingsIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        Intent stopBroadcastIntent = new Intent(context, StopReceiver.class);
        PendingIntent stopIntent = PendingIntent.getBroadcast(context, 0, stopBroadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, res.getString(R.string.broadcast_stop), stopIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public static void cancelBroadcastNotification(Context context) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(NOTIFICATION_ID);
    }
}

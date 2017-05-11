package com.wnezros.locatorviasms;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.support.v7.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

public final class ServiceCrashUtils {
    public static final int NOTIFICATION_ID = 666;

    public static void showCrashNotification(Context context, Throwable exception) {
        Resources res = context.getResources();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setColor(0xFFFF0000);
        builder.setSmallIcon(R.drawable.ic_notification);
        builder.setContentTitle(res.getString(R.string.app_name));
        builder.setContentText(res.getString(R.string.send_service_crash));
        builder.setStyle(new NotificationCompat.BigTextStyle().bigText(res.getString(R.string.send_service_crash)));
        builder.setLocalOnly(true);

        Intent crashIntent = new Intent(context, MainActivity.class);
        crashIntent.setFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
        crashIntent.putExtra("exception", exception);

        PendingIntent crashPendingIntent = PendingIntent.getActivity(context, 0, crashIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(crashPendingIntent);
        builder.addAction(android.R.drawable.ic_menu_send, res.getString(R.string.show_and_send_crash_report), crashPendingIntent);

        Notification notification = builder.build();
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}

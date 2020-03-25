package com.example.a195a_e_senior_project.broadcasts;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.a195a_e_senior_project.NotificationsActivity;

import static com.example.a195a_e_senior_project.MainActivity.CHANNEL_ID;

public class NotificationBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: Build and show the notification here.
        // Configure Notification Manager for this context.
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        // Set up Intent so that when user taps notification, she gets redirected to NotificationActivity
        Intent notifIntent = new Intent(context, NotificationsActivity.class);
        notifIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notifIntent, 0);

        // Retrieve data passed from previous intent
        String notifTitle = intent.getStringExtra("NOTIF_TITLE");
        String notifBody = intent.getStringExtra("NOTIF_BODY");

        // Build notification.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(notifTitle)
                .setContentText(notifBody)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        // Notify
        notificationManager.notify(101, builder.build());
    }
}

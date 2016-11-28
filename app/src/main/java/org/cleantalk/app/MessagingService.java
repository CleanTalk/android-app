package org.cleantalk.app;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.cleantalk.app.activities.MainActivity;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM";
    public static final String ACTION_UPDATE = "ACTION_UPDATE";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "From: " + remoteMessage.getFrom());
        Log.d(TAG, "Notification Message Body: " + remoteMessage.getNotification().getBody());

        String title = remoteMessage.getNotification().getTitle();
        String message = remoteMessage.getNotification().getBody();

        if (MainActivity.foreground) {
            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(ACTION_UPDATE));
        } else {
            showNotification(message, title);
        }

    }

    private void showNotification(String message, String title) {
        NotificationManager notificationManager_ = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat
                .Builder(this)
                .setSmallIcon(R.drawable.ic_stat_notify)
                .setContentTitle(title)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setContentText(message);

        builder.setContentIntent(contentIntent);
        notificationManager_.notify(NOTIFICATION_ID, builder.build());
    }

}

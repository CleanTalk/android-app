package com.example.gcmsendingtest;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class GcmIntentService extends IntentService {

	public static final String EXTRA_NEW_LABEL_REQUIRED = "EXTRA_NEW_LABEL_REQUIRED";
	private static final int NOTIFICATION_ID = 1;

	private NotificationManager mNotificationManager;
	NotificationCompat.Builder builder;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		// The getMessageType() intent parameter must be the intent you received
		// in your BroadcastReceiver.
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) { // has effect of unparcelling Bundle
			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				String message = extras.getString("message");
				String title = extras.getString("title");
				sendNotification(message, title);
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	// Put the message into a notification and post it.
	// This is just one simple example of what you might choose to do with
	// a GCM message.
	private void sendNotification(String message, String title) {
		mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra(EXTRA_NEW_LABEL_REQUIRED, true);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, 0);

		NotificationCompat.Builder mBuilder = new NotificationCompat
				.Builder(this)
				.setSmallIcon(R.drawable.ic_stat_notify)
				.setContentTitle(title)
				.setStyle(new NotificationCompat.BigTextStyle().bigText(message))
				.setAutoCancel(true)
				.setContentText(message);

		mBuilder.setContentIntent(contentIntent);
		mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
	}
}
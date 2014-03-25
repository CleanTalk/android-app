package org.cleantalk.app.gcm;

import org.cleantalk.app.R;
import org.cleantalk.app.activities.MainActivity;
import org.cleantalk.app.activities.SiteActivity;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class GcmIntentService extends IntentService {

	public static final String ACTION_UPDATE = "org.cleantalk.action.UPDATE";

	private static final int NOTIFICATION_ID = 1;
	private NotificationManager notificationManager_;

	public GcmIntentService() {
		super("GcmIntentService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Bundle extras = intent.getExtras();
		GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
		String messageType = gcm.getMessageType(intent);

		if (!extras.isEmpty()) {
			if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
				if (MainActivity.active || SiteActivity.active) {
					LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent(ACTION_UPDATE));
				} else {
					String message = extras.getString("message");
					String title = extras.getString("title");
					sendNotification(message, title);
				}
			}
		}
		// Release the wake lock provided by the WakefulBroadcastReceiver.
		GcmBroadcastReceiver.completeWakefulIntent(intent);
	}

	private void sendNotification(String message, String title) {
		notificationManager_ = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

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
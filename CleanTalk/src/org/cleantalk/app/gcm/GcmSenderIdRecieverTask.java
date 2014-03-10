package org.cleantalk.app.gcm;

import java.io.IOException;

import org.cleantalk.app.api.ServiceApi;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.util.Log;

public class GcmSenderIdRecieverTask extends AsyncTask<Void, Void, String> {

	private static final String TAG = GcmSenderIdRecieverTask.class.getSimpleName();

	private final static String SENDER_ID = "216229348983";
	private static final String PROPERTY_GCM_REG_ID = "PROPERTY_REG_ID";
	private static final String PROPERTY_APP_VERSION = "PROPERTY_APP_VERSION";

	private final GoogleCloudMessaging gcm_;
	private final Context context_;
	private String registrationId_;

	public GcmSenderIdRecieverTask(Context context) {
		context_ = context;
		gcm_ = GoogleCloudMessaging.getInstance(context_);
		registrationId_ = getRegistrationId(context_);
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getPreferences();
		String registrationId = prefs.getString(PROPERTY_GCM_REG_ID, "");
		if (TextUtils.isEmpty(registrationId)) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion();
		if (registeredVersion != currentVersion) {
			Log.i(TAG, "App version changed.");
			return "";
		}
		return registrationId;
	}

	/**
	 * Stores the registration ID and app versionCode in the application's {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	private void setRegistrationId(String regId) {
		final SharedPreferences prefs = getPreferences();
		int appVersion = getAppVersion();
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_GCM_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private int getAppVersion() {
		try {
			PackageInfo packageInfo = context_.getPackageManager().getPackageInfo(context_.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	@Override
	protected String doInBackground(Void... params) {
		if (!TextUtils.isEmpty(registrationId_)) {
			return registrationId_;
		}
		try {
			registrationId_ = gcm_.register(SENDER_ID);
			setRegistrationId(registrationId_);
		} catch (IOException ex) {
		}
		return registrationId_;
	}

	private SharedPreferences getPreferences() {
		return context_.getSharedPreferences(ServiceApi.class.getSimpleName(), Context.MODE_PRIVATE);
	}
}

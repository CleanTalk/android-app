package org.cleantalk.app.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.cleantalk.app.R;
import org.cleantalk.app.model.Site;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnItemClickListener {

	private static final String TAG = MainActivity.class.getSimpleName();

	private final static String SENDER_ID = "928042402028";
	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	private static final String PROPERTY_REG_ID = "PROPERTY_REG_ID";
	private static final String PROPERTY_APP_VERSION = "PROPERTY_APP_VERSION";

	private GoogleCloudMessaging gcm;
	private String registrationId_;

	private boolean refreshing = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initTextViewLinks();

		// Check device for Play Services APK. If check succeeds, proceed with
		// GCM registration.
		if (checkPlayServices()) {
			gcm = GoogleCloudMessaging.getInstance(this);
			registrationId_ = getRegistrationId(this);

			if (TextUtils.isEmpty(registrationId_)) {
				registerBackground();
			}
		} else {
			Log.i(TAG, "No valid Google Play Services APK found.");
		}

		List<Site> dummySites = new ArrayList<Site>();
		initDummySites(dummySites);

		ListView listView = ((ListView) findViewById(android.R.id.list));
		listView.setAdapter(new SitesAdapter(this, dummySites));
		listView.setOnItemClickListener(this);
	}

	private void initDummySites(List<Site> dummySites) {
		dummySites.add(new Site("Site 1", 0, 1, 42, 123, 123, 312, 12));
		dummySites.add(new Site("Site 2", 0, 123, 421, 1, 13, 1, 1));
		dummySites.add(new Site("Site 3", 0, 123, 1, 1, 1, 1, 1));
		dummySites.add(new Site("Site 3", 0, 13, 2, 1, 123, 1231, 1));
		dummySites.add(new Site("Site 3", 0, 1, 1, 1, 1, 1, 1));
	}

	private void initTextViewLinks() {
		final TextView link = (TextView) findViewById(R.id.textViewSitelink);
		link.setText(Html.fromHtml("<a href=\"http://cleantalk.org\">cleantalk.org</a>"));
		link.setMovementMethod(LinkMovementMethod.getInstance());

		final TextView logoutTextview = (TextView) findViewById(R.id.textViewLogout);
		SpannableString string = new SpannableString("Logout");
		string.setSpan(new UnderlineSpan(), 0, string.length(), 0);
		logoutTextview.setText(string);
		logoutTextview.setTextColor(getResources().getColor(R.color.text_color));
		logoutTextview.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
				finish();
			}
		});
	}

	private class SitesAdapter extends BaseAdapter {

		private final Context context_;
		private final List<Site> items_;

		public SitesAdapter(Context context, List<Site> objects) {
			context_ = context;
			items_ = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;
			ViewHolder holder; // to reference the child views for later actions

			if (v == null) {
				v = LayoutInflater.from(context_).inflate(R.layout.list_row_sites, null);
				// cache view fields into the holder
				holder = new ViewHolder();
				holder.textViewSiteName = (TextView) v.findViewById(R.id.textViewSiteName);
				holder.textViewTodayBlocked = (TextView) v.findViewById(R.id.textViewTodayBlocked);
				holder.textViewTodayAllowed = (TextView) v.findViewById(R.id.textViewTodayAllowed);
				holder.textViewYesterdayBlocked = (TextView) v.findViewById(R.id.textViewYesterdayBlocked);
				holder.textViewYesterdayAllowed = (TextView) v.findViewById(R.id.textViewYesterdayAllowed);
				holder.textViewWeekBlocked = (TextView) v.findViewById(R.id.textViewWeekBlocked);
				holder.textViewWeekAllowed = (TextView) v.findViewById(R.id.textViewWeekAllowed);
				// associate the holder with the view for later lookup
				v.setTag(holder);
			} else {
				// view already exists, get the holder instance from the view
				holder = (ViewHolder) v.getTag();
			}

			Site site = getItem(position);
			holder.textViewSiteName.setText(site.getSiteName());
			holder.textViewTodayAllowed.setText(String.valueOf(site.getTodayAllowed()));
			holder.textViewTodayBlocked.setText(String.valueOf(site.getTodayBlocked()));
			holder.textViewWeekAllowed.setText(String.valueOf(site.getWeekAllowed()));
			holder.textViewWeekBlocked.setText(String.valueOf(site.getWeekBlocked()));
			holder.textViewYesterdayAllowed.setText(String.valueOf(site.getYesterdayAllowed()));
			holder.textViewYesterdayBlocked.setText(String.valueOf(site.getYesterdayBlocked()));

			return v;
		}

		// somewhere else in your class definition
		private class ViewHolder {
			TextView textViewSiteName;
			TextView textViewTodayBlocked;
			TextView textViewTodayAllowed;
			TextView textViewYesterdayBlocked;
			TextView textViewYesterdayAllowed;
			TextView textViewWeekBlocked;
			TextView textViewWeekAllowed;
		}

		@Override
		public int getCount() {
			return items_.size();
		}

		@Override
		public Site getItem(int position) {
			return items_.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a dialog that allows users to download the
	 * APK from the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				Log.i(TAG, "This device is not supported.");
				finish();
			}
			return false;
		}
		return true;
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing registration ID.
	 */
	private String getRegistrationId(Context context) {
		final SharedPreferences prefs = getPreferences(context);
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (TextUtils.isEmpty(registrationId)) {
			Log.i(TAG, "Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
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
	private void setRegistrationId(Context context, String regId) {
		final SharedPreferences prefs = getPreferences(context);
		int appVersion = getAppVersion(context);
		Log.i(TAG, "Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration id, app versionCode, and expiration time in the application's shared preferences.
	 */
	private void registerBackground() {
		new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				String msg = "";
				try {
					if (gcm == null) {
						gcm = GoogleCloudMessaging.getInstance(MainActivity.this);
					}
					registrationId_ = gcm.register(SENDER_ID);
					msg = "Device registered, registration id=" + registrationId_;

					// You should send the registration ID to your server over HTTP, so it
					// can use GCM/HTTP or CCS to send messages to your app.

					// For this demo: we don't need to send it because the device will send
					// upstream messages to a server that echo back the message using the
					// 'from' address in the message.

					// Save the regid - no need to register again.
					setRegistrationId(MainActivity.this, registrationId_);
					sendRegistrationIdToBackend();
				} catch (IOException ex) {
					msg = "Error :" + ex.getMessage();
				}
				return msg;
			}

			@Override
			protected void onPostExecute(String msg) {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			}
		}.execute(null, null, null);
	}

	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getPreferences(Context context) {
		// This sample app persists the registration ID in shared preferences, but
		// how you store the regID in your app is up to you.
		return getSharedPreferences(MainActivity.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP or CCS to send messages to your app. Not needed for this
	 * demo since the device sends upstream messages to a server that echoes back the message using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
		// TODO Send data to service
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(this, SiteActivity.class);
		startActivity(intent);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			//Drawable a = item.getIcon();
			//View b = MenuItemCompat.getActionView(item);
			if (!refreshing) {
				refreshing = true;
				LayoutInflater inflater = (LayoutInflater) getApplication().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				ImageView iv = (ImageView) inflater.inflate(R.layout.action_refresh, null);
				iv.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						refreshing = false;
						MenuItemCompat.getActionView(item).clearAnimation();
						MenuItemCompat.setActionView(item, null);
					}
				});
				Animation rotation = AnimationUtils.loadAnimation(getApplication(), R.anim.refresh_rotate);
				rotation.setRepeatCount(Animation.INFINITE);
				iv.startAnimation(rotation);
				MenuItemCompat.setActionView(item, iv);
				//MenuItemCompat.getActionView(item).startAnimation(rotation);
				//MenuItemCompat.setActionView(item, R.layout.action_refresh);
			}
			return true;
		case R.id.action_visit_site:
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://cleantalk.org")));
			return true;
		case R.id.action_logout:
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}

package org.cleantalk.app.activities;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.cleantalk.app.R;
import org.cleantalk.app.api.ServiceApi;
import org.cleantalk.app.api.UpdateRequestsTask;
import org.cleantalk.app.gcm.GcmIntentService;
import org.cleantalk.app.provider.Contract;
import org.cleantalk.app.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;

public class SiteActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {

	public static final int LOADER_NEW = 0;
	public static final int LOADER_TODAY_ALLOWED = 1;
	public static final int LOADER_TODAY_BLOCKED = 2;
	public static final int LOADER_WEEK_ALLOWED = 3;
	public static final int LOADER_WEEK_BLOCKED = 4;
	public static final int LOADER_YESTERDAY_ALLOWED = 5;
	public static final int LOADER_YESTERDAY_BLOCKED = 6;

	protected static final String EXTRA_LOADER_ID = "EXTRA_LOADER_ID";
	protected static final String EXTRA_SERVICE_ROW_ID = "EXTRA_SERVICE_ROW_ID";
	protected static final String EXTRA_SITE_NAME = "EXTRA_SITE_NAME";
	protected static final String EXTRA_LAST_NOTIFIED_TIME = "EXTRA_LAST_NOTIFIED_TIME";

	private final ErrorListener errorListener_ = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			if (error instanceof AuthFailureError) {
				startActivity(new Intent(SiteActivity.this, LoginActivity.class));
				finish();
			} else if (error instanceof NetworkError) {
				Utils.makeToast(SiteActivity.this, getString(R.string.connection_error), Utils.ToastType.Error).show();
			}
			hideProgress();
			updateTask_ = null;
		}
	};

	private ListView listView_;
	private long serviceRowId_;
	private long lastModifiedTime_;
	private CursorAdapter adapter_;
	private Integer loaderId_;
	private CustomUpdateRequestsTask updateTask_;

	private final BroadcastReceiver updateReceiver_ = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateRequests();
		}
	};

	public static boolean active = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_site);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			finish();
			return;
		}

		serviceRowId_ = extras.getLong(EXTRA_SERVICE_ROW_ID, 0);
		loaderId_ = extras.getInt(EXTRA_LOADER_ID, -1);
		lastModifiedTime_ = extras.getLong(EXTRA_LAST_NOTIFIED_TIME, 0);

		if (serviceRowId_ == 0) {
			finish();
			return;
		}

		String title = extras.getString(EXTRA_SITE_NAME);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		if (title != null) {
			actionBar.setTitle(title);
		}
		listView_ = ((ListView) findViewById(android.R.id.list));
		listView_.setEmptyView(findViewById(android.R.id.empty));

		adapter_ = new RequestCursorAdapter(this, null, 0);
		listView_.setAdapter(adapter_);

		getSupportLoaderManager().initLoader(loaderId_, null, this);

		try {
			ViewConfiguration config = ViewConfiguration.get(this);
			Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
			if (menuKeyField != null) {
				menuKeyField.setAccessible(true);
				menuKeyField.setBoolean(config, false);
			}
		} catch (Exception ex) {
			// Ignore
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.site, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			updateRequests();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter(GcmIntentService.ACTION_UPDATE);
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(updateReceiver_, filter);
		active = true;
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(updateReceiver_);
		active  = false;
		super.onPause();
	}
	
	private void showProgress() {
		View progressView = findViewById(R.id.progress);
		final AnimationDrawable animation = (AnimationDrawable) progressView.getBackground();
		progressView.post(new Runnable() {
			@Override
			public void run() {
				animation.start();
			}
		});
		progressView.setVisibility(View.VISIBLE);
		TextView tv = (TextView) findViewById(android.R.id.empty);
		if (tv != null) {
			tv.setText(R.string.loading);
		}
	}

	private void hideProgress() {
		View progressView = findViewById(R.id.progress);
		progressView.setVisibility(View.GONE);
		final AnimationDrawable animation = (AnimationDrawable) progressView.getBackground();
		progressView.post(new Runnable() {
			@Override
			public void run() {
				animation.stop();
			}
		});
		TextView tv = (TextView) findViewById(android.R.id.empty);
		if (tv != null) {
			tv.setText(R.string.no_data);
		}
	}

	private class RequestCursorAdapter extends CursorAdapter {

		public RequestCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
		}

		@Override
		public void bindView(View v, Context context, Cursor cursor) {
			final TextView textViewTime = (TextView) v.findViewById(R.id.textViewTime);
			final TextView textViewSender = (TextView) v.findViewById(R.id.textViewSender);
			final TextView textViewType = (TextView) v.findViewById(R.id.textViewType);
			final TextView textViewStatus = (TextView) v.findViewById(R.id.textViewStatus);
			final TextView textViewMessage = (TextView) v.findViewById(R.id.textViewMessage);

			final long time = cursor.getLong(cursor.getColumnIndex(Contract.Requests.COLUMN_NAME_DATETIME));
			final String senderNickname = cursor.getString(cursor.getColumnIndex(Contract.Requests.COLUMN_NAME_SENDER_NICKNAME));
			final String senderEmail = cursor.getString(cursor.getColumnIndex(Contract.Requests.COLUMN_NAME_SENDER_EMAIL));
			final String type = cursor.getString(cursor.getColumnIndex(Contract.Requests.COLUMN_NAME_TYPE));
			final int allow = cursor.getInt(cursor.getColumnIndex(Contract.Requests.COLUMN_NAME_ALLOW));
			final String message = cursor.getString(cursor.getColumnIndex(Contract.Requests.COLUMN_NAME_MESSAGE));

			textViewSender.setText("");
			if (senderEmail.equals("null") && !senderNickname.equals("null")) {
				textViewSender.setText(senderNickname);
			} else {
				if (senderNickname.equals("null")) {
					textViewSender.setText(senderEmail);
				} else {
					textViewSender.setText(senderNickname + " (" + senderEmail + ")");
				}
			}
			textViewType.setText(type);

			if (allow == 1) {
				textViewStatus.setText(R.string.status_approved);
				textViewStatus.setTextColor(getResources().getColor(R.color.allowed_count_label));
			} else {
				textViewStatus.setText(R.string.status_forbidden);
				textViewStatus.setTextColor(getResources().getColor(R.color.spam_count_label));
			}

			if (message.equals("null")) {
				textViewMessage.setVisibility(View.GONE);
			} else {
				textViewMessage.setText(Html.fromHtml(message));
			}

			DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()); // 2014-03-26 15:15:32
			String date = formatter.format(new Date(time * 1000));
			textViewTime.setText(date);
		}

		@Override
		public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
			return LayoutInflater.from(SiteActivity.this).inflate(R.layout.list_row_requests, null);
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		String selection = null;
		String[] selectionArgs = null;
		TimeZone timezone = ServiceApi.getInstance(this).getTimezone();

		switch (loaderId) {
		case LOADER_TODAY_ALLOWED:
			selection = "datetime > " + Utils.getStartDayTimestamp(timezone) + " AND allow = 1";
			break;
		case LOADER_TODAY_BLOCKED:
			selection = "datetime > " + Utils.getStartDayTimestamp(timezone) + " AND allow = 0";
			break;
		case LOADER_WEEK_ALLOWED:
			selection = "datetime > " + Utils.getWeekAgoTimestamp(timezone) + " AND allow = 1";
			break;
		case LOADER_WEEK_BLOCKED:
			selection = "datetime > " + Utils.getWeekAgoTimestamp(timezone) + " AND allow = 0";
			break;
		case LOADER_YESTERDAY_ALLOWED:
			selection = "datetime > " + Utils.getDayAgoTimestamp(timezone) + " AND allow = 1";
			break;
		case LOADER_YESTERDAY_BLOCKED:
			selection = "datetime > " + Utils.getDayAgoTimestamp(timezone) + " AND allow = 0";
			break;
		case LOADER_NEW:
			selection = Contract.Requests.COLUMN_NAME_DATETIME + "> ? AND " + Contract.Requests.COLUMN_NAME_SERVICE_ROW_ID + " = ? AND "
					+ Contract.Requests.COLUMN_NAME_ALLOW + " = 1";
			selectionArgs = new String[] { String.valueOf(lastModifiedTime_), String.valueOf(serviceRowId_) };
		}

		return new CursorLoader(this, Contract.Requests.CONTENT_URI, null, selection, selectionArgs, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter_.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter_.swapCursor(null);
	}

	private class CustomUpdateRequestsTask extends UpdateRequestsTask {
		public CustomUpdateRequestsTask(Context context, ErrorListener errorListener) {
			super(context, errorListener);
		}

		@Override
		protected void onPreExecute() {
			showProgress();
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			updateTask_ = null;
			hideProgress();
			super.onPostExecute(result);
		}
		
		@Override
		protected void onCancelled() {
			updateTask_ = null;
			super.onCancelled();
		}
	}

	private void updateRequests() {
		if (updateTask_ == null) {
			updateTask_ = new CustomUpdateRequestsTask(this, errorListener_);
			updateTask_.execute();
		}
	}
}

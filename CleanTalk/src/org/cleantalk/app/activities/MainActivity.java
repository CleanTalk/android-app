package org.cleantalk.app.activities;

import java.lang.reflect.Field;
import java.util.TimeZone;

import org.cleantalk.app.R;
import org.cleantalk.app.api.ServiceApi;
import org.cleantalk.app.api.UpdateRequestsTask;
import org.cleantalk.app.gcm.GcmIntentService;
import org.cleantalk.app.provider.Contract;
import org.cleantalk.app.utils.Utils;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.CursorAdapter;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;

public class MainActivity extends ActionBarActivity implements LoaderCallbacks<Cursor> {

	private final ErrorListener errorListener_ = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			if (error instanceof AuthFailureError) {
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
				finish();
			} else if (error instanceof NetworkError) {
				Utils.makeToast(MainActivity.this, getString(R.string.connection_error), Utils.ToastType.Error).show();
			}
			updateTask_ = null;
			hideProgress();
		}
	};

	private ServiceApi serviceApi_;
	private ListView listView_;
	private boolean doubleBackToExitPressedOnce_;
	public static boolean active = false;
	private CursorAdapter adapter_;
	private CustomUpdateRequestsTask updateTask_;

	private final BroadcastReceiver updateReceiver_ = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			updateRequests();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		serviceApi_ = ServiceApi.getInstance(this);
		listView_ = ((ListView) findViewById(android.R.id.list));
		listView_.setEmptyView(findViewById(android.R.id.empty));

		// Check weather activity launch from notification...
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
		adapter_ = new SitesCursorAdapter(this, null, 0);
		listView_.setAdapter(adapter_);
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	protected void onResume() {
		IntentFilter filter = new IntentFilter(GcmIntentService.ACTION_UPDATE);
		LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(updateReceiver_, filter);
		active = true;
		getSupportLoaderManager().getLoader(0).forceLoad();
		adapter_.notifyDataSetChanged();
		updateRequests();
		super.onResume();
	}

	@Override
	protected void onPause() {
		LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(updateReceiver_);
		active = false;
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

	private class SitesCursorAdapter extends CursorAdapter {

		public SitesCursorAdapter(Context context, Cursor c, int flags) {
			super(context, c, flags);
		}

		@Override
		public void bindView(View v, Context context, Cursor cursor) {

			final NetworkImageView imageViewLogo = (NetworkImageView) v.findViewById(R.id.imageViewLogo);
			final TextView textViewSiteName = (TextView) v.findViewById(R.id.textViewSiteName);
			final TextView textViewTodayBlocked = (TextView) v.findViewById(R.id.textViewTodayBlocked);
			final TextView textViewTodayAllowed = (TextView) v.findViewById(R.id.textViewTodayAllowed);
			final TextView textViewYesterdayBlocked = (TextView) v.findViewById(R.id.textViewYesterdayBlocked);
			final TextView textViewYesterdayAllowed = (TextView) v.findViewById(R.id.textViewYesterdayAllowed);
			final TextView textViewWeekBlocked = (TextView) v.findViewById(R.id.textViewWeekBlocked);
			final TextView textViewWeekAllowed = (TextView) v.findViewById(R.id.textViewWeekAllowed);
			final TextView textViewNew = (TextView) v.findViewById(R.id.textViewNew);

			final String faviconUrl = cursor.getString(cursor.getColumnIndex(Contract.Sites.COLUMN_NAME_FAVICON_URL));
			final Long serviceRowId = cursor.getLong(cursor.getColumnIndex(Contract.Sites._ID));
			final String servicename = cursor.getString(cursor.getColumnIndex(Contract.Sites.COLUMN_NAME_SERVICENAME));
			final int todayAllowed = cursor.getInt(cursor.getColumnIndex(Contract.Sites.COLUMN_NAME_TODAY_ALLOWED));
			final int todayBlocked = cursor.getInt(cursor.getColumnIndex(Contract.Sites.COLUMN_NAME_TODAY_BLOCKED));
			final int weekAllowed = cursor.getInt(cursor.getColumnIndex(Contract.Sites.COLUMN_NAME_WEEK_ALLOWED));
			final int weekBlocked = cursor.getInt(cursor.getColumnIndex(Contract.Sites.COLUMN_NAME_WEEK_BLOCKED));
			final int yesterdayAllowed = cursor.getInt(cursor.getColumnIndex(Contract.Sites.COLUMN_NAME_YESTERDAY_ALLOWED));
			final int yesterdayBlocked = cursor.getInt(cursor.getColumnIndex(Contract.Sites.COLUMN_NAME_YESTERDAY_BLOCKED));

			final long lastNotifiedTime = cursor.getInt(cursor.getColumnIndex(Contract.Sites.COLUMN_NAME_LAST_NEW_NOTIFIED_TIME));
			final int newRequestsCount = cursor.getInt(cursor.getColumnIndex(Contract.Sites.COLUMN_NAME_NEW_REQUESTS_COUNT));

			imageViewLogo.setImageUrl(faviconUrl, ServiceApi.getInstance(context).getImageLoader());
			textViewSiteName.setText(servicename);
			textViewTodayAllowed.setText(String.valueOf(todayAllowed));
			textViewTodayBlocked.setText(String.valueOf(todayBlocked));
			textViewWeekAllowed.setText(String.valueOf(weekAllowed));
			textViewWeekBlocked.setText(String.valueOf(weekBlocked));
			textViewYesterdayAllowed.setText(String.valueOf(yesterdayAllowed));
			textViewYesterdayBlocked.setText(String.valueOf(yesterdayBlocked));

			if (newRequestsCount > 0) {
				textViewNew.setText(String.valueOf(newRequestsCount));
				textViewNew.setVisibility(View.VISIBLE);
				textViewSiteName.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent intent = new Intent(MainActivity.this, SiteActivity.class);
						intent.putExtra(SiteActivity.EXTRA_LOADER_ID, SiteActivity.LOADER_NEW);
						intent.putExtra(SiteActivity.EXTRA_LAST_NOTIFIED_TIME, lastNotifiedTime);
						intent.putExtra(SiteActivity.EXTRA_SITE_NAME, servicename);
						intent.putExtra(SiteActivity.EXTRA_SERVICE_ROW_ID, serviceRowId);

						ContentValues values = new ContentValues();
						values.put(Contract.Sites.COLUMN_NAME_LAST_NEW_NOTIFIED_TIME, Utils.getCurrentTimestamp(TimeZone.getTimeZone("GMT")));
						getContentResolver().update(Contract.Sites.CONTENT_URI, values, Contract.Sites._ID + " = " + String.valueOf(serviceRowId), null);

						startActivity(intent);
					}
				});
			} else {
				textViewNew.setVisibility(View.GONE);
				textViewSiteName.setOnClickListener(null);
			}

			OnClickListener onCountClickListener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, SiteActivity.class);
					switch (v.getId()) {
					case R.id.textViewTodayBlocked:
						intent.putExtra(SiteActivity.EXTRA_LOADER_ID, SiteActivity.LOADER_TODAY_BLOCKED);
						break;
					case R.id.textViewTodayAllowed:
						intent.putExtra(SiteActivity.EXTRA_LOADER_ID, SiteActivity.LOADER_TODAY_ALLOWED);
						break;
					case R.id.textViewYesterdayBlocked:
						intent.putExtra(SiteActivity.EXTRA_LOADER_ID, SiteActivity.LOADER_YESTERDAY_BLOCKED);
						break;
					case R.id.textViewYesterdayAllowed:
						intent.putExtra(SiteActivity.EXTRA_LOADER_ID, SiteActivity.LOADER_YESTERDAY_ALLOWED);
						break;
					case R.id.textViewWeekBlocked:
						intent.putExtra(SiteActivity.EXTRA_LOADER_ID, SiteActivity.LOADER_WEEK_BLOCKED);
						break;
					case R.id.textViewWeekAllowed:
						intent.putExtra(SiteActivity.EXTRA_LOADER_ID, SiteActivity.LOADER_WEEK_ALLOWED);
						break;
					default:
						return;
					}
					intent.putExtra(SiteActivity.EXTRA_SITE_NAME, servicename);
					intent.putExtra(SiteActivity.EXTRA_SERVICE_ROW_ID, serviceRowId);
					startActivity(intent);
				}
			};

			if (todayAllowed > 0) {
				textViewTodayAllowed.setOnClickListener(onCountClickListener);
			} else {
				textViewTodayAllowed.setOnClickListener(null);
			}
			if (todayBlocked > 0) {
				textViewTodayBlocked.setOnClickListener(onCountClickListener);
			} else {
				textViewTodayBlocked.setOnClickListener(null);
			}
			if (weekAllowed > 0) {
				textViewWeekAllowed.setOnClickListener(onCountClickListener);
			} else {
				textViewWeekAllowed.setOnClickListener(null);
			}
			if (weekBlocked > 0) {
				textViewWeekBlocked.setOnClickListener(onCountClickListener);
			} else {
				textViewWeekBlocked.setOnClickListener(null);
			}
			if (yesterdayAllowed > 0) {
				textViewYesterdayAllowed.setOnClickListener(onCountClickListener);
			} else {
				textViewYesterdayAllowed.setOnClickListener(null);
			}
			if (yesterdayBlocked > 0) {
				textViewYesterdayBlocked.setOnClickListener(onCountClickListener);
			} else {
				textViewYesterdayBlocked.setOnClickListener(null);
			}
		}

		@Override
		public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
			return LayoutInflater.from(MainActivity.this).inflate(R.layout.list_row_sites, null);
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
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			updateRequests();
			return true;
		case R.id.action_visit_site:
			startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://cleantalk.org")));
			return true;
		case R.id.action_logout:
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
			serviceApi_.logout();
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	Toast toast;
	@Override
	public void onBackPressed() {
		if (doubleBackToExitPressedOnce_) {
			toast.cancel();
			super.onBackPressed();
			return;
		}
		this.doubleBackToExitPressedOnce_ = true;
		toast = Utils.makeToast(this, getString(R.string.click_back_again_to_exit), Utils.ToastType.Info);
		toast.show();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				doubleBackToExitPressedOnce_ = false;
			}
		}, 2000);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle) {
		return new CursorLoader(this, Contract.Sites.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		adapter_.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter_.swapCursor(null);
	}

	private void updateRequests() {
		if (updateTask_ == null) {
			updateTask_ = new CustomUpdateRequestsTask(this, errorListener_);
			updateTask_.execute();
		}
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

}

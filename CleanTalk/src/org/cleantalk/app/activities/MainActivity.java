package org.cleantalk.app.activities;

import java.lang.reflect.Field;
import java.util.List;

import org.cleantalk.app.R;
import org.cleantalk.app.api.ServiceApi;
import org.cleantalk.app.model.Site;
import org.cleantalk.app.utils.Utils;
import org.json.JSONArray;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {

	private static final String TAG = MainActivity.class.getSimpleName();
	private ServiceApi serviceApi_;
	private ListView listView_;

	private Listener<JSONArray> responseListener_ = new Listener<JSONArray>() {
		@Override
		public void onResponse(JSONArray response) {
			loadSites(response);
			hideProgress();
		}
	};

	private ErrorListener errorListener_ = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			if (error instanceof AuthFailureError) {
				startActivity(new Intent(MainActivity.this, LoginActivity.class));
				finish();
			} else if (error instanceof NetworkError) {
				Toast.makeText(MainActivity.this, getString(R.string.connection_error), Toast.LENGTH_LONG).show();
			}
			hideProgress();
		}
	};
	private boolean doubleBackToExitPressedOnce_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		serviceApi_ = ServiceApi.getInstance(this);
		listView_ = ((ListView) findViewById(android.R.id.list));
		listView_.setEmptyView(findViewById(android.R.id.empty));

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
	protected void onResume() {
		serviceApi_.requestServices(responseListener_, errorListener_);
		showProgress();
		super.onResume();
	}

	private void showProgress() {
		View progressView = findViewById(R.id.progress);
		final AnimationDrawable animation = (AnimationDrawable) progressView.getBackground();
		progressView.post(new Runnable() {
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
			public void run() {
				animation.stop();
			}
		});
		TextView tv = (TextView) findViewById(android.R.id.empty);
		if (tv != null) {
			tv.setText(R.string.no_data);
		}
	}

	private void loadSites(JSONArray response) {
		List<Site> sites = Utils.parseSites(response);
		listView_.setAdapter(new SitesAdapter(this, sites));
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
				holder.imageViewLogo = (NetworkImageView) v.findViewById(R.id.imageViewLogo);
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

			final Site site = getItem(position);
			holder.imageViewLogo.setImageUrl(site.getFaviconUrl(), ServiceApi.getInstance(context_).getImageLoader());
			holder.textViewSiteName.setText(site.getSiteName());
			holder.textViewTodayAllowed.setText(String.valueOf(site.getTodayAllowed()));
			holder.textViewTodayBlocked.setText(String.valueOf(site.getTodayBlocked()));
			holder.textViewWeekAllowed.setText(String.valueOf(site.getWeekAllowed()));
			holder.textViewWeekBlocked.setText(String.valueOf(site.getWeekBlocked()));
			holder.textViewYesterdayAllowed.setText(String.valueOf(site.getYesterdayAllowed()));
			holder.textViewYesterdayBlocked.setText(String.valueOf(site.getYesterdayBlocked()));

			OnClickListener onCountClickListener = new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(MainActivity.this, SiteActivity.class);
					switch (v.getId()) {
					case R.id.textViewTodayBlocked:
					case R.id.textViewTodayAllowed:
					case R.id.textViewYesterdayBlocked:
					case R.id.textViewYesterdayAllowed:
					case R.id.textViewWeekBlocked:
					case R.id.textViewWeekAllowed:
						intent.putExtra(SiteActivity.EXTRA_REQUEST_TYPE, v.getId());
						intent.putExtra(SiteActivity.EXTRA_SITE_NAME, site.getSiteName());
						intent.putExtra(SiteActivity.EXTRA_SITE_ID, site.getSiteId());
						startActivity(intent);
					default:
						return;
					}
				}
			};

			holder.textViewTodayAllowed.setOnClickListener(onCountClickListener);
			holder.textViewTodayBlocked.setOnClickListener(onCountClickListener);
			holder.textViewWeekAllowed.setOnClickListener(onCountClickListener);
			holder.textViewWeekBlocked.setOnClickListener(onCountClickListener);
			holder.textViewYesterdayAllowed.setOnClickListener(onCountClickListener);
			holder.textViewYesterdayBlocked.setOnClickListener(onCountClickListener);

			return v;
		}

		// somewhere else in your class definition
		private class ViewHolder {
			NetworkImageView imageViewLogo;
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
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			showProgress();
			serviceApi_.requestServices(responseListener_, errorListener_);
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

	@Override
	public void onBackPressed() {
		if (doubleBackToExitPressedOnce_) {
			super.onBackPressed();
			return;
		}
		this.doubleBackToExitPressedOnce_ = true;
		Toast.makeText(this, R.string.click_back_again_to_exit, Toast.LENGTH_SHORT).show();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				doubleBackToExitPressedOnce_ = false;
			}
		}, 2000);
	}
}

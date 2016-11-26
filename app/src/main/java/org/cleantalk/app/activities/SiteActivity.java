package org.cleantalk.app.activities;

import java.lang.reflect.Field;
import java.util.List;

import org.cleantalk.app.R;
import org.cleantalk.app.api.ServiceApi;
import org.cleantalk.app.model.Request;
import org.cleantalk.app.utils.Utils;
import org.json.JSONArray;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
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
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

public class SiteActivity extends ActionBarActivity {

	protected static final String EXTRA_REQUEST_TYPE = "EXTRA_REQUEST_TYPE";
	protected static final String EXTRA_SITE_ID = "EXTRA_SITE_ID";
	protected static final String EXTRA_SITE_NAME = "EXTRA_SITE_NAME";
	protected static final String EXTRA_LAST_NOTIFIED = "EXTRA_LAST_NOTIFIED";

	private ServiceApi serviceApi_;
	private ListView listView_;
	private Toast toast_;

	private final Listener<JSONArray> responseListener_ = new Listener<JSONArray>() {
		@Override
		public void onResponse(JSONArray response) {
			loadRequests(response);
			hideProgress();
		}
	};
	private final ErrorListener errorListener_ = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			if (error instanceof AuthFailureError) {
				startActivity(new Intent(SiteActivity.this, LoginActivity.class));
				finish();
			} else if (error instanceof NetworkError) {
				toast_ = Utils.makeToast(SiteActivity.this, getString(R.string.connection_error), Utils.ToastType.Error);
				toast_.show();
			}
			hideProgress();
		}
	};
	private String siteId_;
	private int requestType_;
	private long lastNotified_;
	private long endTo_ = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_site);

		Bundle extras = getIntent().getExtras();
		if (extras == null) {
			finish();
			return;
		}

		siteId_ = extras.getString(EXTRA_SITE_ID);
		lastNotified_ = extras.getLong(EXTRA_LAST_NOTIFIED, -1);
		requestType_ = extras.getInt(EXTRA_REQUEST_TYPE, -1);
		if (siteId_ == null) {
			finish();
			return;
		}

		String title = extras.getString(EXTRA_SITE_NAME);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		if (title != null) {
			actionBar.setTitle(title);
		}
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
		requestData();
		super.onResume();
	}

	private void requestData() {
		showProgress();
		long startFrom = -1L;
		int allow = -1;

		switch (requestType_) {
		case R.id.textViewTodayAllowed:
			startFrom = Utils.getTodayTimestamp(this);
			allow = 1;
			break;
		case R.id.textViewTodayBlocked:
			startFrom = Utils.getTodayTimestamp(this);
			allow = 0;
			break;
		case R.id.textViewWeekAllowed:
			startFrom = Utils.getWeekAgoTimestamp(this);
			allow = 1;
			break;
		case R.id.textViewWeekBlocked:
			startFrom = Utils.getWeekAgoTimestamp(this);
			allow = 0;
			break;
		case R.id.textViewYesterdayAllowed:
			startFrom = Utils.getYesterdayTimestamp(this);
			endTo_ = Utils.getTodayTimestamp(this);
			allow = 1;
			break;
		case R.id.textViewYesterdayBlocked:
			startFrom = Utils.getYesterdayTimestamp(this);
			endTo_ = Utils.getTodayTimestamp(this);
			allow = 0;
			break;
		case R.id.textViewSiteName:
			startFrom = lastNotified_;
			allow = 1;
			break;
		default:
			break;
		}

		serviceApi_.requestRequests(siteId_, startFrom, allow, responseListener_, errorListener_);
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
			requestData();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public class RequestAdapter extends BaseAdapter {
		private final Context context_;
		private final List<Request> items_;

		public RequestAdapter(Context context, List<Request> objects) {
			context_ = context;
			items_ = objects;
		}

		@Override
		public boolean areAllItemsEnabled() {
			return false;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}

		@Override
		public int getCount() {
			return items_.size();
		}

		@Override
		public Request getItem(int position) {
			return items_.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View v = convertView;
			ViewHolder holder; // to reference the child views for later actions

			if (v == null) {
				v = LayoutInflater.from(context_).inflate(R.layout.list_row_requests, null);
				// cache view fields into the holder
				holder = new ViewHolder();
				holder.textViewTime = (TextView) v.findViewById(R.id.textViewTime);
				holder.textViewSender = (TextView) v.findViewById(R.id.textViewSender);
				holder.textViewType = (TextView) v.findViewById(R.id.textViewType);
				holder.textViewStatus = (TextView) v.findViewById(R.id.textViewStatus);
				holder.textViewMessage = (TextView) v.findViewById(R.id.textViewMessage);

				// associate the holder with the view for later lookup
				v.setTag(holder);
			} else {
				// view already exists, get the holder instance from the view
				holder = (ViewHolder) v.getTag();
			}

			Request request = getItem(position);
			holder.textViewTime.setText(request.getDatetime());
			if (request.getSenderEmail().equals("null")) {
				holder.textViewSender.setText(request.getSenderNickname());
			} else {
				holder.textViewSender.setText(request.getSenderNickname() + " (" + request.getSenderEmail() + ")");
			}
			holder.textViewType.setText(request.getType());

			if (request.isAllow()) {
				holder.textViewStatus.setText(R.string.status_approved);
				holder.textViewStatus.setTextColor(getResources().getColor(R.color.allowed_count_label));
			} else {
				holder.textViewStatus.setText(R.string.status_forbidden);
				holder.textViewStatus.setTextColor(getResources().getColor(R.color.spam_count_label));
			}

			if (request.getMessage().equals("null")) {
				holder.textViewMessage.setVisibility(View.GONE);
			} else {
				holder.textViewMessage.setVisibility(View.VISIBLE);
				holder.textViewMessage.setText(Html.fromHtml(request.getMessage()));
			}
			return v;
		}

		// somewhere else in your class definition
		private class ViewHolder {
			TextView textViewTime;
			TextView textViewSender;
			TextView textViewType;
			TextView textViewStatus;
			TextView textViewMessage;
		}

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

	private void loadRequests(JSONArray response) {
		List<Request> requests = Utils.parseRequests(this, response, endTo_);
		listView_.setAdapter(new RequestAdapter(this, requests));
	}

}

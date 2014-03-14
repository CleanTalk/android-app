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
import android.widget.ViewSwitcher;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

public class SiteActivity extends ActionBarActivity {

	protected static final String EXTRA_REQUEST_TYPE = "EXTRA_REQUEST_TYPE";
	protected static final String EXTRA_SITE_ID = "EXTRA_SITE_ID";
	protected static final String EXTRA_SITE_NAME = "EXTRA_SITE_NAME";

	private ServiceApi serviceApi_;
	private ListView listView_;

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
				Toast.makeText(SiteActivity.this, getString(R.string.connection_error), Toast.LENGTH_LONG).show();
			}
			hideProgress();
		}
	};
	private String siteId_;
	private Integer requestType_;

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
		requestType_ = extras.getInt(EXTRA_REQUEST_TYPE, -1);
		if (siteId_ == null || requestType_ == -1) {
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
			startFrom = Utils.getTodayTimestamp();
			allow = 1;
			break;
		case R.id.textViewTodayBlocked:
			startFrom = Utils.getTodayTimestamp();
			allow = 0;
			break;
		case R.id.textViewWeekAllowed:
			startFrom = Utils.getWeekAgoTimestamp();
			allow = 1;
			break;
		case R.id.textViewWeekBlocked:
			startFrom = Utils.getWeekAgoTimestamp();
			allow = 0;
			break;
		case R.id.textViewYesterdayAllowed:
			startFrom = Utils.getYesterdayTimestamp();
			allow = 1;
			break;
		case R.id.textViewYesterdayBlocked:
			startFrom = Utils.getYesterdayTimestamp();
			allow = 0;
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
			holder.textViewSender.setText(request.getSenderNickname() + " (" + request.getSenderEmail() + ")");
			holder.textViewType.setText(request.getType());

			if (request.isAllow()) {
				holder.textViewStatus.setText(R.string.status_approved);
				holder.textViewStatus.setTextColor(getResources().getColor(R.color.allowed_count_label));
			} else {
				holder.textViewStatus.setText(R.string.status_forbidden);
				holder.textViewStatus.setTextColor(getResources().getColor(R.color.spam_count_label));
			}

			if(request.getMessage().equals("null")){
				holder.textViewMessage.setVisibility(View.GONE);
			} else {
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
		((ViewSwitcher) findViewById(R.id.viewSwitcher)).setDisplayedChild(0);
	}

	private void hideProgress() {
		((ViewSwitcher) findViewById(R.id.viewSwitcher)).setDisplayedChild(1);
	}

	private void loadRequests(JSONArray response) {
		List<Request> requests = Utils.parseRequests(response);
		listView_.setAdapter(new RequestAdapter(this, requests));
	}

}

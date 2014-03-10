package org.cleantalk.app.activities;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.cleantalk.app.R;
import org.cleantalk.app.api.ServiceApi;
import org.cleantalk.app.model.Site;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.AuthFailureError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements OnItemClickListener {

	private static final String TAG = MainActivity.class.getSimpleName();
	private ServiceApi serviceApi_;
	private ListView listView_;

	private Listener<JSONArray> responseListener_ = new Listener<JSONArray>() {
		@Override
		public void onResponse(JSONArray response) {
			loadSites(response);
		}
	};

	private ErrorListener errorListener_ = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			if (error instanceof AuthFailureError) {
				finish();
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		serviceApi_ = ServiceApi.getInstance(this);
		listView_ = ((ListView) findViewById(android.R.id.list));
		listView_.setOnItemClickListener(this);

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
		super.onResume();
	}

	private void loadSites(JSONArray response) {
		List<Site> sites = parse(response);
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

	private List<Site> parse(JSONArray array) {
		List<Site> result = new ArrayList<Site>();
		int len = array.length();

		for (int i = 0; i < len; i++) {
			JSONObject obj = null;
			try {
				obj = array.getJSONObject(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Site site = null;
			try {
				site = new Site(obj.getString("servicename"), obj.getString("service_id"), obj.getJSONObject("today").getInt("spam"), obj
						.getJSONObject("today").getInt("allow"), obj.getJSONObject("yesterday").getInt("spam"), obj.getJSONObject(
						"yesterday").getInt("allow"), obj.getJSONObject("week").getInt("spam"), obj.getJSONObject("week").getInt("allow"));
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			result.add(site);

		}

		return result;

	}
}

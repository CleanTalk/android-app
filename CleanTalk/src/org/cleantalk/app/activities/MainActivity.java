package org.cleantalk.app.activities;

import java.util.ArrayList;
import java.util.List;

import org.cleantalk.app.R;
import org.cleantalk.app.model.Site;

import android.app.ListActivity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class MainActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		List<Site> dummySites = new ArrayList<Site>();
		dummySites.add(new Site("Site 1", 0, 1, 1, 1, 1, 1, 1));
		dummySites.add(new Site("Site 2", 0, 1, 1, 1, 1, 1, 1));
		dummySites.add(new Site("Site 3", 0, 1, 1, 1, 1, 1, 1));

		setListAdapter(new SitesAdapter(this, dummySites));
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

}

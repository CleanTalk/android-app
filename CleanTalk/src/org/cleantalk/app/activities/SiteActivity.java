package org.cleantalk.app.activities;

import java.util.ArrayList;
import java.util.List;

import org.cleantalk.app.R;
import org.cleantalk.app.model.Request;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class SiteActivity extends ListActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_site);

		List<Request> dummyRequests = new ArrayList<Request>();
		dummyRequests.add(new Request(1, "c1e7028ad9f3fef5f729d31e232b7a89", true, "2014-03-01 07:31:06", "bowers.craig@gmail.com", "cbowers-test", "Post", "<a href=\"http://cleantalk.org\">Избався</a> от <b>спама</b>"));
		dummyRequests.add(new Request(1, "b2f79242fb6a6e3817e3b5148ffeb243", true, "2014-03-01 07:27:24", "bowers.craig@gmail.com", "cbowers-test", "Post", "<p>disappointed...Again</p>"));
		dummyRequests.add(new Request(1, "c1e7028ad9f3fef5f729d31e232b7a89", false, "2014-03-01 07:31:06", "bowers.craig@gmail.com", "cbowers-test", "Post", "<p>disappointed...</p>"));
		dummyRequests.add(new Request(1, "b2f79242fb6a6e3817e3b5148ffeb243", true, "2014-03-01 07:27:24", "bowers.craig@gmail.com", "cbowers-test", "Post", "<p>disappointed...Again</p>"));
		dummyRequests.add(new Request(1, "c1e7028ad9f3fef5f729d31e232b7a89", false, "2014-03-01 07:31:06", "bowers.craig@gmail.com", "cbowers-test", "Post", "<a href=\"http://cleantalk.org\">Избався</a> от <b>спама</b>"));
		dummyRequests.add(new Request(1, "b2f79242fb6a6e3817e3b5148ffeb243", true, "2014-03-01 07:27:24", "bowers.craig@gmail.com", "cbowers-test", "Post", "<p>disappointed...Again</p>"));

		setListAdapter(new RequestAdapter(this, dummyRequests));
		
		final TextView controlPanel = (TextView) findViewById(R.id.textViewControlPanel);
		SpannableString string = new SpannableString("Control panel");
		string.setSpan(new UnderlineSpan(), 0, string.length(), 0);
		controlPanel.setText(string);
		controlPanel.setTextColor(getResources().getColor(R.color.text_color));
		controlPanel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			}
		});
		
	}

	public class RequestAdapter extends BaseAdapter {
		private final Context context_;
		private final List<Request> items_;

		public RequestAdapter(Context context, List<Request> objects) {
			context_ = context;
			items_ = objects;
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

			holder.textViewMessage.setText(Html.fromHtml(request.getMessage()));
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

}

package org.cleantalk.app.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import org.cleantalk.app.R;
import org.cleantalk.app.api.ServiceApi;
import org.cleantalk.app.model.RequestModel;
import org.cleantalk.app.utils.Utils;
import org.json.JSONArray;

import java.lang.reflect.Field;
import java.util.List;
import java.util.TimeZone;

public class SiteActivity extends AppCompatActivity {

    protected static final String EXTRA_REQUEST_TYPE = "EXTRA_REQUEST_TYPE";
    protected static final String EXTRA_AUTH_KEY = "EXTRA_AUTH_KEY";
    protected static final String EXTRA_SITE_ID = "EXTRA_SITE_ID";
    protected static final String EXTRA_SITE_NAME = "EXTRA_SITE_NAME";
    protected static final String EXTRA_LAST_NOTIFIED = "EXTRA_LAST_NOTIFIED";

    private ServiceApi serviceApi_;
    private ListView listView_;

    private final Listener<JSONArray> responseListener_ = new Listener<JSONArray>() {
        @Override
        public void onResponse(JSONArray response) {
            loadRequests(response);
            hideProgress();
        }
    };

    private final Listener<RequestModel> sendFeedbackResponseListener_ = new Listener<RequestModel>() {
        @Override
        public void onResponse(RequestModel request) {
            adapter.updateItem(request);
        }
    };

    private final ErrorListener errorListener_ = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (error instanceof AuthFailureError) {
                startActivity(new Intent(SiteActivity.this, LoginActivity.class));
                finish();
            } else if (error instanceof NetworkError) {
                Toast toast_ = Utils.makeToast(SiteActivity.this, getString(R.string.connection_error), Utils.ToastType.Error);
                toast_.show();
            }
            hideProgress();
        }
    };

    private String siteId_;
    private int requestType_;
    private long lastNotified_;
    private long endTo_ = -1;
    private String authKey_;
    private RequestAdapter adapter;

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
        authKey_ = extras.getString(EXTRA_AUTH_KEY);

        if (siteId_ == null) {
            finish();
            return;
        }

        String title = extras.getString(EXTRA_SITE_NAME);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            if (title != null) {
                actionBar.setTitle(title);
            }
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
        TimeZone tz = ServiceApi.getInstance(this).getTimezone();
        long startFrom = -1L;
        int allow = -1;

        switch (requestType_) {
            case R.id.textViewTodayAllowed:
                startFrom = Utils.getTodayTimestamp(tz);
                allow = 1;
                break;
            case R.id.textViewTodayBlocked:
                startFrom = Utils.getTodayTimestamp(tz);
                allow = 0;
                break;
            case R.id.textViewWeekAllowed:
                startFrom = Utils.getWeekAgoTimestamp(tz);
                allow = 1;
                break;
            case R.id.textViewWeekBlocked:
                startFrom = Utils.getWeekAgoTimestamp(tz);
                allow = 0;
                break;
            case R.id.textViewYesterdayAllowed:
                startFrom = Utils.getYesterdayTimestamp(tz);
                endTo_ = Utils.getTodayTimestamp(tz);
                allow = 1;
                break;
            case R.id.textViewYesterdayBlocked:
                startFrom = Utils.getYesterdayTimestamp(tz);
                endTo_ = Utils.getTodayTimestamp(tz);
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
        private final List<RequestModel> items_;

        RequestAdapter(Context context, List<RequestModel> objects) {
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
        public RequestModel getItem(int position) {
            return items_.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View v = convertView;
            final ViewHolder holder; // to reference the child views for later actions

            if (v == null) {
                v = LayoutInflater.from(context_).inflate(R.layout.list_row_requests, parent, false);
                // cache view fields into the holder
                holder = new ViewHolder();
                holder.textViewTime = (TextView) v.findViewById(R.id.textViewTime);
                holder.textViewSender = (TextView) v.findViewById(R.id.textViewSender);
                holder.textViewType = (TextView) v.findViewById(R.id.textViewType);
                holder.textViewStatus = (TextView) v.findViewById(R.id.textViewStatus);
                holder.textViewMessage = (TextView) v.findViewById(R.id.textViewMessage);
                holder.buttonSpam = (Button) v.findViewById(R.id.buttonSpam);
                holder.textViewMarkedMessage = (TextView) v.findViewById(R.id.textViewMarkedMessage);

                // associate the holder with the view for later lookup
                v.setTag(holder);
            } else {
                // view already exists, get the holder instance from the view
                holder = (ViewHolder) v.getTag();
            }

            final RequestModel request = getItem(position);
            holder.textViewTime.setText(request.getDatetime());
            if (request.getSenderEmail().equals("null")) {
                holder.textViewSender.setText(request.getSenderNickname());
            } else {
                holder.textViewSender.setText(request.getSenderNickname() + " (" + request.getSenderEmail() + ")");
            }
            holder.textViewType.setText(request.getType());

            if (request.isAllow()) {
                holder.textViewStatus.setText(R.string.status_approved);
                holder.textViewStatus.setTextColor(ContextCompat.getColor(SiteActivity.this, R.color.allowed_count_label));
            } else {
                holder.textViewStatus.setText(R.string.status_forbidden);
                holder.textViewStatus.setTextColor(ContextCompat.getColor(SiteActivity.this, R.color.spam_count_label));
            }

            if (request.getMessage().equals("null")) {
                holder.textViewMessage.setVisibility(View.GONE);
            } else {
                holder.textViewMessage.setVisibility(View.VISIBLE);
                holder.textViewMessage.setText(Html.fromHtml(request.getMessage()));
            }

            if (request.getApproved() == 1) { // 0 - spam (not approved), 1 - not spam (approved)
                holder.buttonSpam.setText(R.string.spam);
                holder.textViewMarkedMessage.setText(R.string.marked_as_not_spam);
            } else {
                holder.buttonSpam.setText(R.string.not_spam);
                holder.textViewMarkedMessage.setText(R.string.marked_as_spam);
            }
            holder.textViewMarkedMessage.setVisibility(request.getShowApproved() ? View.VISIBLE : View.GONE);
            holder.buttonSpam.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDialog(
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    holder.buttonSpam.setEnabled(false);
                                    serviceApi_.sendFeedback(
                                            authKey_,
                                            request,
                                            sendFeedbackResponseListener_,
                                            errorListener_);
                                }
                            },
                            request.getApproved() == 1 ? R.string.mark_it_as_spam : R.string.mark_it_as_not_spam);
                }
            });
            holder.buttonSpam.setEnabled(true);
            return v;
        }

        void updateItem(RequestModel request) {
            for (int i = 0; i < items_.size(); i++) {
                if (items_.get(i).getRequestId().equals(request.getRequestId())) {
                    items_.set(i, request);
                    notifyDataSetChanged();
                    break;
                }
            }
        }

        // somewhere else in your class definition
        private class ViewHolder {
            TextView textViewTime;
            TextView textViewSender;
            TextView textViewType;
            TextView textViewStatus;
            TextView textViewMessage;
            Button buttonSpam;
            TextView textViewMarkedMessage;
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
        List<RequestModel> requests = Utils.parseRequests(this, response, endTo_);
        adapter = new RequestAdapter(this, requests);
        listView_.setAdapter(adapter);
    }

    public void showDialog(final DialogInterface.OnClickListener yesClickListener, @StringRes int messageTextResId) {
        new AlertDialog.Builder(this)
                .setMessage(messageTextResId)
                .setPositiveButton(R.string.yes, yesClickListener)
                .setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                }).show();
    }

}

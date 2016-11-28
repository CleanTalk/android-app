package org.cleantalk.app.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
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

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.NetworkImageView;

import org.cleantalk.app.MessagingService;
import org.cleantalk.app.R;
import org.cleantalk.app.api.ServiceApi;
import org.cleantalk.app.model.Site;
import org.cleantalk.app.utils.Utils;

import java.lang.reflect.Field;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static boolean foreground = false;

    private final Listener<List<Site>> responseListener_ = new Listener<List<Site>>() {
        @Override
        public void onResponse(List<Site> sites) {
            loadSites(sites);
            hideProgress();
            listView_.setSelectionFromTop(list_index, list_top);
        }
    };

    private final ErrorListener errorListener_ = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            if (error instanceof AuthFailureError) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            } else if (error instanceof NetworkError) {
                toast_ = Utils.makeToast(MainActivity.this, getString(R.string.connection_error), Utils.ToastType.Error);
                toast_.show();
            }
            hideProgress();
        }
    };

    private final BroadcastReceiver updateReceiver_ = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            showProgress();
            serviceApi_.requestServices(responseListener_, errorListener_);
        }
    };

    private ServiceApi serviceApi_;
    private ListView listView_;
    private boolean doubleBackToExitPressedOnce_;
    private Toast toast_;
    private int list_index;
    private int list_top;

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
    }

    @Override
    protected void onResume() {
        showProgress();
        serviceApi_.requestServices(responseListener_, errorListener_);
        IntentFilter filter = new IntentFilter(MessagingService.ACTION_UPDATE);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(updateReceiver_, filter);
        foreground = true;
        super.onResume();
        listView_.setSelectionFromTop(list_index, list_top);
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(updateReceiver_);
        foreground = false;
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

    private void loadSites(List<Site> sites) {
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
                holder.textViewNew = (TextView) v.findViewById(R.id.textViewNew);
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

            int today = site.getTodayAllowed();
            int notified = getTodayNotified(site.getSiteId());
            if (today - notified > 0) {
                holder.textViewNew.setText(String.valueOf(today - notified));
                holder.textViewNew.setVisibility(View.VISIBLE);
                holder.textViewSiteName.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MainActivity.this, SiteActivity.class);
                        intent.putExtra(SiteActivity.EXTRA_REQUEST_TYPE, v.getId());
                        intent.putExtra(SiteActivity.EXTRA_SITE_NAME, site.getSiteName());
                        intent.putExtra(SiteActivity.EXTRA_SITE_ID, site.getSiteId());
                        intent.putExtra(SiteActivity.EXTRA_AUTH_KEY, site.getAuthKey());
                        intent.putExtra(SiteActivity.EXTRA_LAST_NOTIFIED, setTodayNotified(site.getSiteId(), site.getTodayAllowed()));
                        startActivity(intent);
                    }
                });
            } else {
                holder.textViewNew.setVisibility(View.GONE);
                holder.textViewSiteName.setOnClickListener(null);

            }

            OnClickListener onCountClickListener = new OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, SiteActivity.class);

                    list_index = listView_.getFirstVisiblePosition();
                    View view = listView_.getChildAt(0);
                    list_top = (view == null) ? 0 : (v.getTop() - listView_.getPaddingTop());

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
                            intent.putExtra(SiteActivity.EXTRA_AUTH_KEY, site.getAuthKey());
                            setTodayNotified(site.getSiteId(), site.getTodayAllowed());
                            startActivity(intent);
                        default:
                            return;
                    }
                }
            };

            if (site.getTodayAllowed() > 0) {
                holder.textViewTodayAllowed.setOnClickListener(onCountClickListener);
            }
            if (site.getTodayBlocked() > 0) {
                holder.textViewTodayBlocked.setOnClickListener(onCountClickListener);
            }
            if (site.getYesterdayAllowed() > 0) {
                holder.textViewYesterdayAllowed.setOnClickListener(onCountClickListener);
            }
            if (site.getYesterdayBlocked() > 0) {
                holder.textViewYesterdayBlocked.setOnClickListener(onCountClickListener);
            }
            if (site.getWeekAllowed() > 0) {
                holder.textViewWeekAllowed.setOnClickListener(onCountClickListener);
            }
            if (site.getWeekBlocked() > 0) {
                holder.textViewWeekBlocked.setOnClickListener(onCountClickListener);
            }

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
            TextView textViewNew;
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
                ServiceApi.getInstance(this).logout();
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
            toast_.cancel();
            toast_ = null;
            return;
        }
        this.doubleBackToExitPressedOnce_ = true;
        toast_ = Utils.makeToast(this, getString(R.string.click_back_again_to_exit), Utils.ToastType.Info);
        toast_.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce_ = false;
            }
        }, 2000);
    }

    private long setTodayNotified(String siteId, int todayAllowedNotified) {
        long time = Utils.getTimestamp(this);
        getPreferences(MODE_PRIVATE)
                .edit()
                .putInt("notified" + siteId, todayAllowedNotified)
                .putLong("time" + siteId, time)
                .apply();
        return time;
    }

    private int getTodayNotified(String siteId) {
        SharedPreferences pref = getPreferences(MODE_PRIVATE);
        int notified = pref.getInt("notified" + siteId, -1);
        long time = pref.getLong("time" + siteId, -1);
        if ((Utils.getTimestamp(this) - time) < 86400000) {
            return notified;
        } else {
            return 0;
        }
    }
}

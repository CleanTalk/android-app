package org.cleantalk.app.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.cleantalk.app.R;
import org.cleantalk.app.api.ServiceApi;

public class SplashActivity extends Activity {

    /**
     * Timer for showing splash screen. App will open login activity when timer will finish its work
     */
    public class TimerTimeTask extends AsyncTask<Void, Void, Void> {

        public static final int TIMER_TIME = 2000;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(TIMER_TIME);
            } catch (InterruptedException e) {
                Log.e(this.getClass().getName(), e.toString());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            Intent intent = new Intent(SplashActivity.this,
                    ServiceApi.getInstance(SplashActivity.this).isSessionExist() ? MainActivity.class : LoginActivity.class);
            startActivity(intent);
            finish();
        }
    }

    public static final int TIMER_TIME = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        // Create timer for showing of splash screen
        TimerTimeTask timerTimeTask = new TimerTimeTask();
        timerTimeTask.execute();
    }

}

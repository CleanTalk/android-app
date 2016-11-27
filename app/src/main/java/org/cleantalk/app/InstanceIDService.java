package org.cleantalk.app;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import org.cleantalk.app.utils.Preferences;

public class InstanceIDService extends FirebaseInstanceIdService {

    private static final String TAG = "FCM";

    @Override
    public void onTokenRefresh() {
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d(TAG, "Refreshed token: " + refreshedToken);

        Preferences.setFcmToken(this, refreshedToken);
    }

}

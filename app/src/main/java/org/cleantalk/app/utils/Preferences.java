package org.cleantalk.app.utils;

import android.content.Context;
import android.preference.PreferenceManager;

public class Preferences {

    public static final String FCM_TOKEN = "FCM_TOKEN";

    public static String getFcmToken(Context context) {
        return PreferenceManager
                .getDefaultSharedPreferences(context)
                .getString(FCM_TOKEN, "");
    }

    public static void setFcmToken(Context context, String token) {
        PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putString(FCM_TOKEN, token)
                .apply();
    }

}

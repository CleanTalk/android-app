package org.cleantalk.app.api;

import java.util.TimeZone;

import org.cleantalk.app.R;
//import org.cleantalk.app.gcm.GcmSenderIdRecieverTask;
import org.cleantalk.app.utils.Utils;
import org.json.JSONArray;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.text.TextUtils;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

public class ServiceApi {
	private static final String PROPERTY_SESSION = "PROPERTY_SESSION";
	private static final String PROPERTY_TIMEZONE = "PROPERTY_TIMEZONE";

	private static final String HOST = "https://cleantalk.org";
	private static final String AUTH_URI = HOST + "/my/session?app_mode=1";
	private static final String SERVICES_URI = HOST + "/my/main?app_mode=1";
	private static final String REQUESTS_URI = HOST + "/my/show_requests?app_mode=1";

	private static ServiceApi serviceApi_;

	public static ServiceApi getInstance(Context context) {
		if (serviceApi_ == null) {
			serviceApi_ = new ServiceApi(context);
		}
		return serviceApi_;
	}

	private final RequestQueue requestQueue_;
	private final Context context_;
	private final ImageLoader imageLoader_;

	public ServiceApi(Context context) {
		context_ = context;
		requestQueue_ = Volley.newRequestQueue(context_);
		imageLoader_ = new ImageLoader(requestQueue_, new ImageLoader.ImageCache() {
			private final LruCache<String, Bitmap> mCache = new LruCache<String, Bitmap>(5);

			@Override
			public void putBitmap(String url, Bitmap bitmap) {
				mCache.put(url, bitmap);
			}

			@Override
			public Bitmap getBitmap(String url) {
				return mCache.get(url);
			}
		});
	}

	public boolean isSessionExist() {
		return !TextUtils.isEmpty(getAppSessionId());
	}

	public ImageLoader getImageLoader() {
		return imageLoader_;
	}

	public void authenticate(String login, String pass, String appSenderId, final Listener<Boolean> listener,
			final ErrorListener errorListener) {
		AuthRequest request = new AuthRequest(AUTH_URI, login, pass, appSenderId, Utils.getDeviceId(context_), new Listener<String>() {
			@Override
			public void onResponse(String response) {
				if (!TextUtils.isEmpty(response)) {
					setAppSessionId(response);
					listener.onResponse(true);
				} else {
					VolleyError error = new AuthFailureError(context_.getString(R.string.auth_error));
					errorListener.onErrorResponse(error);
				}
			}
		}, errorListener);
		requestQueue_.add(request);
	}

	public void requestServices(Listener<JSONArray> listener, ErrorListener errorListener) {
		String appSessionId = getAppSessionId();
		if (TextUtils.isEmpty(appSessionId)) {
			AuthFailureError error = new AuthFailureError(context_.getString(R.string.auth_error));
			errorListener.onErrorResponse(error);
			return;
		}
		ServicesRequest request = new ServicesRequest(context_, SERVICES_URI, appSessionId, listener, errorListener);
		requestQueue_.add(request);
	}

	public void requestRequests(String siteId, long startFrom, int allow, Listener<JSONArray> listener, ErrorListener errorListener) {
		String appSessionId = getAppSessionId();
		if (TextUtils.isEmpty(appSessionId)) {
			AuthFailureError error = new AuthFailureError(context_.getString(R.string.auth_error));
			errorListener.onErrorResponse(error);
			return;
		}
		RequestsRequest request = new RequestsRequest(context_, REQUESTS_URI, appSessionId, siteId, startFrom, allow, listener,
				errorListener);
		requestQueue_.add(request);
	}

	private SharedPreferences getPreferences() {
		return context_.getSharedPreferences(ServiceApi.class.getSimpleName(), Context.MODE_PRIVATE);
	}

	private String getAppSessionId() {
		String appSessionId = getPreferences().getString(PROPERTY_SESSION, null);
		String initialisationVector = Utils.getDeviceId(context_);
		appSessionId = Utils.AES128Decrypt(appSessionId, initialisationVector, initialisationVector);
		return appSessionId;
	}

	private void setAppSessionId(String appSessionId) {
		if (appSessionId == null) {
			getPreferences().edit().remove(PROPERTY_SESSION).commit();
		} else {
			String initialisationVector = Utils.getDeviceId(context_);
			String eAppSessionId = Utils.AES128Encrypt(appSessionId, initialisationVector, initialisationVector);
			getPreferences().edit().putString(PROPERTY_SESSION, eAppSessionId).commit();
		}
	}

	public TimeZone getTimezone() {
		String timezone = getPreferences().getString(PROPERTY_TIMEZONE, null);
		if (timezone != null) {
			return TimeZone.getTimeZone(timezone);
		} else {
			return TimeZone.getDefault();
		}
	}

	public void setTimezone(TimeZone timezone) {
		getPreferences().edit().putString(PROPERTY_TIMEZONE, timezone.getID()).commit();
	}

	public void logout() {
		setAppSessionId(null);
//		GcmSenderIdRecieverTask.clearRegistrationId(context_);
	}
}

package org.cleantalk.app.api;

import android.text.TextUtils;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

class AuthRequest extends Request<String> {

    private static final int RESULT_SUCCESS = 1;
    private static final String REQUEST_PARAMS_LOGIN = "login";
    private static final String REQUEST_PARAMS_PASSWORD = "password";
    private static final String REQUEST_PARAMS_DEVICE_TOKEN = "app_device_token";
    private static final String REQUEST_PARAMS_APP_SENDER_ID = "app_sender_id";
    private static final String RESULT_FIELD_SUCCESS = "success";
    private static final String RESULT_FIELD_APP_SESSION_ID = "app_session_id";

    private final HashMap<String, String> params = new HashMap<>(4);
    private final Listener<String> listener;

    public AuthRequest(String url,
                       String login,
                       String password,
                       String appSenderId,
                       String deviceId,
                       Listener<String> listener,
                       ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.listener = listener;
        params.put(REQUEST_PARAMS_LOGIN, login);
        params.put(REQUEST_PARAMS_PASSWORD, password);
        params.put(REQUEST_PARAMS_DEVICE_TOKEN, deviceId);
        if (!TextUtils.isEmpty(appSenderId)) {
            params.put(REQUEST_PARAMS_APP_SENDER_ID, appSenderId);
        }
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject result = new JSONObject(jsonString);
            int resultCode = result.getInt(RESULT_FIELD_SUCCESS);
            String appSessionId = null;
            if (resultCode == RESULT_SUCCESS) {
                appSessionId = result.getString(RESULT_FIELD_APP_SESSION_ID);
            }
            return Response.success(appSessionId, HttpHeaderParser.parseCacheHeaders(response));
        } catch (UnsupportedEncodingException e) {
            return Response.error(new ParseError(e));
        } catch (JSONException je) {
            return Response.error(new ParseError(je));
        }
    }

    @Override
    public HashMap<String, String> getParams() {
        return params;
    }

    @Override
    protected void deliverResponse(String response) {
        listener.onResponse(response);
    }
}

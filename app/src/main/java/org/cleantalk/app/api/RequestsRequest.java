package org.cleantalk.app.api;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

import org.cleantalk.app.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

class RequestsRequest extends Request<JSONArray> {

    private static final int RESULT_SUCCESS = 1;
    private static final String REQUEST_PARAMS_APP_SESSION_ID = "app_session_id";
    private static final String REQUEST_PARAMS_SERVICE_ID = "service_id";
    private static final String REQUEST_PARAMS_START_FROM = "start_from";
    private static final String REQUEST_PARAMS_ALLOW = "allow";

    private static final String RESULT_FIELD_AUTH = "auth";
    private static final String RESULT_FIELD_REQUESTS = "requests";


    private final HashMap<String, String> params = new HashMap<>(4);
    private final Listener<JSONArray> listener;
    private final Context context;

    public RequestsRequest(Context context,
                           String url,
                           String appSessionId,
                           String siteId,
                           long startFrom,
                           int allow,
                           Listener<JSONArray> listener,
                           ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.listener = listener;
        this.context = context;

        params.put(REQUEST_PARAMS_APP_SESSION_ID, appSessionId);
        params.put(REQUEST_PARAMS_SERVICE_ID, siteId);
        params.put(REQUEST_PARAMS_START_FROM, String.valueOf(startFrom));
        params.put(REQUEST_PARAMS_ALLOW, String.valueOf(allow));
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject result = new JSONObject(jsonString);
            int resultCode = result.getInt(RESULT_FIELD_AUTH);
            if (resultCode == RESULT_SUCCESS) {
                return Response.success(
                        result.getJSONArray(RESULT_FIELD_REQUESTS),
                        HttpHeaderParser.parseCacheHeaders(response));
            } else {
                AuthFailureError error = new AuthFailureError(context.getString(R.string.auth_error));
                return Response.error(error);
            }
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
    protected void deliverResponse(JSONArray response) {
        listener.onResponse(response);
    }

}

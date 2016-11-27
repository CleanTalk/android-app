package org.cleantalk.app.api;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import org.cleantalk.app.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.TimeZone;

class ServicesRequest extends Request<JSONArray> {

    private static final int RESULT_SUCCESS = 1;
    private static final String REQUEST_PARAMS_APP_SESSION_ID = "app_session_id";

    private final Listener<JSONArray> listener;
    private final HashMap<String, String> params = new HashMap<>(1);
    private final Context context;

    public ServicesRequest(Context context,
                           String url,
                           String appSessionId,
                           Listener<JSONArray> listener,
                           ErrorListener errorListener) {
        super(Method.POST, url, errorListener);
        this.listener = listener;
        this.context = context;
        params.put(REQUEST_PARAMS_APP_SESSION_ID, appSessionId);
    }

    @Override
    protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject result = new JSONObject(jsonString);
            int resultCode = result.getInt("auth");
            if (resultCode == RESULT_SUCCESS) {
                String tz = result.getString("timezone");
                ServiceApi.getInstance(context).setTimezone(TimeZone.getTimeZone("GMT" + tz));
                return Response.success(result.getJSONArray("services"), HttpHeaderParser.parseCacheHeaders(response));
            } else {
                VolleyError error = new VolleyError(context.getString(R.string.auth_error));
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

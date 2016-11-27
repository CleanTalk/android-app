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
import org.cleantalk.app.model.Site;
import org.cleantalk.app.utils.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

class ServicesRequest extends Request<List<Site>> {

    private static final int RESULT_SUCCESS = 1;
    private static final String REQUEST_PARAMS_APP_SESSION_ID = "app_session_id";
    private static final String SERVICES_URI = ServiceApi.HOST + "/my/main?app_mode=1";

    private final Listener<List<Site>> listener;
    private final HashMap<String, String> params = new HashMap<>(2);
    private final Context context;

    public ServicesRequest(Context context,
                           String appSessionId,
                           int pageNumber,
                           Listener<List<Site>> listener,
                           ErrorListener errorListener) {
        super(Method.POST, SERVICES_URI + "&service_page=" + String.valueOf(pageNumber), errorListener);
        this.listener = listener;
        this.context = context;
        params.put(REQUEST_PARAMS_APP_SESSION_ID, appSessionId);
    }

    @Override
    protected Response<List<Site>> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject result = new JSONObject(jsonString);
            int resultCode = result.getInt("auth");
            if (resultCode == RESULT_SUCCESS) {
                String tz = result.getString("timezone");
                ServiceApi.getInstance(context).setTimezone(TimeZone.getTimeZone("GMT" + tz));

                List<Site> sites = Utils.parseSites(result.getJSONArray("services"));
                return Response.success(sites, HttpHeaderParser.parseCacheHeaders(response));
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
    protected void deliverResponse(List<Site> response) {
        listener.onResponse(response);
    }
}

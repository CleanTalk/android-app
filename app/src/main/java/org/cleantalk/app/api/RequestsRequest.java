package org.cleantalk.app.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.message.BasicNameValuePair;
import org.cleantalk.app.R;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

public class RequestsRequest extends Request<JSONArray> {

	private static final int RESULT_SUCCESS = 1;
	private final Listener<JSONArray> listener_;
	private final Context context_;
//	private UrlEncodedFormEntity entity_;

	public RequestsRequest(Context context, String url, String appSessionId, String siteId, long startFrom, int allow,
			Listener<JSONArray> listener, ErrorListener errorListener) {
		super(Method.POST, url, errorListener);
		listener_ = listener;
		context_ = context;
//		List<BasicNameValuePair> values = new ArrayList<BasicNameValuePair>();
//		values.add(new BasicNameValuePair("app_session_id", appSessionId));
//		values.add(new BasicNameValuePair("service_id", siteId));
//		values.add(new BasicNameValuePair("start_from", String.valueOf(startFrom)));
//		values.add(new BasicNameValuePair("allow", String.valueOf(allow)));
//		try {
//			entity_ = new UrlEncodedFormEntity(values, "UTF-8");
//		} catch (UnsupportedEncodingException e) {
//			e.printStackTrace();
//		}
	}

	@Override
	protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
		try {
			String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
			JSONObject result = new JSONObject(jsonString);
			int resultCode = result.getInt("auth");
			if (resultCode == RESULT_SUCCESS) {
				return Response.success(result.getJSONArray("requests"), HttpHeaderParser.parseCacheHeaders(response));
			} else {
				AuthFailureError error = new AuthFailureError(context_.getString(R.string.auth_error));
				return Response.error(error);
			}
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JSONException je) {
			return Response.error(new ParseError(je));
		}
	}

	@Override
	public byte[] getBody() {
		byte[] array = null;
//		try {
//			array = new byte[entity_.getContent().available()];
//			entity_.getContent().read(array);
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
		return array;
	}

	@Override
	protected void deliverResponse(JSONArray response) {
		listener_.onResponse(response);
	}
}

package org.cleantalk.app.api;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.text.TextUtils;

import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;

public class AuthRequest extends Request<String> {

	private static final int RESULT_SUCCESS = 1;
	private UrlEncodedFormEntity entity_;
	private final Listener<String> listener_;

	public AuthRequest(String url, String login, String password, String deviceId, String appSenderId, Listener<String> listener,
			ErrorListener errorListener) {
		super(Method.POST, url, errorListener);
		listener_ = listener;
		List<BasicNameValuePair> values = new ArrayList<BasicNameValuePair>();
		values.add(new BasicNameValuePair("login", login));
		values.add(new BasicNameValuePair("password", password));
		values.add(new BasicNameValuePair("app_device_token", deviceId));
		if(!TextUtils.isEmpty(appSenderId)){
			// We send GCM sender id if user has that kind of service
			values.add(new BasicNameValuePair("app_sender_id", appSenderId));
		}
		try {
			entity_ = new UrlEncodedFormEntity(values, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected Response<String> parseNetworkResponse(NetworkResponse response) {
		try {
			String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
			JSONObject result = new JSONObject(jsonString);
			int resultCode = result.getInt("success");
			String appSessionId = null;
			if(resultCode == RESULT_SUCCESS){
				appSessionId = result.getString("app_session_id");
			}
			return Response.success(appSessionId, HttpHeaderParser.parseCacheHeaders(response));
		} catch (UnsupportedEncodingException e) {
			return Response.error(new ParseError(e));
		} catch (JSONException je) {
			return Response.error(new ParseError(je));
		}
	}

	@Override
	public byte[] getBody() {
		byte[] array = null;
		try {
			array = new byte[entity_.getContent().available()];
			entity_.getContent().read(array);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return array;
	}

	@Override
	protected void deliverResponse(String response) {
		listener_.onResponse(response);
	}
}

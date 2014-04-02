package com.example.gcmsendingtest;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.Response.Listener;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.Volley;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private UserLoginTask mAuthTask;
	private String appRegistrationId_;
	private RequestQueue requestQueue_;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		requestQueue_ = Volley.newRequestQueue(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void onGetSenderClick(View v) {
		mAuthTask = new UserLoginTask(this);
		mAuthTask.execute((Void) null);
	}

	public void onLoginClick(View v) {

	}

	public void onSendMessageClick(View v) {
		String request = "{\"data\": {\"title\":\"" + ((EditText) findViewById(R.id.editTextTitle)).getText().toString()
				+ "\",\"message\":\"" + ((EditText) findViewById(R.id.editTextMessage)).getText().toString()
				+ "\"},\"registration_ids\": [\"" + appRegistrationId_ + "\"]}";
		JsonRequest<String> a = new JsonRequest<String>(Method.POST, "https://android.googleapis.com/gcm/send", request,
				new Listener<String>() {
					@Override
					public void onResponse(String request) {
						TextView s = (TextView) findViewById(R.id.textViewSendNotificationRequest);
						s.setText(request);
					}
				}, null) {
			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				Map<String, String> headers = new HashMap<String, String>();
				headers.put("Authorization", "key=AIzaSyBpF5UAAfZwacXIqsrsQlsErKfrfWd0kWU");
				headers.put("Content-Type", "application/json");
				return headers;
			}

			@Override
			protected Response<String> parseNetworkResponse(NetworkResponse response) {
				String jsonString = "none";
				try {
					jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return Response.success(jsonString, HttpHeaderParser.parseCacheHeaders(response));
			}
		};

		requestQueue_.add(a);
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate the user.
	 */
	public class UserLoginTask extends GcmSenderIdRecieverTask {

		public UserLoginTask(Context context) {
			super(context);
		}

		@Override
		protected void onPostExecute(final String appSenderId) {
			appRegistrationId_ = appSenderId;
			EditText aa = (EditText) findViewById(R.id.editTextRegistrationId);
			aa.setText(appSenderId);
		}

		@Override
		protected void onCancelled() {
		}
	}

}

package org.cleantalk.app.activities;

import org.cleantalk.app.R;
import org.cleantalk.app.api.ServiceApi;
import org.cleantalk.app.gcm.GcmSenderIdRecieverTask;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class LoginActivity extends Activity {

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private TextView mLoginStatusMessageView;

	private ServiceApi serviceApi_;
	private boolean isPlayservicesDeclined_ = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		serviceApi_ = ServiceApi.getInstance(this);
		mEmailView = (EditText) findViewById(R.id.email);
		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
				if (id == R.id.login || id == EditorInfo.IME_NULL) {
					attemptLogin();
					return true;
				}
				return false;
			}
		});

		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		findViewById(R.id.sign_in_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				attemptLogin();
			}
		});
	}

	/**
	 * Attempts to sign in or register the account specified by the login form. If there are form errors (invalid email, missing fields,
	 * etc.), the errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (!isPlayservicesDeclined_ && !checkPlayServices()) {
			return;
		}

		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		}

		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			InputMethodManager imm = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(mPasswordView.getWindowToken(), 0);
			mAuthTask = new UserLoginTask(this);
			mAuthTask.execute((Void) null);
		}
	}

	private void showProgress(final boolean show) {
		((ViewSwitcher) findViewById(R.id.viewSwitcher)).setDisplayedChild(show ? 1 : 0);
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
			serviceApi_.authenticate(mEmail, mPassword, appSenderId, authResultListener, authErrorListener);
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it doesn't, display a dialog that allows users to download the
	 * APK from the Google Play Store or enable it in the device's system settings.
	 */
	private boolean checkPlayServices() {
		int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST, new OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						declinePlayService();
					}
				}).show();
			}
			return false;
		}
		return true;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case PLAY_SERVICES_RESOLUTION_REQUEST:
			if (resultCode == RESULT_CANCELED) {
				declinePlayService();
			}
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private Listener<Boolean> authResultListener = new Listener<Boolean>() {
		@Override
		public void onResponse(Boolean success) {
			mAuthTask = null;
			if (success) {
				Intent intent = new Intent(LoginActivity.this, MainActivity.class);
				startActivity(intent);
				finish();
			} else {
				showProgress(false);
				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}
	};

	private ErrorListener authErrorListener = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			mAuthTask = null;
			showProgress(false);
			if (error instanceof NoConnectionError) {
				Toast.makeText(LoginActivity.this, getString(R.string.connection_error), Toast.LENGTH_LONG).show();
			} else 
				if (error instanceof AuthFailureError) {
				mPasswordView.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			} else {
				Toast.makeText(LoginActivity.this, error.getMessage(), Toast.LENGTH_LONG).show();
			}
		}
	};

	private void declinePlayService(){
		isPlayservicesDeclined_ = true;
		Toast.makeText(this, R.string.play_services_required, Toast.LENGTH_LONG).show();
	}
}

package org.cleantalk.app.activities;

import org.cleantalk.app.R;
import org.cleantalk.app.api.ServiceApi;
import org.cleantalk.app.utils.Preferences;
import org.cleantalk.app.utils.Utils;

import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.android.volley.AuthFailureError;
import com.android.volley.NoConnectionError;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnEditorAction;

/**
 * Activity which displays a login screen to the user, offering registration as well.
 */
public class LoginActivity extends Activity {

    @BindView(R.id.email) EditText emailView;
    @BindView(R.id.password) EditText passwordView;
    @BindView(R.id.login_status_message) TextView loginStatusMessageView;
    @BindView(R.id.viewSwitcher) ViewSwitcher viewSwitcher;

    private ServiceApi serviceApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        serviceApi = ServiceApi.getInstance(this);
    }

    @OnEditorAction(R.id.password)
    public boolean onPasswordEditorAction(TextView textView, int id, KeyEvent keyEvent) {
        if (id == R.id.login || id == EditorInfo.IME_NULL) {
            attemptLogin();
            return true;
        }
        return false;
    }

    @OnClick(R.id.sign_in_button)
    public void onSignInClick() {
        attemptLogin();
    }

    /**
     * Attempts to sign in or register the account specified by the login form. If there are form errors (invalid email, missing fields,
     * etc.), the errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {

        // Reset errors.
        emailView.setError(null);
        passwordView.setError(null);

        // Store values at the time of the login attempt.
        String email = emailView.getText().toString();
        String password = passwordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
            focusView = passwordView;
            cancel = true;
        }

        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
            focusView = emailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            loginStatusMessageView.setText(R.string.login_progress_signing_in);
            showProgress(true);
            InputMethodManager imm = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(passwordView.getWindowToken(), 0);
            serviceApi.authenticate(email, password, Preferences.getFcmToken(this), authResultListener, authErrorListener);
        }
    }

    private void showProgress(final boolean show) {
        viewSwitcher.setDisplayedChild(show ? 1 : 0);
    }

    private final Listener<Boolean> authResultListener = new Listener<Boolean>() {

        @Override
        public void onResponse(Boolean success) {
            if (success) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                showProgress(false);
                passwordView.setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
            }
        }
    };

    private final ErrorListener authErrorListener = new ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
            showProgress(false);
            if (error instanceof NoConnectionError) {
                Utils.makeToast(LoginActivity.this, getString(R.string.connection_error), Utils.ToastType.Error).show();
            } else if (error instanceof AuthFailureError) {
                passwordView.setError(getString(R.string.error_incorrect_password));
                passwordView.requestFocus();
            } else {
                Utils.makeToast(LoginActivity.this, error.getMessage(), Utils.ToastType.Error).show();
            }
        }
    };

}

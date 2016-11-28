package org.cleantalk.app.api;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Response;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import org.cleantalk.app.R;
import org.cleantalk.app.model.RequestModel;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

class SendFeedbackRequest extends com.android.volley.Request<RequestModel> {

    private static final String RESULT_SUCCESS = "Ok.";
    private static final String SERVICES_URI = "https://moderate.cleantalk.org/api2.0";
    protected static final String PROTOCOL_CHARSET = "utf-8";

    private final Listener<RequestModel> listener;
    private final Context context;
    private final String mRequestBody;
    private RequestModel request;

    public SendFeedbackRequest(Context context,
                               String authKey,
                               RequestModel request,
                               Listener<RequestModel> listener,
                               ErrorListener errorListener) {
        super(Method.POST, SERVICES_URI, errorListener);
        this.listener = listener;
        this.context = context;

        HashMap<String, String> params = new HashMap<>(2);

        this.request = request;
        params.put("method_name", "send_feedback");
        params.put("auth_key", authKey);
        params.put("feedback", request.getRequestId()
                + ":"
                + (request.getApproved() == 1 ? 0 : 1));

        mRequestBody = (new JSONObject(params)).toString();
        Log.d("!!!", mRequestBody);
//        {
//            "method_name": "send_feedback",
//                "auth_key": "qyrasequ8usy",
//                "feedback": "3c9f43547b3bcd2e6b1b611d4a9b9828:1"
//        }
    }

    @Override
    protected Response<RequestModel> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject result = new JSONObject(jsonString);
            String comment = result.getString("comment");
            if (RESULT_SUCCESS.equals(comment)) {
//                {
//                    "comment": "Ok.",
//                        "received": 1
//                }
                Log.d("!!!", result.toString());
                request = new RequestModel(request.getRequestId(),
                        request.isAllow(),
                        request.getDatetime(),
                        request.getSenderEmail(),
                        request.getSenderNickname(),
                        request.getType(),
                        true,
                        request.getMessage(),
                        request.getApproved() == 1 ? 0 : 1);
                return Response.success(request, HttpHeaderParser.parseCacheHeaders(response));
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
    public byte[] getBody() throws AuthFailureError {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }

    @Override
    protected void deliverResponse(RequestModel response) {
        listener.onResponse(response);
    }
}

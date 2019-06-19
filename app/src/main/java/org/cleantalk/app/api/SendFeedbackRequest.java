package org.cleantalk.app.api;

import android.content.Context;
import com.android.volley.*;
import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
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
    private Integer approved;

    public SendFeedbackRequest(Context context,
                               String authKey,
                               RequestModel request,
                               Listener<RequestModel> listener,
                               ErrorListener errorListener) {
        super(Method.POST, SERVICES_URI, errorListener);
        this.listener = listener;
        this.context = context;
        this.request = request;

        HashMap<String, String> params = new HashMap<>(3);
        params.put("method_name", "send_feedback");
        params.put("auth_key", authKey);


        // 0 - spam (not approved), 1 - not spam (approved), -1 - not moderated
        // 0 - spam (not approved), 1 - not spam (approved), -1 - not moderated
        if (request.getApproved() == 1) {
            if (request.isInProgress()) {
                approved = 1;
            } else {
                approved = 0;
            }
        } else if (request.getApproved() == 0) {
            if (request.isInProgress()) {
                approved = 0;
            } else {
                approved = 1;
            }
        } else {
            if (request.isAllow()) {
                approved = 0;
            } else {
                approved = 1;
            }
        }
        params.put("feedback", request.getRequestId() + ":" + approved);
        mRequestBody = (new JSONObject(params)).toString();
    }

    @Override
    protected Response<RequestModel> parseNetworkResponse(NetworkResponse response) {
        try {
            String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
            JSONObject result = new JSONObject(jsonString);
            String comment = result.getString("comment");
            if (RESULT_SUCCESS.equals(comment)) {
                request = new RequestModel(request.getRequestId(),
                        request.isAllow(),
                        request.getDatetime(),
                        request.getSenderEmail(),
                        request.getSenderNickname(),
                        request.getType(),
                        true,
                        true,
                        request.getMessage(),
                        approved == 1 ? 0 : 1);
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

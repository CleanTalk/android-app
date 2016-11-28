package org.cleantalk.app.model;

public class RequestModel {
    private final String requestId_;
    private final boolean allow_;
    private final String datetime_;
    private final String senderEmail_;
    private final String senderNickname_;
    private final String type_;
    private final String message_;
    private final boolean show_approved_;
    private final int approved_;

    public RequestModel(String requestId,
                        boolean allow,
                        String datetime,
                        String senderEmail,
                        String senderNickname,
                        String type,
                        boolean show_approved,
                        String message,
                        int approved) {
        requestId_ = requestId;
        allow_ = allow;
        datetime_ = datetime;
        senderEmail_ = senderEmail;
        senderNickname_ = senderNickname;
        type_ = type;
        show_approved_ = show_approved;
        message_ = message;
        approved_ = approved;
    }

    public String getRequestId() {
        return requestId_;
    }

    public boolean isAllow() {
        return allow_;
    }

    public String getDatetime() {
        return datetime_;
    }

    public String getSenderEmail() {
        return senderEmail_;
    }

    public String getSenderNickname() {
        return senderNickname_;
    }

    public String getType() {
        return type_;
    }

    public String getMessage() {
        return message_;
    }

    public boolean getShowApproved() {
        return show_approved_;
    }

    public int getApproved() {
        return approved_;
    }
}

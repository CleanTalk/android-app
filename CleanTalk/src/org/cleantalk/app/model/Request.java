package org.cleantalk.app.model;

public class Request {
	private final String requestId_;
	private final boolean allow_;
	private final String datetime_;
	private final String senderEmail_;
	private final String senderNickname_;
	private final String type_;
	private final String message_;

	public Request(String requestId, boolean allow, String datetime, String senderEmail, String senderNickname, String type, String message) {
		requestId_ = requestId;
		allow_ = allow;
		datetime_ = datetime;
		senderEmail_ = senderEmail;
		senderNickname_ = senderNickname;
		type_ = type;
		message_ = message;
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

}

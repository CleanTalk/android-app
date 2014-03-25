package org.cleantalk.app.provider;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public class Contract {
	/** Content provider authority. */
	public static final String CONTENT_AUTHORITY = "org.cleantalk.app.provider.cleantalkprovider";

	/** Base URI. (content://org.cleantalk.app.provider.cleantalkprovider) */
	public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
	
	/** Path component for "site"-type resources.. */
	private static final String PATH_SITES = "sites";

	/** Path component for "request"-type resources.. */
	private static final String PATH_REQUESTS = "requests";

	/** Columns supported by "sites" records. */
	public static class Sites implements BaseColumns {
		/** MIME type for lists of entries. */
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.cleantalkprovider.sites";
		/** MIME type for individual entries. */
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.cleantalkprovider.site";
		/** Fully qualified URI for "site" resources. */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_SITES).build();
		/** Table name where records are stored for "site" resources. */
		public static final String TABLE_NAME = "sites";

		/** Table columns */
		public static final String COLUMN_NAME_SERVICE_ID = "service_id";
		public static final String COLUMN_NAME_SERVICENAME = "servicename";
		public static final String COLUMN_NAME_HOSTNAME = "hostname";
		public static final String COLUMN_NAME_FAVICON_URL = "favicon_url";
		public static final String COLUMN_NAME_LAST_NEW_NOTIFIED_TIME = "last_new_notified_time";

		public static final String COLUMN_NAME_TODAY_BLOCKED = "today_blocked";
		public static final String COLUMN_NAME_TODAY_ALLOWED = "today_allowed";
		public static final String COLUMN_NAME_YESTERDAY_BLOCKED = "yesterday_blocked";
		public static final String COLUMN_NAME_YESTERDAY_ALLOWED = "yesterday_allowed";
		public static final String COLUMN_NAME_WEEK_BLOCKED = "week_blocked";
		public static final String COLUMN_NAME_WEEK_ALLOWED = "week_allowed";
		public static final String COLUMN_NAME_NEW_REQUESTS_COUNT = "last_new_notified_count";
	}

	/** Columns supported by "requests" records. */
	public static class Requests implements BaseColumns {
		/** MIME type for lists of entries. */
		public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.cleantalkprovider.requests";
		/** MIME type for individual entries. */
		public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.cleantalkprovider.request";
		/** Fully qualified URI for "request" resources. */
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_REQUESTS).build();
		/** Table name where records are stored for "request" resources. */
		public static final String TABLE_NAME = "requests";

		/** Table columns */
		public static final String COLUMN_NAME_SERVICE_ROW_ID = "service_row_id";
		public static final String COLUMN_NAME_REQUEST_ID = "request_id";
		public static final String COLUMN_NAME_ALLOW = "allow";
		public static final String COLUMN_NAME_DATETIME = "datetime";
		public static final String COLUMN_NAME_SENDER_EMAIL = "sender_email";
		public static final String COLUMN_NAME_SENDER_NICKNAME = "sender_nickname";
		public static final String COLUMN_NAME_TYPE = "type";
		public static final String COLUMN_NAME_MESSAGE = "message";
	}
}

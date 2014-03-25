package org.cleantalk.app.provider;

import java.util.ArrayList;
import java.util.TimeZone;

import org.cleantalk.app.api.ServiceApi;
import org.cleantalk.app.utils.SelectionBuilder;
import org.cleantalk.app.utils.Utils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class CleanTalkProvider extends ContentProvider {

	/** Content authority for this provider. */
	private static final String AUTHORITY = Contract.CONTENT_AUTHORITY;

	// Content URI codes
	private static final int ROUTE_SITES = 1;
	private static final int ROUTE_SITES_ID = 2;
	private static final int ROUTE_REQUESTS = 3;
	private static final int ROUTE_REQUESTS_ID = 4;

	// Declare and fill UriMatcher instance
	private static UriMatcher uriMatcher_ = new UriMatcher(UriMatcher.NO_MATCH);
	static {
		uriMatcher_.addURI(AUTHORITY, Contract.Sites.TABLE_NAME, ROUTE_SITES);
		uriMatcher_.addURI(AUTHORITY, Contract.Sites.TABLE_NAME + "/#", ROUTE_SITES_ID);
		uriMatcher_.addURI(AUTHORITY, Contract.Requests.TABLE_NAME, ROUTE_REQUESTS);
		uriMatcher_.addURI(AUTHORITY, Contract.Requests.TABLE_NAME + "/#", ROUTE_REQUESTS_ID);
	}

	private CleanTalkDbHelper dbHelper_;

	@Override
	public boolean onCreate() {
		dbHelper_ = new CleanTalkDbHelper(getContext());
		return true;
	}

	@Override
	public String getType(Uri uri) {
		switch (uriMatcher_.match(uri)) {
		case ROUTE_SITES:
			return Contract.Sites.CONTENT_TYPE;
		case ROUTE_SITES_ID:
			return Contract.Sites.CONTENT_ITEM_TYPE;
		case ROUTE_REQUESTS:
			return Contract.Requests.CONTENT_TYPE;
		case ROUTE_REQUESTS_ID:
			return Contract.Requests.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SelectionBuilder builder = new SelectionBuilder();
		final SQLiteDatabase db = dbHelper_.getWritableDatabase();
		final int match = uriMatcher_.match(uri);
		int count = 0;
		switch (match) {
		case ROUTE_SITES:
			count = builder.table(Contract.Sites.TABLE_NAME).where(selection, selectionArgs).delete(db);
			break;
		case ROUTE_SITES_ID:
			String rowId = String.valueOf(ContentUris.parseId(uri));
			count = builder.table(Contract.Sites.TABLE_NAME)
						.where(Contract.Sites._ID + "=?", rowId)
						.where(selection, selectionArgs)
						.delete(db);
			builder.table(Contract.Requests.TABLE_NAME)
					.where(Contract.Requests.COLUMN_NAME_SERVICE_ROW_ID + "=?", rowId)
					.delete(db);
			break;
		case ROUTE_REQUESTS:
			count = builder.table(Contract.Requests.TABLE_NAME).where(selection, selectionArgs).delete(db);
			break;
		case ROUTE_REQUESTS_ID:
			break;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		// Send broadcast to registered ContentObservers, to refresh UI.
		getContext().getContentResolver().notifyChange(uri, null, false);
		return count;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final SQLiteDatabase db = dbHelper_.getWritableDatabase();
		final int match = uriMatcher_.match(uri);
		Uri result = null;
		long rowId;

		switch (match) {
		case ROUTE_SITES:
			rowId = db.insertWithOnConflict(Contract.Sites.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			result = ContentUris.withAppendedId(Contract.Sites.CONTENT_URI, rowId);
			break;
		case ROUTE_SITES_ID:
			long serviceRowId = ContentUris.parseId(uri);
			values.put(Contract.Requests.COLUMN_NAME_SERVICE_ROW_ID, serviceRowId);
			rowId = db.insertWithOnConflict(Contract.Requests.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			break;
		case ROUTE_REQUESTS:
			rowId = db.insertWithOnConflict(Contract.Requests.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_IGNORE);
			result = ContentUris.withAppendedId(Contract.Requests.CONTENT_URI, rowId);
			break;
		case ROUTE_REQUESTS_ID:
			throw new UnsupportedOperationException("Insert not supported on URI: " + uri);
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		return result;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
		SQLiteDatabase db = dbHelper_.getReadableDatabase();

		String finalSelection = selection;
		String groupBy = null;
		String having = null;
		String orderBy = sortOrder;

		switch (uriMatcher_.match(uri)) {
		case ROUTE_SITES_ID:
			long rowId = ContentUris.parseId(uri);
			queryBuilder.appendWhere(Contract.Sites._ID + "=" + rowId);
			// we don't put break, because we just add where clause if specify id
		case ROUTE_SITES:
			 queryBuilder.setTables(Contract.Sites.TABLE_NAME + " LEFT OUTER JOIN " + Contract.Requests.TABLE_NAME
					 + " ON (" + Contract.Sites.TABLE_NAME + "." + Contract.Sites._ID + " = " 
					 + Contract.Requests.TABLE_NAME + "." + Contract.Requests.COLUMN_NAME_SERVICE_ROW_ID + ")");

			ArrayList<String> projectionList = new ArrayList<String>();
			projectionList.add(Contract.Sites.TABLE_NAME + "." + Contract.Sites._ID);
			projectionList.add(Contract.Sites.TABLE_NAME + "." + Contract.Sites.COLUMN_NAME_SERVICE_ID);
			projectionList.add(Contract.Sites.TABLE_NAME + "." + Contract.Sites.COLUMN_NAME_SERVICENAME);
			projectionList.add(Contract.Sites.TABLE_NAME + "." + Contract.Sites.COLUMN_NAME_HOSTNAME);
			projectionList.add(Contract.Sites.TABLE_NAME + "." + Contract.Sites.COLUMN_NAME_FAVICON_URL);
			projectionList.add(Contract.Sites.TABLE_NAME + "." + Contract.Sites.COLUMN_NAME_LAST_NEW_NOTIFIED_TIME);

			String start = "sum(" + Contract.Requests.TABLE_NAME + "." + Contract.Requests.COLUMN_NAME_DATETIME;
			String end = Contract.Requests.COLUMN_NAME_ALLOW + ") as ";

			TimeZone timezone = ServiceApi.getInstance(getContext()).getTimezone();
			projectionList.add(start +">" + Utils.getStartDayTimestamp(timezone) + " AND NOT " + end + Contract.Sites.COLUMN_NAME_TODAY_BLOCKED);
			projectionList.add(start +">" + Utils.getDayAgoTimestamp(timezone) + " AND NOT " + end + Contract.Sites.COLUMN_NAME_YESTERDAY_BLOCKED);
			projectionList.add(start +">" + Utils.getWeekAgoTimestamp(timezone) + " AND NOT " + end + Contract.Sites.COLUMN_NAME_WEEK_BLOCKED);
			projectionList.add(start +">" + Utils.getStartDayTimestamp(timezone) + " AND " + end + Contract.Sites.COLUMN_NAME_TODAY_ALLOWED);
			projectionList.add(start +">" + Utils.getDayAgoTimestamp(timezone) + " AND " + end + Contract.Sites.COLUMN_NAME_YESTERDAY_ALLOWED);
			projectionList.add(start +">" + Utils.getWeekAgoTimestamp(timezone) + " AND " + end + Contract.Sites.COLUMN_NAME_WEEK_ALLOWED);
			projectionList.add(start +">=" + Contract.Sites.TABLE_NAME +"."+ Contract.Sites.COLUMN_NAME_LAST_NEW_NOTIFIED_TIME 
					+ " AND " + end + Contract.Sites.COLUMN_NAME_NEW_REQUESTS_COUNT);

			projection = projectionList.toArray(new String[projectionList.size()]); 
			orderBy = TextUtils.isEmpty(sortOrder) ? Contract.Sites.COLUMN_NAME_SERVICENAME + " ASC" : sortOrder;
			groupBy = Contract.Sites.TABLE_NAME + "." + Contract.Sites.COLUMN_NAME_SERVICE_ID;
			break;
		case ROUTE_REQUESTS:
			queryBuilder.setTables(Contract.Requests.TABLE_NAME);
			orderBy = Contract.Requests.COLUMN_NAME_DATETIME + " DESC";
			break;
		case ROUTE_REQUESTS_ID:

		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		Cursor cursor = queryBuilder.query(db, projection, finalSelection, selectionArgs, groupBy, having, orderBy);
		cursor.setNotificationUri(getContext().getContentResolver(), Contract.Requests.CONTENT_URI);
		cursor.setNotificationUri(getContext().getContentResolver(), Contract.Sites.CONTENT_URI);
		return cursor;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		SelectionBuilder builder = new SelectionBuilder();
		final SQLiteDatabase db = dbHelper_.getWritableDatabase();

		final int match = uriMatcher_.match(uri);
		int count;
		switch (match) {
		case ROUTE_SITES:
			count = builder.table(Contract.Sites.TABLE_NAME).where(selection, selectionArgs).update(db, values);
			break;
		case ROUTE_SITES_ID:
			long rowId = ContentUris.parseId(uri);
			count = builder.table(Contract.Sites.TABLE_NAME).where(Contract.Sites._ID + "=?", String.valueOf(rowId))
					.where(selection, selectionArgs).update(db, values);
			break;
		default:
			throw new UnsupportedOperationException("Unknown uri: " + uri);
		}
		return count;
	}
}

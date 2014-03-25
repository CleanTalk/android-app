package org.cleantalk.app.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

import org.cleantalk.app.model.Request;
import org.cleantalk.app.model.Site;
import org.cleantalk.app.provider.Contract;
import org.cleantalk.app.utils.Utils;
import org.json.JSONArray;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.RemoteException;

import com.android.volley.Response.ErrorListener;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.RequestFuture;

public class UpdateRequestsTask extends AsyncTask<Void, Integer, Boolean>{

	private final ErrorListener errorListener_;
	private final ContentResolver contentResolver_;
	private final Context context_;

	public UpdateRequestsTask( Context context, ErrorListener errorListener) {
		errorListener_ = errorListener;
		context_ = context;
		contentResolver_ = context.getContentResolver();
	}

	@Override
	protected Boolean doInBackground(Void... params) {
		JSONArray array = null;
		TimeZone timezone = ServiceApi.getInstance(context_).getTimezone();
		final RequestFuture<JSONArray> servFuture = RequestFuture.newFuture();
		ServiceApi.getInstance(context_).getServices(servFuture, new ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				servFuture.cancel(true);
				cancel(true);
				errorListener_.onErrorResponse(error);
			}
		});
		
		try {
			array = servFuture.get();
		} catch (InterruptedException | ExecutionException e1) {
			e1.printStackTrace();
		}

		if(array == null) return false;
		updateSites(array);
		Map<Long, String> ids = new HashMap<Long, String>();
		Cursor c = contentResolver_.query(Contract.Sites.CONTENT_URI, 
				new String[] { Contract.Sites._ID, Contract.Sites.COLUMN_NAME_SERVICE_ID },
				null, null, null);
		while (c.moveToNext()) {
			ids.put(c.getLong(0), c.getString(1));
		}
		c.close();
		boolean changed = false;
		for (Entry<Long, String> service : ids.entrySet()) {
			long lastUpdatedTime = Utils.getLastUpdatedTime(context_, service.getValue());
			long currentTime = Utils.getCurrentTimestamp(timezone);
			final RequestFuture<JSONArray> future = RequestFuture.newFuture();
			ServiceApi.getInstance(context_).getRequests(future, new ErrorListener() {
				@Override
				public void onErrorResponse(VolleyError error) {
					future.cancel(true);
					cancel(true);
					errorListener_.onErrorResponse(error);
				}
			}, service.getValue(), lastUpdatedTime);
			try {
				array = future.get();
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
			if(array.length() > 0){
				changed = true;
				updateRequests(service.getKey(), array);
				Utils.setLastUpdatedTime(context_,service.getValue(), currentTime);
			}
		}
		if(changed){
			contentResolver_.delete(Contract.Requests.CONTENT_URI, Contract.Requests.COLUMN_NAME_DATETIME + "<?",
					new String[] { String.valueOf(Utils.getWeekAgoTimestamp(timezone)) });
			contentResolver_.notifyChange(Contract.Requests.CONTENT_URI, null);
			contentResolver_.notifyChange(Contract.Sites.CONTENT_URI, null);
		}
		return true;
	}

	private void updateSites(JSONArray array){
		List<Site> sites = Utils.parseSites(array);
		ArrayList<ContentProviderOperation> batch = new ArrayList<ContentProviderOperation>();

		// Build hash table of incoming sites
		HashMap<String, Site> entryMap = new HashMap<String, Site>();
		for (Site e : sites) {
			entryMap.put(e.getSiteId(), e);
		}

		// Get list of all sites in DB
		Uri uri = Contract.Sites.CONTENT_URI;
		Cursor c = contentResolver_.query(uri, new String[] {
				Contract.Sites._ID,
				Contract.Sites.COLUMN_NAME_SERVICE_ID,
				Contract.Sites.COLUMN_NAME_SERVICENAME,
				Contract.Sites.COLUMN_NAME_HOSTNAME,
				Contract.Sites.COLUMN_NAME_FAVICON_URL },
				null, null, null);
		if (c != null) {
			int id;
			String serviceId = null;
			String serviceName = null;
			String hostname = null;
			String faviconUri = null;

			while (c.moveToNext()) {
				id = c.getInt(0);
				serviceId = c.getString(1);
				serviceName = c.getString(2);
				hostname = c.getString(3);
				faviconUri = c.getString(4);
				Site match = entryMap.get(serviceId);

				if (match != null) {
					// Entry exists. Remove from entry map to prevent insert later.
					entryMap.remove(serviceId);
					// Check to see if the entry needs to be updated
					Uri existingUri = Contract.Sites.CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();
					if ((match.getSiteId() != null && !match.getSiteId().equals(serviceId))
							|| (match.getSiteName() != null && !match.getSiteName().equals(serviceName))
							|| (match.getHostname() != null && !match.getHostname().equals(hostname))
							|| (match.getFaviconUrl() != null && !match.getFaviconUrl().equals(faviconUri))) {
						// Update existing record
						batch.add(ContentProviderOperation.newUpdate(existingUri)
								.withValue(Contract.Sites.COLUMN_NAME_SERVICE_ID, serviceId)
								.withValue(Contract.Sites.COLUMN_NAME_SERVICENAME, serviceName)
								.withValue(Contract.Sites.COLUMN_NAME_HOSTNAME, hostname)
								.withValue(Contract.Sites.COLUMN_NAME_FAVICON_URL, faviconUri).build());
					} else {
					}
				} else {
					// Entry doesn't exist. Remove it from the database.
					Uri deleteUri = Contract.Sites.CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();
					batch.add(ContentProviderOperation.newDelete(deleteUri).build());
				}
			}
			c.close();
		}
		// Add new items
		for (Site site : entryMap.values()) {
			batch.add(ContentProviderOperation.newInsert(Contract.Sites.CONTENT_URI)
					.withValue(Contract.Sites.COLUMN_NAME_SERVICE_ID, site.getSiteId())
					.withValue(Contract.Sites.COLUMN_NAME_SERVICENAME, site.getSiteName())
					.withValue(Contract.Sites.COLUMN_NAME_HOSTNAME, site.getHostname())
					.withValue(Contract.Sites.COLUMN_NAME_LAST_NEW_NOTIFIED_TIME, Utils.getCurrentTimestamp(TimeZone.getTimeZone("GMT")))
					.withValue(Contract.Sites.COLUMN_NAME_FAVICON_URL, site.getFaviconUrl()).build());
		}
		try {
			contentResolver_.applyBatch(Contract.CONTENT_AUTHORITY, batch);
		} catch (RemoteException | OperationApplicationException e1) {
			e1.printStackTrace();
		}
	}

	private void updateRequests(long serviceRowId, JSONArray array) {
		List<Request> requests = Utils.parseRequests(context_, array);
		ArrayList<ContentValues> valuesList = new ArrayList<ContentValues>();
		for (Request request : requests) {
			ContentValues values = new ContentValues(8);
			values.put(Contract.Requests.COLUMN_NAME_SERVICE_ROW_ID, serviceRowId);
			values.put(Contract.Requests.COLUMN_NAME_ALLOW, request.isAllow());
			values.put(Contract.Requests.COLUMN_NAME_DATETIME, request.getDatetime());
			values.put(Contract.Requests.COLUMN_NAME_MESSAGE, request.getMessage());
			values.put(Contract.Requests.COLUMN_NAME_REQUEST_ID, request.getRequestId());
			values.put(Contract.Requests.COLUMN_NAME_SENDER_EMAIL, request.getSenderEmail());
			values.put(Contract.Requests.COLUMN_NAME_SENDER_NICKNAME, request.getSenderNickname());
			values.put(Contract.Requests.COLUMN_NAME_TYPE, request.getType());
			valuesList.add(values);
		}
		contentResolver_.bulkInsert(Contract.Requests.CONTENT_URI, valuesList.toArray(new ContentValues[valuesList.size()]));
	}
}
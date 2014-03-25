package org.cleantalk.app.provider;

import org.cleantalk.app.utils.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class CleanTalkDbHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "cleantalk.db";
	private static int DATABASE_VERSION = 1;
	private final Context context_;

	CleanTalkDbHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		context_ = context;
	}

	@Override
	public void onCreate(SQLiteDatabase sqLiteDatabase) {
		createTables(sqLiteDatabase);
	}

	private void createTables(SQLiteDatabase sqLiteDatabase) {
		String qs = "CREATE TABLE " + Contract.Sites.TABLE_NAME + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Contract.Sites.COLUMN_NAME_SERVICE_ID + " TEXT UNIQUE, " 
				+ Contract.Sites.COLUMN_NAME_SERVICENAME + " TEXT, "
				+ Contract.Sites.COLUMN_NAME_HOSTNAME + " TEXT, "
				+ Contract.Sites.COLUMN_NAME_LAST_NEW_NOTIFIED_TIME + " INTEGER, "
				+ Contract.Sites.COLUMN_NAME_FAVICON_URL + " TEXT);";
		sqLiteDatabase.execSQL(qs);

		qs = "CREATE TABLE " + Contract.Requests.TABLE_NAME  + " (" 
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ Contract.Requests.COLUMN_NAME_SERVICE_ROW_ID + " TEXT, " 
				+ Contract.Requests.COLUMN_NAME_REQUEST_ID + " TEXT UNIQUE, " 
				+ Contract.Requests.COLUMN_NAME_ALLOW + " INTEGER, "
				+ Contract.Requests.COLUMN_NAME_DATETIME + " INTEGER, " 
				+ Contract.Requests.COLUMN_NAME_SENDER_EMAIL + " TEXT, " 
				+ Contract.Requests.COLUMN_NAME_SENDER_NICKNAME + " TEXT, " 
				+ Contract.Requests.COLUMN_NAME_TYPE + " TEXT, " 
				+ Contract.Requests.COLUMN_NAME_MESSAGE + " TEXT);";
		sqLiteDatabase.execSQL(qs);

		Utils.cleanLastUpdatedTime(context_);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldv, int newv) {
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Contract.Sites.TABLE_NAME + ";");
		sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + Contract.Requests.TABLE_NAME + ";");
		createTables(sqLiteDatabase);
	}
}
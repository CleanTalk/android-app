/*
 * Copyright 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Modifications:
 * -Imported from AOSP frameworks/base/core/java/com/android/internal/content
 * -Changed package name
 */

package org.cleantalk.app.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper for building selection clauses for {@link SQLiteDatabase}. Each appended clause is combined using {@code AND}. This class is
 * <em>not</em> thread safe.
 */
public class SelectionBuilder {
	private String table_ = null;
	private Map<String, String> projectionMap_ = new HashMap<String, String>();
	private StringBuilder selection_ = new StringBuilder();
	private ArrayList<String> selectionArgs_ = new ArrayList<String>();

	/**
	 * Reset any internal state, allowing this builder to be recycled.
	 */
	public SelectionBuilder reset() {
		table_ = null;
		selection_.setLength(0);
		selectionArgs_.clear();
		return this;
	}

	/**
	 * Append the given selection clause to the internal state. Each clause is surrounded with parenthesis and combined using {@code AND}.
	 */
	public SelectionBuilder where(String selection, String... selectionArgs) {
		if (TextUtils.isEmpty(selection)) {
			if (selectionArgs != null && selectionArgs.length > 0) {
				throw new IllegalArgumentException("Valid selection required when including arguments=");
			}

			// Shortcut when clause is empty
			return this;
		}

		if (selection_.length() > 0) {
			selection_.append(" AND ");
		}

		selection_.append("(").append(selection).append(")");
		if (selectionArgs != null) {
			Collections.addAll(selectionArgs_, selectionArgs);
		}

		return this;
	}

	public SelectionBuilder table(String table) {
		table_ = table;
		return this;
	}

	private void assertTable() {
		if (table_ == null) {
			throw new IllegalStateException("Table not specified");
		}
	}

	public SelectionBuilder mapToTable(String column, String table) {
		projectionMap_.put(column, table + "." + column);
		return this;
	}

	public SelectionBuilder map(String fromColumn, String toClause) {
		projectionMap_.put(fromColumn, toClause + " AS " + fromColumn);
		return this;
	}

	/**
	 * Return selection string for current internal state.
	 * 
	 * @see #getSelectionArgs()
	 */
	public String getSelection() {
		return selection_.toString();
	}

	/**
	 * Return selection arguments for current internal state.
	 * 
	 * @see #getSelection()
	 */
	public String[] getSelectionArgs() {
		return selectionArgs_.toArray(new String[selectionArgs_.size()]);
	}

	private void mapColumns(String[] columns) {
		for (int i = 0; i < columns.length; i++) {
			final String target = projectionMap_.get(columns[i]);
			if (target != null) {
				columns[i] = target;
			}
		}
	}

	@Override
	public String toString() {
		return "SelectionBuilder[table=" + table_ + ", selection=" + getSelection() + ", selectionArgs="
				+ Arrays.toString(getSelectionArgs()) + "]";
	}

	/**
	 * Execute query using the current internal state as {@code WHERE} clause.
	 */
	public Cursor query(SQLiteDatabase db, String[] columns, String orderBy) {
		return query(db, columns, null, null, orderBy, null);
	}

	/**
	 * Execute query using the current internal state as {@code WHERE} clause.
	 */
	public Cursor query(SQLiteDatabase db, String[] columns, String groupBy, String having, String orderBy, String limit) {
		assertTable();
		if (columns != null)
			mapColumns(columns);
		return db.query(table_, columns, getSelection(), getSelectionArgs(), groupBy, having, orderBy, limit);
	}

	/**
	 * Execute update using the current internal state as {@code WHERE} clause.
	 */
	public int update(SQLiteDatabase db, ContentValues values) {
		assertTable();
		return db.update(table_, values, getSelection(), getSelectionArgs());
	}

	/**
	 * Execute delete using the current internal state as {@code WHERE} clause.
	 */
	public int delete(SQLiteDatabase db) {
		assertTable();
		return db.delete(table_, getSelection(), getSelectionArgs());
	}
}

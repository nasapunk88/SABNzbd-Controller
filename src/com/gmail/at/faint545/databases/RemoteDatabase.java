/* 
 * Copyright 2011 Alex Fu
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 		
 * 		http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.gmail.at.faint545.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RemoteDatabase {
	private static final String TABLE = "remote";
	private static final String _ID = "_id";
	private static final String NAME = "name";
	private static final String ADDR = "address";
	private static final String PORT = "port";
	private static final String API_KEY = "api_key";
	private static final String REFRESH = "refresh";
	public static final int ID_INDEX = 0;
	public static final int NAME_INDEX = 1;
	public static final int ADDR_INDEX = 2;
	public static final int PORT_INDEX = 3;
	public static final int API_KEY_INDEX = 4;
	public static final int REFRESH_INDEX = 5;
	
	private Context mContext;
	private DatabaseOpenHelper dbHelper;
	private SQLiteDatabase database;
	
	public RemoteDatabase(Context context) {
		mContext = context;
	}
	
	public void open() {
		dbHelper = new DatabaseOpenHelper(mContext);
		database = dbHelper.getWritableDatabase();
	}
	
	public void close() {
		database.close();
		dbHelper.close();
	}
	
	public long insert(String name, String address, String port, String apiKey, String refreshInterval) {
		ContentValues values = createContentValues(name, address, port, apiKey,refreshInterval);
		return database.insert(TABLE, null, values);
	}
	
	public long update(int rowID, String name, String address, String port, String apiKey, String refreshInterval) {
		ContentValues values = createContentValues(name, address, port, apiKey,refreshInterval);		
		return database.update(TABLE, values, _ID + "=" + rowID, null);
	}
	
	public long delete(int rowID) {
		return database.delete(TABLE, _ID + "=" + rowID, null);
	}
	
	public Cursor getAllRows() {
		return database.query(TABLE, null, null, null, null, null, null);
	}
	
	public Cursor getRowAt(int rowID) {
		return database.query(TABLE, null, _ID + "=" + rowID, null, null, null, null);
	}

	private ContentValues createContentValues(String name, String address, String port, String apiKey, String refreshInterval) {
		ContentValues values = new ContentValues();
		if(name != null) values.put(NAME, name);
		if(address != null) values.put(ADDR, address);
		if(port != null) values.put(PORT, port);
		if(apiKey != null) values.put(API_KEY, apiKey);
		if(refreshInterval != null) values.put(REFRESH, refreshInterval);
		return values;
	}
}

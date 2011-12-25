package com.gmail.at.faint545.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class RemoteDatabase {
	private static final String TABLE = "remote", _ID = "_id", NAME = "name", ADDR = "address", PORT = "port", API_KEY = "api_key";
	public static final int ID_INDEX = 0, NAME_INDEX = 1, ADDR_INDEX = 2, PORT_INDEX = 3, API_KEY_INDEX = 4;
	
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
	
	public long insert(String name, String address, String port, String apiKey) {
		ContentValues values = createContentValues(name, address, port, apiKey);
		return database.insert(TABLE, null, values);
	}
	
	public long update(int rowID, String name, String address, String port, String apiKey) {
		ContentValues values = createContentValues(name, address, port, apiKey);		
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

	private ContentValues createContentValues(String name, String address, String port, String apiKey) {
		ContentValues values = new ContentValues();
		if(name != null) values.put(NAME, name);
		if(address != null) values.put(ADDR, address);
		if(port != null) values.put(PORT, port);
		if(apiKey != null) values.put(API_KEY, apiKey);
		return values;
	}
}

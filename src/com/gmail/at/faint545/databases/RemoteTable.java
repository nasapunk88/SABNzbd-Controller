package com.gmail.at.faint545.databases;

import android.database.sqlite.SQLiteDatabase;

public class RemoteTable {
	
	private static String CREATE_STATEMENT = "CREATE TABLE remote " +
											 "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + // ID
											 "name TEXT NOT NULL," + // Name
											 "address TEXT NOT NULL," + // Address
											 "port TEXT NOT NULL," + // Port
											 "api_key TEXT NOT NULL);"; // SABNzbd API key
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_STATEMENT);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		database.execSQL("DROP TABLE IF EXISTS remote");
		onCreate(database);
	}
}

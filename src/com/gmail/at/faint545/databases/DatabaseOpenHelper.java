package com.gmail.at.faint545.databases;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseOpenHelper extends SQLiteOpenHelper {
	private final static String DATABASE_NAME = "sabnzbdcontroller";
	private final static int VERSION = 2;

	public DatabaseOpenHelper(Context context) {
		super(context, DATABASE_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase database) {
		RemoteTable.onCreate(database);
	}

	@Override
	public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		RemoteTable.onUpgrade(database, oldVersion, newVersion);
	}
}

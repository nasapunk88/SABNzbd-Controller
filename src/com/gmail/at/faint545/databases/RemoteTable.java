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

import android.database.sqlite.SQLiteDatabase;

public class RemoteTable {
	
	private static String CREATE_STATEMENT = "CREATE TABLE remote " +
											 "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + // ID
											 "name TEXT NOT NULL," + // Name
											 "address TEXT NOT NULL," + // Address
											 "port TEXT NOT NULL," + // Port
											 "api_key TEXT NOT NULL," + // SABNzbd API key
											 "refresh TEXT);"; // Refresh interval
	
	public static void onCreate(SQLiteDatabase database) {
		database.execSQL(CREATE_STATEMENT);
	}
	
	public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
		database.execSQL("DROP TABLE IF EXISTS remote");
		onCreate(database);
	}
}
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
package com.gmail.at.faint545;

import android.os.Parcel;
import android.os.Parcelable;

public class Remote implements Parcelable {
	private String name;
	private String address;
	private String port;
	private String apiKey;
	private String id;
	private long refreshInterval;

	public Remote(String name) {
		this.name = name;
	}
	
	public Remote setAddress(String address) {
		this.address = address;
		return this;
	}
	
	public Remote setPort(String port) {
		this.port = port;
		return this;
	}

	public Remote setApiKey(String apiKey) {
		this.apiKey = apiKey;
		return this;
	}
	
	public Remote setId(String id) {
		this.id = id;
		return this;
	}
	
	public Remote setRefreshInterval(long interval) {
		refreshInterval = interval;
		return this;
	}
	
	public String getName() {
		return name;
	}
	
	public String getApiKey() {
		return apiKey;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getPort() {
		return port;
	}
	
	public String getHost() {
		StringBuilder hostname = new StringBuilder();
		hostname.append("http://").append(address).append(":").append(port);
		return hostname.toString();
	}
	
	public String getId() {
		return id;
	}
	
	public long getRefreshInterval() {
		return refreshInterval;
	}
	
	public String buildURL() {
		//http://localhost:8080/sabnzbd/api?apikey=
		return getHost() + "/" + "sabnzbd/api?";
	}
	
	/*
	 * Below here is for Parcelable
	 */

	public Remote(Parcel source) {
		readFromParcel(source);
	}
	
	public int describeContents() {
		return 0;
	}	
	
	public static final Creator<Remote> CREATOR = new Parcelable.Creator<Remote>() {

		public Remote createFromParcel(Parcel source) {
			return new Remote(source);
		}

		public Remote[] newArray(int size) {
			return new Remote[size];
		}
	};
	
	/*
	 * Method to write each element to a 
	 * Parcel. BE AWARE: The order in which
	 * elements get written MATTERS! The order 
	 * must be the same in readFromParcel().
	 */	
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(address);
		dest.writeString(port);
		dest.writeString(apiKey);
		dest.writeString(id);
		dest.writeLong(refreshInterval);
	}
	
	/*
	 * Note the order in which the Parcel is 
	 * read is the same as when the Parcel was
	 * written.
	 */	
	private void readFromParcel(Parcel source) {
		name = source.readString();
		address = source.readString();
		port = source.readString();
		apiKey = source.readString();
		id = source.readString();
		refreshInterval = source.readLong();
	}	
}

package com.gmail.at.faint545;

import android.os.Parcel;
import android.os.Parcelable;

public class Remote implements Parcelable {
	private String name,address,port,apiKey;

	public Remote(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public Remote setAddress(String address) {
		this.address = address;
		return this;
	}
	
	public Remote setPort(String port) {
		this.port = port;
		return this;
	}
	
	public String getHost() {
		StringBuilder hostname = new StringBuilder();
		hostname.append(address).append(":").append(port);
		return hostname.toString();
	}

	public Remote setApiKey(String apiKey) {
		this.apiKey = apiKey;
		return this;
	}

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
	}	
}

package com.gmail.at.faint545;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author  alex
 */
public class Remote implements Parcelable {
	/**
	 * @uml.property  name="name"
	 */
	private String name;
	/**
	 * @uml.property  name="address"
	 */
	private String address;
	/**
	 * @uml.property  name="port"
	 */
	private String port;
	/**
	 * @uml.property  name="apiKey"
	 */
	private String apiKey;
	/**
	 * @uml.property  name="id"
	 */
	private String id;

	public Remote(String name) {
		this.name = name;
	}
	
	/**
	 * @param address
	 * @return
	 * @uml.property  name="address"
	 */
	public Remote setAddress(String address) {
		this.address = address;
		return this;
	}
	
	/**
	 * @param port
	 * @return
	 * @uml.property  name="port"
	 */
	public Remote setPort(String port) {
		this.port = port;
		return this;
	}

	/**
	 * @param apiKey
	 * @return
	 * @uml.property  name="apiKey"
	 */
	public Remote setApiKey(String apiKey) {
		this.apiKey = apiKey;
		return this;
	}
	
	/**
	 * @param id
	 * @return
	 * @uml.property  name="id"
	 */
	public Remote setId(String id) {
		this.id = id;
		return this;
	}
	
	/**
	 * @return
	 * @uml.property  name="name"
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * @return
	 * @uml.property  name="apiKey"
	 */
	public String getApiKey() {
		return apiKey;
	}
	
	/**
	 * @return
	 * @uml.property  name="address"
	 */
	public String getAddress() {
		return address;
	}
	
	/**
	 * @return
	 * @uml.property  name="port"
	 */
	public String getPort() {
		return port;
	}
	
	public String getHost() {
		StringBuilder hostname = new StringBuilder();
		hostname.append("http://").append(address).append(":").append(port);
		return hostname.toString();
	}
	
	/**
	 * @return
	 * @uml.property  name="id"
	 */
	public String getId() {
		return id;
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
	}	
}

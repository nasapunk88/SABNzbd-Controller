package com.gmail.at.faint545;

public class Remote {
	private String name,address,port;

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
}

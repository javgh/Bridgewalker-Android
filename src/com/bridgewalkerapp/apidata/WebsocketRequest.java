package com.bridgewalkerapp.apidata;

public abstract class WebsocketRequest {
	public static final int TYPE_REQUEST_VERSION = 0;
	
	abstract public int getRequestType();
}

package com.bridgewalkerapp.androidclient.apidata;

public abstract class WebsocketRequest {
	public static final int TYPE_REQUEST_VERSION = 0;
	public static final int TYPE_CREATE_GUEST_ACCOUNT = 1;
	public static final int TYPE_LOGIN = 2;
	public static final int TYPE_REQUEST_STATUS = 1;
	
	abstract public int getRequestType();
}

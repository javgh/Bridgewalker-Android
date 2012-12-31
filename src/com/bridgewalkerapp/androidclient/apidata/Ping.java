package com.bridgewalkerapp.androidclient.apidata;

public class Ping extends WebsocketRequest {
	public String getOp() {
		return "ping";
	}
	
	@Override
	public int getRequestType() {
		return TYPE_PING;
	}
}

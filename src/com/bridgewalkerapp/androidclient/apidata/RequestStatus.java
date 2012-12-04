package com.bridgewalkerapp.androidclient.apidata;

public class RequestStatus extends WebsocketRequest {
	public String getOp() {
		return "request_status";
	}
	@Override
	public int getRequestType() {
		return TYPE_REQUEST_STATUS;
	}
}

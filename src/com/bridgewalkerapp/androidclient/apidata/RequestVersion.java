package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonProperty;

public class RequestVersion extends WebsocketRequest {
	public static final String BRIDGEWALKER_CLIENT_VERSION = "0.1";
	
	public String getOp() {
		return "request_version";
	}

	@JsonProperty("client_version")
	public String getClientVersion() {
		return BRIDGEWALKER_CLIENT_VERSION;
	}

	@Override
	public int getRequestType() {
		return TYPE_REQUEST_VERSION;
	}
}

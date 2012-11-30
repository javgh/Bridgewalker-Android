package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WSLoginSuccessful extends WebsocketReply {
	@Override
	public int getReplyType() {
		return TYPE_WS_LOGIN_SUCCESSFUL;
	}	
	
	@Override
	public boolean isReplyTo(WebsocketRequest request) {
		return request.getRequestType() == WebsocketRequest.TYPE_LOGIN;
	}
}

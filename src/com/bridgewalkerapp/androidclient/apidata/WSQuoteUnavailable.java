package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WSQuoteUnavailable extends WebsocketReply {
	@Override
	public int getReplyType() {
		return TYPE_WS_QUOTE_UNAVAILABLE;
	}

	@Override
	public boolean isReplyTo(WebsocketRequest request) {
		return request.getRequestType() == WebsocketRequest.TYPE_REQUEST_QUOTE;
	}
}
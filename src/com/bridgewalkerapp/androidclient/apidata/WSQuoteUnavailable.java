package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WSQuoteUnavailable extends WebsocketReply {
	private long id;
	
	@Override
	public int getReplyType() {
		return TYPE_WS_QUOTE_UNAVAILABLE;
	}
	
	@JsonProperty("request_id")
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}

	@Override
	public boolean isReplyTo(WebsocketRequest request) {
		return request.getRequestType() == WebsocketRequest.TYPE_REQUEST_QUOTE;
	}
}
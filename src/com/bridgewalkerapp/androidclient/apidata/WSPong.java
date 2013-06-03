package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WSPong extends WebsocketReply {
	private long exchangeRate;
	
	@JsonProperty("exchange_rate")
	public void setExchangeRate(long exchangeRate) {
		this.exchangeRate = exchangeRate;
	}
	
	public long getExchangeRate() {
		return exchangeRate;
	}
	
	@Override
	public int getReplyType() {
		return TYPE_WS_PONG;
	}

	@Override
	public boolean isReplyTo(WebsocketRequest request) {
		return request.getRequestType() == WebsocketRequest.TYPE_PING;
	}
}

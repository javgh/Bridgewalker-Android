package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WSSendSuccessful extends WebsocketReply {
	private long id;
	private String tx;

	@JsonProperty("request_id")
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setTx(String tx) {
		this.tx = tx;
	}
	
	public String getTx() {
		return tx;
	}
	
	@Override
	public int getReplyType() {
		return TYPE_WS_SEND_SUCCESSFUL;
	}

	@Override
	public boolean isReplyTo(WebsocketRequest request) {
		return request.getRequestType() == WebsocketRequest.TYPE_SEND_PAYMENT;
	}
}

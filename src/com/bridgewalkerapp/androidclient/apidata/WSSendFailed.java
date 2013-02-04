package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WSSendFailed extends WebsocketReply {
	private long id;
	private String reason;
	
	@JsonProperty("request_id")
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}	
	
	public void setReason(String reason) {
		this.reason = reason;
	}
	
	public String getReason() {
		return reason;
	}
	
	@Override
	public int getReplyType() {
		return TYPE_WS_SEND_FAILED;
	}

	@Override
	public boolean isReplyTo(WebsocketRequest request) {
		return request.getRequestType() == WebsocketRequest.TYPE_SEND_PAYMENT;
	}
}

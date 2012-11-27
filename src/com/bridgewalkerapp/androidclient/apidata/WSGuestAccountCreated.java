package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WSGuestAccountCreated extends WebsocketReply {
	private String accountName;
	private String accountPassword;
	
	@JsonProperty("account_name")
	public void setAccountName(String accountName) {
		this.accountName = accountName;
	}
	
	@JsonProperty("account_password")
	public void setAccountPassword(String accountPassword) {
		this.accountPassword = accountPassword;
	}
	
	public String getAccountName() {
		return accountName;
	}
	
	public String getAccountPassword() {
		return accountPassword;
	}
	
	@Override
	public int getReplyType() {
		return TYPE_WS_GUEST_ACCOUNT_CREATED;
	}

	@Override
	public boolean isReplyTo(WebsocketRequest request) {
		return request.getRequestType() == WebsocketRequest.TYPE_CREATE_GUEST_ACCOUNT;
	}


}

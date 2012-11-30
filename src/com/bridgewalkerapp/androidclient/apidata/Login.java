package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonProperty;

public class Login extends WebsocketRequest {
	private final String accountName;
	private final String accountPassword;
	
	public Login(String accountName, String accountPassword) {
		this.accountName = accountName;
		this.accountPassword = accountPassword;
	}
	
	public String getOp() {
		return "login";
	}
	
	@JsonProperty("account_name")
	public String getAccountName() {
		return accountName;
	}

	@JsonProperty("account_password")
	public String getAccountPassword() {
		return accountPassword;
	}

	@Override
	public int getRequestType() {
		return TYPE_LOGIN;
	}
}

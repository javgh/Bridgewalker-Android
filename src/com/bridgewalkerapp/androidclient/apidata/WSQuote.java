package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WSQuote extends WebsocketReply {
	private long id;
	private long btc;
	private long usdRecipient;
	private long usdAccount;
	private boolean hasSufficientBalance;
	
	@Override
	public int getReplyType() {
		return TYPE_WS_QUOTE;
	}
	
	@JsonProperty("request_id")
	public void setId(long id) {
		this.id = id;
	}
	
	public long getId() {
		return id;
	}
	
	public void setBtc(long btc) {
		this.btc = btc;
	}
	
	public long getBtc() {
		return btc;
	}	

	@JsonProperty("usd_recipient")
	public void setUsdRecipient(long usdRecipient) {
		this.usdRecipient = usdRecipient;
	}

	public long getUsdRecipient() {
		return usdRecipient;
	}	

	@JsonProperty("usd_account")	
	public void setUsdAccount(long usdAccount) {
		this.usdAccount = usdAccount;
	}

	public long getUsdAccount() {
		return usdAccount;
	}
	
	@JsonProperty("sufficient_balance")
	public void setHasSufficientBalance(boolean hasSufficientBalance) {
		this.hasSufficientBalance = hasSufficientBalance;
	}
	
	public boolean hasSufficientBalance() {
		return hasSufficientBalance;
	}
	
	@Override
	public boolean isReplyTo(WebsocketRequest request) {
		return request.getRequestType() == WebsocketRequest.TYPE_REQUEST_QUOTE;
	}
}

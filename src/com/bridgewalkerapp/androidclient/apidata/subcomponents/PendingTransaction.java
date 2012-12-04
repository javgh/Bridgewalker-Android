package com.bridgewalkerapp.androidclient.apidata.subcomponents;

import org.codehaus.jackson.annotate.JsonProperty;

public class PendingTransaction {
	private long amount;
	private PendingReason reason;
	
	@JsonProperty("amount")
	public long getAmount() {
		return amount;
	}
	
	public void setAmount(long amount) {
		this.amount = amount;
	}
	
	@JsonProperty("reason")
	public PendingReason getReason() {
		return reason;
	}
	
	public void setReason(PendingReason reason) {
		this.reason = reason;
	}
}

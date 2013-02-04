package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonProperty;

public class SendPayment extends WebsocketRequest {
	private long id;
	private String address;
	private AmountType type;
	private long amount;
	
	public SendPayment(long id, String address, AmountType type, long amount) {
		this.id = id;
		this.address = address;
		this.type = type;
		this.amount = amount;
	}

	public String getOp() {
		return "send_payment";
	}
	
	@JsonProperty("request_id")	
	public long getId() {
		return id;
	}
	
	public String getAddress() {
		return address;
	}
	
	public String getType() {
		switch (type) {
			case AMOUNT_BASED_ON_BTC: return "amount_based_on_btc";
			case AMOUNT_BASED_ON_USD_BEFORE_FEES: return "amount_based_on_usd_before_fees";
			case AMOUNT_BASED_ON_USD_AFTER_FEES: return "amount_based_on_usd_after_fees";
		}
		throw new RuntimeException("Should not reach here");
	}
	
	public long getAmount() {
		return amount;
	}	
	
	@Override
	public int getRequestType() {
		return TYPE_SEND_PAYMENT;
	}
}

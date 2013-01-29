package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonProperty;

public class RequestQuote extends WebsocketRequest {
	public enum AmountType { AMOUNT_BASED_ON_BTC
						   , AMOUNT_BASED_ON_USD_BEFORE_FEES
						   , AMOUNT_BASED_ON_USD_AFTER_FEES
						   }
	
	private long id;
	private AmountType type;
	private long amount;
	
	public RequestQuote(long id, AmountType type, long amount) {
		this.id = id;
		this.type = type;
		this.amount = amount;
	}
	
	public String getOp() {
		return "request_quote";
	}
	
	@JsonProperty("request_id")	
	public long getId() {
		return id;
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
		return TYPE_REQUEST_QUOTE;
	}
	
	public boolean isSameRequest(RequestQuote rq) {
		if (rq == null)
			return false;
		else 
			return (rq.getType() == getType() && rq.getAmount() == getAmount());
	}
}

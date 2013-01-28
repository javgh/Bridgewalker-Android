package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonProperty;

public class RequestQuote extends WebsocketRequest {
	public enum QuoteType { QUOTE_BASED_ON_BTC
						  , QUOTE_BASED_ON_USD_BEFORE_FEES
						  , QUOTE_BASED_ON_USD_AFTER_FEES
						  }
	
	private long id;
	private QuoteType type;
	private long amount;
	
	public RequestQuote(long id, QuoteType type, long amount) {
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
			case QUOTE_BASED_ON_BTC: return "quote_based_on_btc";
			case QUOTE_BASED_ON_USD_BEFORE_FEES: return "quote_based_on_usd_before_fees";
			case QUOTE_BASED_ON_USD_AFTER_FEES: return "quote_based_on_usd_after_fees";
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

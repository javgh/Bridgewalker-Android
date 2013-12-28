package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

public class RequestQuote extends WebsocketRequest {
	private long id;
	@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
	private String address;		/* 'null' signals no address */
	private AmountType type;
	private long amount;
	
	public RequestQuote(long id, AmountType type, long amount) {
		this(id, null, type, amount);
	}
	
	public RequestQuote(long id, String address, AmountType type, long amount) {
		this.id = id;
		this.address = address;
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
		return TYPE_REQUEST_QUOTE;
	}
	
	private boolean isAddressEqual(RequestQuote rq) {
		// if both are 'null' the addresses are the same
		if (rq.getAddress() == null && getAddress() == null)
			return true;
		
		// if only one of them is 'null' they are not the same
		if (rq.getAddress() == null || getAddress() == null)
			return false;
		
		// otherwise we compare the strings
		return rq.getAddress().equals(getAddress());
	}
	
	private boolean isAddressEqual(SendPayment sp) {
		// if both are 'null' the addresses are the same
		if (sp.getAddress() == null && getAddress() == null)
			return true;
		
		// if only one of them is 'null' they are not the same
		if (sp.getAddress() == null || getAddress() == null)
			return false;
		
		// otherwise we compare the strings
		return sp.getAddress().equals(getAddress());
	}
	
	public boolean isSameRequest(RequestQuote rq) {
		if (rq == null)
			return false;
		else 
			return (isAddressEqual(rq) && rq.getType() == getType() && rq.getAmount() == getAmount());
	}
	
	public boolean isSimilarRequest(SendPayment sp) {
		if (sp == null)
			return false;
		else
			return (isAddressEqual(sp) && sp.getType() == getType() && sp.getAmount() == getAmount());
	}
}

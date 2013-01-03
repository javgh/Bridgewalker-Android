package com.bridgewalkerapp.androidclient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//TODO: Upgrade to be able to parse all of these:
//
//bitcoin:1abc
//bitcoin:1abc?amount=123
//bitcoin://1abc				(broken format)
//bitcoin://1abc?amount=123	(broken format)
//1abc
//1abc?amount=123
//
//write unit tests for it

public class BitcoinURI {
	private String address;
	private long amount;

	public BitcoinURI(String address, long amount) {
		this.address = address;
		this.amount = amount;
	}
	
	public String getAddress() {
		return address;
	}
	
	public long getAmount() {
		return amount;
	}
	
	public static BitcoinURI parse(String uriString) {
		Pattern pattern = Pattern.compile("(bitcoin:(//)?)?([^?]*)(\\?.*)?");
		Matcher matcher = pattern.matcher(uriString);
		
		if (matcher.matches()) {
			// group 0 is the whole match
			String bitcoinAddress = matcher.group(3);
			String queryPart = matcher.group(4);	// might be null
			
			// TODO: deal with queryPart
			
			return new BitcoinURI(bitcoinAddress, 0);
		} else {
			return null;
		}
	}
}

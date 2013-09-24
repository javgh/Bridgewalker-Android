package com.bridgewalkerapp.androidclient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

// See unit tests for sample input.
public class BitcoinURI {
	private static final double BTC_BASE_AMOUNT = Math.pow(10, 8);
	
	private String address;
	private long amount;
	private String currency;

	public BitcoinURI(String address, long amount, String currency) {
		this.address = address;
		this.amount = amount;
		this.currency = currency;
	}
	
	public String getAddress() {
		return address;
	}
	
	public long getAmount() {
		return amount;
	}
	
	public String getCurrency() {
		return currency;
	}
	
	public static BitcoinURI parse(String uriString) {
		Pattern pattern = Pattern.compile("(bitcoin:(//)?)?([^?]*)(\\?(.*))?");
		Matcher matcher = pattern.matcher(uriString);
		
		if (matcher.matches()) {
			// group 0 is the whole match
			String bitcoinAddress = matcher.group(3);
			String queryPart = matcher.group(5);	// might be null
			
			if (queryPart == null)
				return new BitcoinURI(bitcoinAddress, 0, "BTC");

			// try to parse amount & currency
			long amount = 0;
			String currency = "BTC";
			Pattern amountSubpattern = Pattern.compile("amount=(.*)");
			Pattern currencySubpattern = Pattern.compile("currency=(.*)");
			String[] parameters = queryPart.split("&");
			for (String parameter : parameters) {
				// amount
				Matcher submatcher = amountSubpattern.matcher(parameter);
				if (submatcher.matches()) {
					String asString = submatcher.group(1);
					try {
						double asDouble = Double.parseDouble(asString);
						amount = Math.round(asDouble * BTC_BASE_AMOUNT);
					} catch (NumberFormatException e) { /* ignore */ }
				}
				
				// currency
				submatcher = currencySubpattern.matcher(parameter);
				if (submatcher.matches()) {
					currency = submatcher.group(1);
				}
			}
			
			return new BitcoinURI(bitcoinAddress, amount, currency);
		} else {
			return null;
		}
	}
}

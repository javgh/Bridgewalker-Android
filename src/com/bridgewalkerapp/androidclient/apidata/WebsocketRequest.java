package com.bridgewalkerapp.androidclient.apidata;

public abstract class WebsocketRequest {
	public enum AmountType { AMOUNT_BASED_ON_BTC
						   , AMOUNT_BASED_ON_USD_BEFORE_FEES
						   , AMOUNT_BASED_ON_USD_AFTER_FEES
	   					   }

	public static final int TYPE_REQUEST_VERSION = 0;
	public static final int TYPE_CREATE_GUEST_ACCOUNT = 1;
	public static final int TYPE_LOGIN = 2;
	public static final int TYPE_REQUEST_STATUS = 3;
	public static final int TYPE_PING = 4;
	public static final int TYPE_REQUEST_QUOTE = 5;
	public static final int TYPE_SEND_PAYMENT = 6;
	public static final int TYPE_SUBMIT_CLAIM = 7;
	
	abstract public int getRequestType();
}

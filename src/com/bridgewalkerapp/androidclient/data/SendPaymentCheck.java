package com.bridgewalkerapp.androidclient.data;

public class SendPaymentCheck {
	boolean isReady;
	String hint;
	
	public SendPaymentCheck(boolean isReady, String hint) {
		this.isReady = isReady;
		this.hint = hint;
	}
	
	public boolean isReady() {
		return isReady;
	}
	
	public String getHint() {
		return hint;
	}
}
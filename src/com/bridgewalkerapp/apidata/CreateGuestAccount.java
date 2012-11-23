package com.bridgewalkerapp.apidata;

public class CreateGuestAccount extends WebsocketRequest {
	public String getOp() {
		return "create_guest_account";
	}
	
	@Override
	public int getRequestType() {
		return TYPE_CREATE_GUEST_ACCOUNT;
	}
}

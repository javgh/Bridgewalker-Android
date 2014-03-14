package com.bridgewalkerapp.androidclient.apidata;

public class SubmitClaim extends WebsocketRequest {
	private String address;
	
	public SubmitClaim(String address) {
		this.address = address;
	}

	public String getOp() {
		return "submit_claim";
	}
	
	public String getAddress() {
		return address;
	}
	
	@Override
	public int getRequestType() {
		return TYPE_SUBMIT_CLAIM;
	}
}

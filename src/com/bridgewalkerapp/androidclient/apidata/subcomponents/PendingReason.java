package com.bridgewalkerapp.androidclient.apidata.subcomponents;

import org.codehaus.jackson.annotate.JsonProperty;

public class PendingReason {
	public static final int PENDING_REASON_TOO_FEW_CONFIRMATIONS = 0;
	public static final int PENDING_REASON_MARKER_ADDRESS_LIMIT_REACHED = 1;
	
	private int type = -1;
	private int confirmations = 0;
	private String markerAddress = null;

	@JsonProperty("type")
	public void setType(String type) {
		if (type.equalsIgnoreCase("too_few_confirmations")) {
			this.type = PENDING_REASON_TOO_FEW_CONFIRMATIONS;
		} else if (type.equalsIgnoreCase("marker_address_limit_reached")) {
			this.type = PENDING_REASON_MARKER_ADDRESS_LIMIT_REACHED;
		}
	}
	
	public int getType() {
		return type;
	}
	
	@JsonProperty("confirmations")
	public int getConfirmations() {
		return confirmations;
	}
	
	public void setConfirmations(int confirmations) {
		this.confirmations = confirmations;
	}
	
	@JsonProperty("marker_address")
	public String getMarkerAddress() {
		return markerAddress;
	}
	
	public void setMarkerAddress(String markerAddress) {
		this.markerAddress = markerAddress;
	}
}
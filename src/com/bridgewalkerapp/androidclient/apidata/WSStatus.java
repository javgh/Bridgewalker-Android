package com.bridgewalkerapp.androidclient.apidata;

import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import com.bridgewalkerapp.androidclient.apidata.subcomponents.PendingTransaction;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WSStatus extends WebsocketReply {
	private long usdBalance;
	private long btcIn;
	private String primaryBTCAddress;
	private List<PendingTransaction> pendingTxs;

	@JsonProperty("usd_balance")
	public long getUsdBalance() {
		return usdBalance;
	}

	public void setUsdBalance(long usdBalance) {
		this.usdBalance = usdBalance;
	}

	@JsonProperty("btc_in")
	public long getBtcIn() {
		return btcIn;
	}

	public void setBtcIn(long btcIn) {
		this.btcIn = btcIn;
	}

	@JsonProperty("primary_btc_address")
	public String getPrimaryBTCAddress() {
		return primaryBTCAddress;
	}

	public void setPrimaryBTCAddress(String primaryBTCAddress) {
		this.primaryBTCAddress = primaryBTCAddress;
	}

	@JsonProperty("pending_txs")
	public List<PendingTransaction> getPendingTxs() {
		return pendingTxs;
	}

	public void setPendingTxs(List<PendingTransaction> pendingTxs) {
		this.pendingTxs = pendingTxs;
	}

	@Override
	public int getReplyType() {
		return TYPE_WS_STATUS;
	}

	@Override
	public boolean isReplyTo(WebsocketRequest request) {
		return request.getRequestType() == WebsocketRequest.TYPE_REQUEST_STATUS;
	}
}


//
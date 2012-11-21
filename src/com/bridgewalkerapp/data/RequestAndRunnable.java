package com.bridgewalkerapp.data;

import com.bridgewalkerapp.apidata.WebsocketRequest;

public class RequestAndRunnable {
	private WebsocketRequest request;
	private Runnable runnable;
	
	public RequestAndRunnable(WebsocketRequest request, Runnable runnable) {
		this.request = request;
		this.runnable = runnable;
	}

	public WebsocketRequest getRequest() {
		return request;
	}

	public Runnable getRunnable() {
		return runnable;
	}
}

package com.bridgewalkerapp.androidclient.data;

import com.bridgewalkerapp.androidclient.apidata.WebsocketRequest;

public class RequestAndRunnable {
	private WebsocketRequest request;
	private ParameterizedRunnable runnable;
	
	public RequestAndRunnable(WebsocketRequest request, ParameterizedRunnable runnable) {
		this.request = request;
		this.runnable = runnable;
	}

	public WebsocketRequest getRequest() {
		return request;
	}

	public ParameterizedRunnable getRunnable() {
		return runnable;
	}
}

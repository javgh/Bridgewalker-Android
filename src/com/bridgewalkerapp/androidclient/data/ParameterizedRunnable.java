package com.bridgewalkerapp.androidclient.data;

import com.bridgewalkerapp.androidclient.apidata.WebsocketReply;

public interface ParameterizedRunnable {
	public void run(WebsocketReply reply);
}

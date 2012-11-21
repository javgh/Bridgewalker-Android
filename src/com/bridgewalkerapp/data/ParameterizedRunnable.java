package com.bridgewalkerapp.data;

import com.bridgewalkerapp.apidata.WebsocketReply;

public interface ParameterizedRunnable {
	public void run(WebsocketReply reply);
}

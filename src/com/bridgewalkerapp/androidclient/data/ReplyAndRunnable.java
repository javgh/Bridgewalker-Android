package com.bridgewalkerapp.androidclient.data;

import com.bridgewalkerapp.androidclient.apidata.WebsocketReply;

public class ReplyAndRunnable {
	private WebsocketReply reply;
	private ParameterizedRunnable runnable;
	
	public ReplyAndRunnable(WebsocketReply reply, ParameterizedRunnable runnable) {
		this.reply = reply;
		this.runnable = runnable;
	}

	public WebsocketReply getReply() {
		return reply;
	}
	
	public ParameterizedRunnable getRunnable() {
		return runnable;
	}
}

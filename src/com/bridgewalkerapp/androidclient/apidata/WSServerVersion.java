package com.bridgewalkerapp.androidclient.apidata;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class WSServerVersion extends WebsocketReply {
	private String serverVersion;
	
	@JsonProperty("server_version")
	public void setServerVersion(String serverVersion) {
		this.serverVersion = serverVersion;
	}

	public String getServerVersion() {
		return serverVersion;
	}
	
	@Override
	public int getReplyType() {
		return TYPE_WS_SERVER_VERSION;
	}

	@Override
	public boolean isReplyTo(WebsocketRequest request) {
		return request.getRequestType() == WebsocketRequest.TYPE_REQUEST_VERSION;
	}
}

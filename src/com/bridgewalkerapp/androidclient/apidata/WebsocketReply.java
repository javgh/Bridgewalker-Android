package com.bridgewalkerapp.androidclient.apidata;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.util.Log;

public abstract class WebsocketReply {
	private static final String TAG = "com.bridgewalkerapp";
	private static final boolean DEBUG_LOG = false;
	
	public static final int TYPE_WS_SERVER_VERSION = 0;
	public static final int TYPE_WS_GUEST_ACCOUNT_CREATED = 1;
	public static final int TYPE_WS_LOGIN_SUCCESSFUL = 2;
	public static final int TYPE_WS_LOGIN_FAILED = 3;
	public static final int TYPE_WS_STATUS = 4;
	public static final int TYPE_WS_QUOTE_UNAVAILABLE = 5;
	public static final int TYPE_WS_QUOTE = 6;
	public static final int TYPE_WS_SEND_FAILED = 7;
	public static final int TYPE_WS_SEND_SUCCESSFUL = 8;
	
	private static ObjectMapper mapper = new ObjectMapper(); 
	
	abstract public int getReplyType();
	
	abstract public boolean isReplyTo(WebsocketRequest request); 
	
	public static WebsocketReply parseJSON(JsonNode json) {
		try {
			if (json.get("reply") == null)
				return null;
			
			if (json.get("reply").asText().equals("server_version")) {
				WSServerVersion wsServerVersion =
						mapper.treeToValue(json, WSServerVersion.class);
				return wsServerVersion;
			}
			
			if (json.get("reply").asText().equals("guest_account_created")) {
				WSGuestAccountCreated wsGAC =
						mapper.treeToValue(json, WSGuestAccountCreated.class);
				return wsGAC;
			}
			
			if (json.get("reply").asText().equals("login_successful")) {
				WSLoginSuccessful wsLS =
						mapper.treeToValue(json, WSLoginSuccessful.class);
				return wsLS;
			}
			
			if (json.get("reply").asText().equals("login_failed")) {
				WSLoginFailed wsLF =
						mapper.treeToValue(json, WSLoginFailed.class);
				return wsLF;
			}
			
			if (json.get("reply").asText().equals("quote_unavailable")) {
				WSQuoteUnavailable wsQU =
						mapper.treeToValue(json, WSQuoteUnavailable.class);
				return wsQU;
			}
			
			if (json.get("reply").asText().equals("quote")) {
				WSQuote wsQ =
						mapper.treeToValue(json, WSQuote.class);
				return wsQ;
			}					
			
			if (json.get("reply").asText().equals("send_failed")) {
				WSSendFailed wsSF =
						mapper.treeToValue(json, WSSendFailed.class);
				return wsSF;
			}					
			
			if (json.get("reply").asText().equals("send_successful")) {
				WSSendSuccessful wsSS =
						mapper.treeToValue(json, WSSendSuccessful.class);
				return wsSS;
			}					
			
			if (json.get("reply").asText().equals("status")) {
				JsonNode statusJSON = json.get("status");
				if (statusJSON == null)
					return null;
				
				WSStatus wsStatus =
						mapper.treeToValue(statusJSON, WSStatus.class);
				return wsStatus;
			}
		} catch (JsonParseException e) {
			if (DEBUG_LOG) Log.d(TAG, e.toString());
			/* ignore, will return null */
		} catch (JsonMappingException e) {
			if (DEBUG_LOG) Log.d(TAG, e.toString());
			/* ignore, will return null */
		} catch (IOException e) {
			if (DEBUG_LOG) Log.d(TAG, e.toString());
			/* ignore, will return null */
		}

		return null;
	}
}

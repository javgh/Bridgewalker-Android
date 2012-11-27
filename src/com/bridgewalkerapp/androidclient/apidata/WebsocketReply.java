package com.bridgewalkerapp.androidclient.apidata;

import java.io.IOException;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import android.util.Log;

public abstract class WebsocketReply {
	private static final String TAG = "com.bridgewalkerapp";
	
	public static final int TYPE_WS_SERVER_VERSION = 0;
	public static final int TYPE_WS_GUEST_ACCOUNT_CREATED = 1;
	
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
		} catch (JsonParseException e) {
			Log.d(TAG, e.toString());
			/* ignore, will return null */
		} catch (JsonMappingException e) {
			Log.d(TAG, e.toString());
			/* ignore, will return null */
		} catch (IOException e) {
			Log.d(TAG, e.toString());
			/* ignore, will return null */
		}

		return null;
	}
}

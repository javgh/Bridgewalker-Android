package com.bridgewalkerapp;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.TreeMap;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import com.bridgewalkerapp.apidata.RequestVersion;
import com.bridgewalkerapp.apidata.WSServerVersion;
import com.bridgewalkerapp.apidata.WebsocketReply;
import com.bridgewalkerapp.apidata.WebsocketRequest;
import com.bridgewalkerapp.data.ReplyAndRunnable;
import com.bridgewalkerapp.data.RequestAndRunnable;

import de.tavendo.autobahn.WebSocketConnection;
import de.tavendo.autobahn.WebSocketException;
import de.tavendo.autobahn.WebSocketHandler;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.Handler.Callback;
import android.os.RemoteException;
import android.util.Log;

public class BackendService extends Service implements Callback {
	private static final String TAG = "com.bridgewalkerapp";
	
	public static final int MSG_REGISTER_CLIENT = 1;
	public static final int MSG_UNREGISTER_CLIENT = 2;
	public static final int MSG_SEND_COMMAND = 3;
	public static final int MSG_RECEIVED_COMMAND = 4;
	public static final int MSG_EXECUTE_RUNNABLE = 5;
	public static final int MSG_REQUEST_STATUS = 6;
	public static final int MSG_CONNECTION_STATUS = 7;

	public static final int CONNECTION_STATE_PERMANENT_ERROR = -1;
	public static final int CONNECTION_STATE_CONNECTING = 0;
	public static final int CONNECTION_STATE_COMPATIBILITY_CHECKED = 1;
	public static final int CONNECTION_STATE_AUTHENTICATED = 2;
	
	public static final String BRIDGEWALKER_PREFERENCES_FILE = "bridgewalker_preferences";
	public static final String SETTING_GUEST_ACCOUNT = "SETTING_GUEST_ACCOUNT";
	public static final String SETTING_GUEST_PASSWORD = "SETTING_GUEST_PASSWORD";
	
	private static final String BRIDGEWALKER_URI = "ws://192.168.1.6:9160";
	private static final int MAX_ERROR_WAIT_TIME = 15 * 1000;
	private static final int INITIAL_ERROR_WAIT_TIME = 1 * 1000;
	
	private WebSocketConnection connection;
	private boolean isRunning = true;
	private int currentErrorWaitTime = INITIAL_ERROR_WAIT_TIME;
	private int connectionState = 0;
	
	private boolean useAuthentication = false;
	private String guestAccount = null;
	private String guestPassword = null;

	private ObjectMapper mapper;
	
	private Handler myHandler = null;
	private Map<Integer, Messenger> clientMessengers;
	
	private Messenger myMessenger = null;
	
	private Map<Integer, RequestAndRunnable> outstandingReplies;
	private Queue<WebsocketRequest> cmdQueue;
	
	@SuppressLint("UseSparseArrays")
	@Override
	public void onCreate() {
		super.onCreate();
		this.myHandler = new Handler(this);
		this.myMessenger = new Messenger(this.myHandler);
		this.clientMessengers = new TreeMap<Integer, Messenger>();
		this.outstandingReplies = new HashMap<Integer, RequestAndRunnable>();
		this.cmdQueue = new LinkedList<WebsocketRequest>();
		
		this.mapper = new ObjectMapper();
		
		this.connection = new WebSocketConnection();
		connect();
		
		Log.d(TAG, "BackendService created");
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case MSG_REGISTER_CLIENT:
				this.clientMessengers.put(msg.replyTo.hashCode(), msg.replyTo);
				Log.d(TAG, "Client registered");
				break;
			case MSG_UNREGISTER_CLIENT:
				this.clientMessengers.remove(msg.replyTo.hashCode());
				Log.d(TAG, "Client unregistered. Clients remaining: " + this.clientMessengers.size());
				break;
			case MSG_REQUEST_STATUS:
				Message replyMsg = Message.obtain(null, MSG_CONNECTION_STATUS);
				replyMsg.obj = Integer.valueOf(this.connectionState);
				try {
					msg.replyTo.send(replyMsg);
				} catch (RemoteException e) { /* ignore */ }
				break;
			case MSG_SEND_COMMAND:
				RequestAndRunnable cmd = (RequestAndRunnable)msg.obj;
				this.outstandingReplies.put(msg.replyTo.hashCode(), cmd);
				this.cmdQueue.offer(cmd.getRequest());
				sendCommands();
				break;
		}
		return true;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		Bundle extras = intent.getExtras();
		
		if (extras != null && extras.containsKey(SETTING_GUEST_ACCOUNT)) {
			this.guestAccount = extras.getString(SETTING_GUEST_ACCOUNT);
			this.guestPassword = extras.getString(SETTING_GUEST_PASSWORD);
			this.useAuthentication = true;
		} else {
			this.useAuthentication = false;
		}
		
		return myMessenger.getBinder();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		this.isRunning = false;
		this.connection.disconnect();
		Log.d(TAG, "BackendService destroyed");
	}
	
	private void connect() {
		try {
			this.connectionState = CONNECTION_STATE_CONNECTING;
			this.connection.connect(BRIDGEWALKER_URI, webSocketHandler);
		} catch (WebSocketException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void reconnect() {
		if (this.isRunning) {
			Log.d(TAG, "Lost connection; retrying in " + currentErrorWaitTime + " ms.");
			connectionState = CONNECTION_STATE_CONNECTING;
			sendToAllClients(MSG_CONNECTION_STATUS, Integer.valueOf(connectionState));
			
			Runnable initReconnect = new Runnable() {
				@Override
				public void run() { connect();	}
			};
			
			this.myHandler.postDelayed(initReconnect, currentErrorWaitTime);
			currentErrorWaitTime *= 2;
			if (currentErrorWaitTime > MAX_ERROR_WAIT_TIME)
				currentErrorWaitTime = MAX_ERROR_WAIT_TIME;
		}
	}
	
	private void sendCommands() {
		if (this.connection.isConnected()) {
			WebsocketRequest cmd;
			while ((cmd = this.cmdQueue.poll()) != null) {
				sendCommand(cmd);
			}
		}
	}
	
	private void sendCommand(Object cmd) {
		connection.sendTextMessage(asJson(cmd));
	}
	
	private void processReply(WebsocketReply reply) {
		// check for replies that are relevant for us
		if (this.connectionState < CONNECTION_STATE_COMPATIBILITY_CHECKED &&
						reply.getReplyType() == WebsocketReply.TYPE_WS_SERVER_VERSION) {
			WSServerVersion serverVersion = (WSServerVersion)reply;
			if (isServerVersionCompatible(serverVersion.getServerVersion())) {
				this.connectionState = CONNECTION_STATE_COMPATIBILITY_CHECKED;				
			} else {
				this.connectionState = CONNECTION_STATE_PERMANENT_ERROR;
				this.isRunning = false;
				this.connection.disconnect();
			}
			sendToAllClients(MSG_CONNECTION_STATUS, Integer.valueOf(this.connectionState));
		}
		
		// see if this is a reply to a specific request
		Iterator<Map.Entry<Integer, RequestAndRunnable>> it = this.outstandingReplies.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, RequestAndRunnable> entry = it.next();
			if (reply.isReplyTo(entry.getValue().getRequest())) {
				// found the client that send the request
				Messenger client = this.clientMessengers.get(entry.getKey());
				if (client != null) {
					Message msg = Message.obtain(null, MSG_EXECUTE_RUNNABLE);
					ReplyAndRunnable randr =
							new ReplyAndRunnable(reply, entry.getValue().getRunnable());
					msg.obj = randr;
					try {
						client.send(msg);
					} catch (RemoteException e) { /* ignore */ }
				}
				
				// remove from outstanding replies and return
				it.remove();
				return;
			}
		}

		// apparently not a specific reply, so just broadcast to all clients
		sendToAllClients(MSG_RECEIVED_COMMAND, reply);
	}
	
	private void sendToAllClients(int msgCode, Object msgObj) {
		for (Messenger client : this.clientMessengers.values()) {
			Message msg = Message.obtain(null, msgCode);
			msg.obj = msgObj;
			try {
				client.send(msg);
			} catch (RemoteException e) { /* ignore */ }
		}
	}
	
	private void authenticate() {
		// TODO
	}
	
    private String asJson(Object o) {
    	String json = "";
    	
    	/* We expect the caller to know what
    	 * they are doing, so won't really deal with
    	 * any potential errors.
    	 */
    	try {
			json = mapper.writeValueAsString(o);
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    	return json;
    }
    
	private boolean isServerVersionCompatible(String serverVersion) {
		int clientMajor = extractMajorVersion(RequestVersion.BRIDGEWALKER_CLIENT_VERSION);
		int serverMajor = extractMajorVersion(serverVersion);
		
		return clientMajor == serverMajor;
	}
	
	private Integer extractMajorVersion(String version) {
		String[] parts = version.split("\\.");
		return Integer.valueOf(parts[0]);
	}
	
	private WebSocketHandler webSocketHandler = new WebSocketHandler() {
		@Override
		public void onOpen() {
			Log.d(TAG, "WS: Connected to " + BRIDGEWALKER_URI);
			currentErrorWaitTime = INITIAL_ERROR_WAIT_TIME;
			
			sendCommand(new RequestVersion());
			
			sendCommands();		// send out any commands that are queued
		}
		
		@Override
		public void onTextMessage(String payload) {
			Log.d(TAG, "WS: Text message received (" + payload + ")");
			
			try {
				JsonNode json = mapper.readValue(payload, JsonNode.class);
				if (json == null)
					return;
				
				WebsocketReply wsReply = WebsocketReply.parseJSON(json);
				if (wsReply == null)
					return;
				
				processReply(wsReply);
			} catch (JsonParseException e) {
				/* ignore malformed reply */
			} catch (JsonMappingException e) {
				/* ignore malformed reply */
			} catch (IOException e) {
				/* ignore malformed reply */
			}
		}
		
		@Override
		public void onClose(int code, String reason) {
			Log.d(TAG, "WS: Connection lost.");
			reconnect();
		}
	};
}

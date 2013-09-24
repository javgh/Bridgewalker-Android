package com.bridgewalkerapp.androidclient;

import java.util.LinkedList;
import java.util.List;

import com.bridgewalkerapp.androidclient.apidata.WebsocketRequest;
import com.bridgewalkerapp.androidclient.data.ParameterizedRunnable;
import com.bridgewalkerapp.androidclient.data.RequestAndRunnable;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.os.Handler.Callback;
import android.os.Messenger;

public class ServiceUtils {
	private Handler handler = null;
	private Messenger myMessenger = null;
	
	private Messenger serviceMessenger = null;
	private boolean isServiceBound = false;
	
	private SharedPreferences settings;
	private Context context;
	
	private List<Message> commandQueue = null;
	
	public ServiceUtils(Callback parent, SharedPreferences settings, Context context) {
		this.handler = new Handler(parent);
		this.myMessenger = new Messenger(this.handler);
		this.settings = settings;
		this.context = context;
		this.commandQueue = new LinkedList<Message>();
	}
	
	public boolean hasCredentials() {
		return this.settings.contains(BackendService.SETTING_GUEST_ACCOUNT);
	}
	
	public void bindService() {
		Intent intent = new Intent(this.context, BackendService.class);
		
		if (this.settings.contains(BackendService.SETTING_GUEST_ACCOUNT)) {
			intent.putExtra(BackendService.SETTING_GUEST_ACCOUNT,
					this.settings.getString(BackendService.SETTING_GUEST_ACCOUNT, null));
			intent.putExtra(BackendService.SETTING_GUEST_PASSWORD,
					this.settings.getString(BackendService.SETTING_GUEST_PASSWORD, null));
		}
		
		// Start service before also binding to it, so that it
		// can stick around for a little longer even after we unbind
		// again. In that way, it will be available, if we need it again
		// shortly afterwards.
		this.context.startService(intent);
		this.context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void unbindService() {
		if (this.isServiceBound) {
			try {
				Message msg = Message.obtain(null, BackendService.MSG_UNREGISTER_CLIENT);
				msg.replyTo = myMessenger;
				serviceMessenger.send(msg);
			} catch (RemoteException e) { /* can be ignored */ }
			
			this.context.unbindService(serviceConnection);
			isServiceBound = false;
		}
	}
	
	public void sendCommand(int what) {
		Message msg = Message.obtain(null, what);
		msg.replyTo = this.myMessenger;
		
		if (this.serviceMessenger != null) {
			try {
				this.serviceMessenger.send(msg);
			} catch (RemoteException e) {
				this.commandQueue.add(msg);
			}
		} else {
			this.commandQueue.add(msg);
		}
	}
	
	public void sendCommand(WebsocketRequest request, ParameterizedRunnable runnable) {
		Message msg = Message.obtain(null, BackendService.MSG_SEND_COMMAND);
		msg.replyTo = this.myMessenger;
		RequestAndRunnable randr = new RequestAndRunnable(request, runnable);
		msg.obj = randr;
		
		if (this.isServiceBound && this.serviceMessenger != null) { 
			try {
				this.serviceMessenger.send(msg);
			} catch (RemoteException e) {
				this.commandQueue.add(msg);
			}
		} else {
			this.commandQueue.add(msg);
		}
		
	}
	
	public boolean isServiceBound() {
		return isServiceBound;
	}
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			serviceMessenger = new Messenger(service);
			isServiceBound = true;
			
			try {
				Message msg = Message.obtain(null, BackendService.MSG_REGISTER_CLIENT);
				msg.replyTo = myMessenger;
				serviceMessenger.send(msg);
				
				Message msg2 = Message.obtain(null, BackendService.MSG_REQUEST_CONNECTION_STATUS);
				msg2.replyTo = myMessenger;
				serviceMessenger.send(msg2);
			} catch (RemoteException e) {
				/* can be ignored, as we should be automatically reconnected */
			}
			
			List<Message> cmdQueue = commandQueue;
			commandQueue = new LinkedList<Message>();
			
			for (Message msg3: cmdQueue) {
				try {
					serviceMessenger.send(msg3);
				} catch (RemoteException e) {
					throw new RuntimeException("Background service disappeared during critical phase.", e);
				}
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			isServiceBound = false;
			serviceMessenger = null;
		}
	};
}

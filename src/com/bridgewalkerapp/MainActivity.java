package com.bridgewalkerapp;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.TextView;

public class MainActivity extends Activity implements Callback {
	private TextView debugTextView;
	
	private Handler handler = null;
	private Messenger myMessenger = null;
	
	private Messenger serviceMessenger = null;
	private boolean isServiceBound = false;
	
	private SharedPreferences settings;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.debugTextView = (TextView)findViewById(R.id.hello_textview);
        this.settings = getSharedPreferences(BackendService.BRIDGEWALKER_PREFERENCES_FILE, 0);
        this.handler = new Handler(this);
        this.myMessenger = new Messenger(this.handler);
    }

	@Override
	protected void onStart() {
		super.onStart();
		
		if (!this.settings.contains(BackendService.SETTING_GUEST_ACCOUNT)) {
			throw new RuntimeException("MainActivity launched without existing guest account (?)");
		}
		
		bindService(new Intent(this, BackendService.class), serviceConnection,
				Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		if (isServiceBound) {
			try {
				Message msg = Message.obtain(null, BackendService.MSG_UNREGISTER_CLIENT);
				msg.replyTo = myMessenger;
				serviceMessenger.send(msg);
			} catch (RemoteException e) { /* can be ignored */ }
			
			unbindService(serviceConnection);
			isServiceBound = false;
		}
	}

	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case BackendService.MSG_CONNECTION_STATUS:
				int status = (Integer)msg.obj;
				if (status == BackendService.CONNECTION_STATE_COMPATIBILITY_CHECKED) {
					debugTextView.setText("Connected");
				}
				break;
		}
		return false;
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
				
				Message msg2 = Message.obtain(null, BackendService.MSG_REQUEST_STATUS);
				msg2.replyTo = myMessenger;
				serviceMessenger.send(msg2);
			} catch (RemoteException e) {
				/* can be ignored, as we should be automatically reconnected */
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			isServiceBound = false;
			serviceMessenger = null;
		}
	};
}

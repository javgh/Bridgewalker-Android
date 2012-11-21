package com.bridgewalkerapp;

import com.bridgewalkerapp.apidata.RequestVersion;
import com.bridgewalkerapp.apidata.WSServerVersion;
import com.bridgewalkerapp.apidata.WebsocketReply;
import com.bridgewalkerapp.apidata.WebsocketRequest;
import com.bridgewalkerapp.data.ParameterizedRunnable;
import com.bridgewalkerapp.data.ReplyAndRunnable;
import com.bridgewalkerapp.data.RequestAndRunnable;

import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoginActivity extends Activity implements Callback {
	private static final String TAG = "com.bridgewalkerapp";
	
	private Handler handler = null;
	private Messenger myMessenger = null;
	
	private Messenger serviceMessenger = null;
	private boolean isServiceBound = false;
	
	private ProgressBar loginProgressBar;
	private LinearLayout loginButtonsLayout;
	private TextView oldVersionTextView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.loginProgressBar = (ProgressBar)findViewById(R.id.login_progressbar);
        this.loginButtonsLayout = (LinearLayout)findViewById(R.id.login_buttons_layout);
        this.oldVersionTextView = (TextView)findViewById(R.id.old_version_textview);
        this.handler = new Handler(this);
        this.myMessenger = new Messenger(this.handler);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login, menu);
        return true;
    }
    
	@Override
	protected void onStart() {
		super.onStart();
		Log.d(TAG, "LoginActivity: binding service");
		bindService(new Intent(this, BackendService.class), serviceConnection,
				Context.BIND_AUTO_CREATE);
		this.loginButtonsLayout.setVisibility(View.INVISIBLE);
		this.oldVersionTextView.setVisibility(View.INVISIBLE);
		this.loginProgressBar.setVisibility(View.VISIBLE);
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		Log.d(TAG, "LoginActivity: stopping");
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
	
	private boolean isServerVersionCompatible(String serverVersion) {
		int clientMajor = extractMajorVersion(RequestVersion.BRIDGEWALKER_CLIENT_VERSION);
		int serverMajor = extractMajorVersion(serverVersion);
		
		return clientMajor == serverMajor;
	}
	
	private Integer extractMajorVersion(String version) {
		String[] parts = version.split("\\.");
		return Integer.valueOf(parts[0]);
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case BackendService.MSG_EXECUTE_RUNNABLE:
				ReplyAndRunnable randr = (ReplyAndRunnable)msg.obj;
				randr.getRunnable().run(randr.getReply());
				return true;
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
				
				Message msg2 = Message.obtain(null, BackendService.MSG_SEND_COMMAND);
				msg2.replyTo = myMessenger;
				WebsocketRequest request = new RequestVersion();
				ParameterizedRunnable runnable = new ParameterizedRunnable() {
					@Override
					public void run(WebsocketReply reply) {
						loginProgressBar.setVisibility(View.INVISIBLE);
						
						WSServerVersion serverVersion = (WSServerVersion)reply;
						if (isServerVersionCompatible(serverVersion.getServerVersion())) {
							loginButtonsLayout.setVisibility(View.VISIBLE);
						} else {
							oldVersionTextView.setVisibility(View.VISIBLE);
						}
					}
				};
				RequestAndRunnable randr = new RequestAndRunnable(request, runnable);
				msg2.obj = randr;
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

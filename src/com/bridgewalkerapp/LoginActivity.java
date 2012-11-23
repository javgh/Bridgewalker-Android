package com.bridgewalkerapp;

import com.bridgewalkerapp.apidata.CreateGuestAccount;
import com.bridgewalkerapp.apidata.WSGuestAccountCreated;
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
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
	private Button guestLoginButton;
	
	private SharedPreferences settings;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.loginProgressBar = (ProgressBar)findViewById(R.id.login_progressbar);
        this.loginButtonsLayout = (LinearLayout)findViewById(R.id.login_buttons_layout);
        this.oldVersionTextView = (TextView)findViewById(R.id.old_version_textview);
        this.guestLoginButton = (Button)findViewById(R.id.guest_login_button);
        this.guestLoginButton.setOnClickListener(this.guestLoginButtonOnClickListener);
        this.handler = new Handler(this);
        this.myMessenger = new Messenger(this.handler);
        this.settings = getSharedPreferences(BackendService.BRIDGEWALKER_PREFERENCES_FILE, 0);
    }

	@Override
	protected void onStart() {
		super.onStart();
		
		if (!this.settings.contains(BackendService.SETTING_GUEST_ACCOUNT)) {
			switchToMainActivity();
		} else {
			Log.d(TAG, "LoginActivity: binding service");
			bindService(new Intent(this, BackendService.class), serviceConnection,
					Context.BIND_AUTO_CREATE);
			showProgressBar();
		}
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
	
	private void switchToMainActivity() {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);		
	}
	
	private void showProgressBar() {
		this.loginButtonsLayout.setVisibility(View.INVISIBLE);
		this.oldVersionTextView.setVisibility(View.INVISIBLE);
		this.loginProgressBar.setVisibility(View.VISIBLE);
	}
	
	private void hideProgressBar(boolean showButtons) {
		loginProgressBar.setVisibility(View.INVISIBLE);
		if (showButtons) {
			loginButtonsLayout.setVisibility(View.VISIBLE);
		} else {
			oldVersionTextView.setVisibility(View.VISIBLE);
		}
	}
	
	private OnClickListener guestLoginButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (isServiceBound) {
				showProgressBar();
				
				Message msg = draftCommand(new CreateGuestAccount(), new ParameterizedRunnable() {
					@Override
					public void run(WebsocketReply reply) {
						WSGuestAccountCreated gac = (WSGuestAccountCreated)reply;
						
						SharedPreferences.Editor editor = settings.edit();
						editor.putString(BackendService.SETTING_GUEST_ACCOUNT,
												gac.getAccountName());
						editor.putString(BackendService.SETTING_GUEST_PASSWORD,
								gac.getAccountPassword());
						editor.commit();
						
						switchToMainActivity();
					}
				});
				try {
					serviceMessenger.send(msg);
				} catch (RemoteException e) {
					throw new RuntimeException(
							"Exception while sending CreateGuestAccount command", e);
				}
			}
		}
	};
	
	private Message draftCommand(WebsocketRequest request, ParameterizedRunnable runnable) {
		Message msg = Message.obtain(null, BackendService.MSG_SEND_COMMAND);
		msg.replyTo = myMessenger;
		RequestAndRunnable randr = new RequestAndRunnable(request, runnable);
		msg.obj = randr;
		return msg;
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case BackendService.MSG_EXECUTE_RUNNABLE:
				ReplyAndRunnable randr = (ReplyAndRunnable)msg.obj;
				randr.getRunnable().run(randr.getReply());
				return true;
			case BackendService.MSG_CONNECTION_STATUS:
				int status = (Integer)msg.obj;
				Log.d(TAG, "Connection state is: " + status);
				if (status == BackendService.CONNECTION_STATE_CONNECTING) {
					showProgressBar();
				} else if (status == BackendService.CONNECTION_STATE_PERMANENT_ERROR) {
					hideProgressBar(false);
				} else if (status >= BackendService.CONNECTION_STATE_COMPATIBILITY_CHECKED) {
					hideProgressBar(true);
				}
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

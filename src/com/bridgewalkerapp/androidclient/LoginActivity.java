package com.bridgewalkerapp.androidclient;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.bridgewalkerapp.androidclient.apidata.CreateGuestAccount;
import com.bridgewalkerapp.androidclient.apidata.WSGuestAccountCreated;
import com.bridgewalkerapp.androidclient.apidata.WebsocketReply;
import com.bridgewalkerapp.androidclient.data.ParameterizedRunnable;
import com.bridgewalkerapp.androidclient.data.ReplyAndRunnable;

import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.RemoteException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class LoginActivity extends SherlockFragmentActivity implements Callback {
	private static final String TAG = "com.bridgewalkerapp";
	
	private ProgressBar loginProgressBar;
	private LinearLayout loginButtonsLayout;
	private TextView oldVersionTextView;
	private Button guestLoginButton;
	
	private SharedPreferences settings;
	
	private ServiceUtils serviceUtils;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        this.loginProgressBar = (ProgressBar)findViewById(R.id.login_progressbar);
        this.loginButtonsLayout = (LinearLayout)findViewById(R.id.login_buttons_layout);
        this.oldVersionTextView = (TextView)findViewById(R.id.old_version_textview);
        this.guestLoginButton = (Button)findViewById(R.id.guest_login_button);
        this.guestLoginButton.setOnClickListener(this.guestLoginButtonOnClickListener);
        this.settings = getSharedPreferences(BackendService.BRIDGEWALKER_PREFERENCES_FILE, 0);
        this.serviceUtils = new ServiceUtils(this, this.settings, getBaseContext());
    }

	@Override
	protected void onStart() {
		super.onStart();
		
		if (!this.settings.contains(BackendService.SETTING_GUEST_ACCOUNT)) {
			switchToMainActivity();
		} else {
			this.serviceUtils.bindService();
			showProgressBar();
		}
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		this.serviceUtils.unbindService();
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
			if (serviceUtils.isServiceBound()) {
				showProgressBar();

				try {
					serviceUtils.sendCommand(new CreateGuestAccount(), new ParameterizedRunnable() {
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
				} catch (RemoteException e) {
					throw new RuntimeException(
							"Exception while sending CreateGuestAccount command", e);
				}
			}
		}
	};
	
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
}

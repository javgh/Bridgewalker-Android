package com.bridgewalkerapp.androidclient;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

@SuppressWarnings("deprecation")	/* use old clipboard to work on API 8+ */
public class BackupActivity extends SherlockFragmentActivity {
	private TextView guestCredentialsTextview = null;
	private Button showCredentialsButton = null;
	private Button copyCredentialsButton = null;
	
	private SharedPreferences settings;
	
	private long lastBackup;
	private boolean credentialsVisible = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);
        
        this.guestCredentialsTextview = (TextView)findViewById(R.id.guest_credentials_textview);
        this.showCredentialsButton = (Button)findViewById(R.id.show_credentials_button);
        this.copyCredentialsButton = (Button)findViewById(R.id.copy_credentials_button);
        
        this.settings = getSharedPreferences(BackendService.BRIDGEWALKER_PREFERENCES_FILE, 0);
        
        lastBackup = settings.getLong(BackendService.SETTING_LAST_BACKUP, -1);
        hideCredentials();
        
        this.showCredentialsButton.setOnClickListener(this.showCredentialsButtonOnClickListener);
        this.copyCredentialsButton.setOnClickListener(this.copyCredentialsButtonOnClickListener);
        
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		if (!this.settings.contains(BackendService.SETTING_GUEST_ACCOUNT)) {
			switchToLoginActivity();
		}
	}
	
	private void switchToLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);		
	}
	
	private String formatLastBackup(long lastBackup) {
		if (lastBackup == -1) {
			return getString(R.string.never);
		} else {
			return formatRelativeTimeSpan(lastBackup);
		}
	}
	
	private String formatRelativeTimeSpan(long timestamp) {
		/* This could ideally use DateUtils.getRelativeTimeSpanString(),
		 * but it does not seem to be possible to override the locale used,
		 * which makes for a weird mix of system language and app language
		 */
		
		long diff = (System.currentTimeMillis() / 1000) - (timestamp / 1000); // seconds
		
		if (diff > 24 * 60 * 60) {
			return getString(R.string.timespan_days);
		} else if (diff > 60 * 60) {
			return getString(R.string.timespan_hours);
		} else if (diff > 60) {
			return getString(R.string.timespan_minutes);
		} else if (diff > 5) {
			return getString(R.string.timespan_seconds);
		} else {
			return getString(R.string.timespan_now);
		}
	}
	
	private void hideCredentials() {
		String credentials = getString(R.string.guest_credentials,
											"********", "********", formatLastBackup(lastBackup));
        this.guestCredentialsTextview.setText(credentials);
		
		this.showCredentialsButton.setText(getString(R.string.show_credentials));
		this.credentialsVisible = false;
	}
	
	private void showCredentials() {
		/* get credentials */
		String account = this.settings.getString(BackendService.SETTING_GUEST_ACCOUNT, "");
		String password = this.settings.getString(BackendService.SETTING_GUEST_PASSWORD, "");
		
		/* update last accessed */
		this.lastBackup = System.currentTimeMillis();
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(BackendService.SETTING_LAST_BACKUP, this.lastBackup);
		editor.commit();
	
		/* display credentials */
		String credentials = getString(R.string.guest_credentials,
											account, password, formatLastBackup(lastBackup));
		this.guestCredentialsTextview.setText(credentials);
		
		this.showCredentialsButton.setText(getString(R.string.hide_credentials));
		this.credentialsVisible = true;	
	}
	
	private void copyCredentials() {
		String account = this.settings.getString(BackendService.SETTING_GUEST_ACCOUNT, "");
		String password = this.settings.getString(BackendService.SETTING_GUEST_PASSWORD, "");
		
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		clipboard.setText(account + ":" + password);
		Toast.makeText(this, R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();	
	}
	
	private OnClickListener showCredentialsButtonOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if (credentialsVisible) {
				hideCredentials();
			} else {
				showCredentials();
			}
		}
	};
	
	private OnClickListener copyCredentialsButtonOnClickListener = new OnClickListener() {
		
		@Override
		public void onClick(View v) {
			showCredentials();
			copyCredentials();
		}
	};
}

package com.bridgewalkerapp.androidclient;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.bridgewalkerapp.androidclient.SendConfirmationDialogFragment.SendConfirmationDialogListener;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;

public class MainActivity extends SherlockFragmentActivity implements Callback, BitcoinFragmentHost, SendConfirmationDialogListener {
	public static final String BITCOIN_FRAGMENT_HOST = "BITCOIN_FRAGMENT_HOST";
	
	private ServiceUtils serviceUtils;
	
	private BitcoinFragment currentFragment = null;
	
	private BitcoinURI btcURI = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.serviceUtils = new ServiceUtils(this,
        		getSharedPreferences(BackendService.BRIDGEWALKER_PREFERENCES_FILE, 0),
        		getBaseContext());
        
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        
        actionBar.addTab(actionBar.newTab()
        		.setText(R.string.send_tab_label)
        		.setTabListener(new TabListenerUtils<SendFragment>(
        				this, "send", SendFragment.class)));
        
        actionBar.addTab(actionBar.newTab()
        		.setText(R.string.receive_tab_label)
        		.setTabListener(new TabListenerUtils<ReceiveFragment>(
        				this, "receive", ReceiveFragment.class)));
        
        /* check for bitcoin: intent */
        Intent intent = getIntent();
        String action = intent.getAction();
        Uri intentUri = intent.getData();
        if (Intent.ACTION_VIEW.equals(action) && intentUri != null
        		&& "bitcoin".equals(intentUri.getScheme())) {
        	this.btcURI = BitcoinURI.parse(intentUri.toString());
        }
    }

	@Override
	protected void onStart() {
		super.onStart();
		
		if (!this.serviceUtils.hasCredentials())
			switchToLoginActivity();
		
		this.serviceUtils.bindService();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		this.serviceUtils.unbindService();
	}
	
	private void switchToLoginActivity() {
		Intent intent = new Intent(this, LoginActivity.class);
		startActivity(intent);		
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		if (this.currentFragment != null) {
			return this.currentFragment.handleMessage(msg);
		} else {
			return false;
		}
	}
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (this.currentFragment != null) {
			this.currentFragment.onActivityResult(requestCode, resultCode, intent);
		}
	}

	@Override
	public void registerFragment(BitcoinFragment fragment) {
		this.currentFragment = fragment;
		
		/* pass on a Bitcoin URI, if we received one earlier */
		if (this.btcURI != null) {
			this.currentFragment.handleBitcoinURI(this.btcURI);
			this.btcURI = null;
		}
	}

	@Override
	public void deregisterFragment(BitcoinFragment fragment) {
		this.currentFragment = null;
	}

	@Override
	public ServiceUtils getServiceUtils() {
		return serviceUtils;
	}

	@Override
	public void onDialogPositiveClick() {
		if (this.currentFragment != null) {
			this.currentFragment.onDialogPositiveClick();
		}
	}

	@Override
	public void onDialogNegativeClick() {
		if (this.currentFragment != null) {
			this.currentFragment.onDialogNegativeClick();
		}
	}
}

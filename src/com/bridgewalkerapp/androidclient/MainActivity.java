package com.bridgewalkerapp.androidclient;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;

import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;

public class MainActivity extends SherlockFragmentActivity implements Callback, BitcoinFragmentHost {
	public static final String BITCOIN_FRAGMENT_HOST = "BITCOIN_FRAGMENT_HOST";
	
	private ServiceUtils serviceUtils;
	
	private Callback currentFragment = null;
	
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
        //actionBar.setSelectedNavigationItem(1);
    }

	@Override
	protected void onStart() {
		super.onStart();
		this.serviceUtils.bindService();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		this.serviceUtils.unbindService();
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
	public void registerFragment(Callback fragment) {
		this.currentFragment = fragment;
		
	}

	@Override
	public void deregisterFragment(Callback fragment) {
		this.currentFragment = null;
	}

	@Override
	public ServiceUtils getServiceUtils() {
		return serviceUtils;
	}
}

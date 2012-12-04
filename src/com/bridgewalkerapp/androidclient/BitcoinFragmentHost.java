package com.bridgewalkerapp.androidclient;

import android.os.Handler.Callback;

public interface BitcoinFragmentHost {
	public void registerFragment(Callback fragment);
	public void deregisterFragment(Callback fragment);
	
	public ServiceUtils getServiceUtils();
}

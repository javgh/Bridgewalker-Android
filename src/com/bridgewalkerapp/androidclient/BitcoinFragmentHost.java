package com.bridgewalkerapp.androidclient;

public interface BitcoinFragmentHost {
	public void registerFragment(BitcoinFragment fragment);
	public void deregisterFragment(BitcoinFragment fragment);
	
	public ServiceUtils getServiceUtils();
}

package com.bridgewalkerapp.androidclient;

import android.content.Intent;
import android.os.Handler.Callback;

public interface BitcoinFragment extends Callback {
	public void onActivityResult(int requestCode, int resultCode, Intent intent);
}

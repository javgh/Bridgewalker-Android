package com.bridgewalkerapp.androidclient;

import com.bridgewalkerapp.androidclient.SendConfirmationDialogFragment.SendConfirmationDialogListener;

import android.content.Intent;
import android.os.Handler.Callback;

public interface BitcoinFragment extends Callback, SendConfirmationDialogListener {
	public void onActivityResult(int requestCode, int resultCode, Intent intent);
}

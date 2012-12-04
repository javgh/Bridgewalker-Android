package com.bridgewalkerapp.androidclient;

import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.actionbarsherlock.app.SherlockFragment;

public class SendFragment extends SherlockFragment implements Callback {
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_send, container, false);
	}

	@Override
	public boolean handleMessage(Message msg) {
		return false;
	}
}

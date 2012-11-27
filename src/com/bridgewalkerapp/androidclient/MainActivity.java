package com.bridgewalkerapp.androidclient;

import com.bridgewalkerapp.R;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.widget.TextView;

public class MainActivity extends Activity implements Callback {
	private TextView debugTextView;
	
	private ServiceUtils serviceUtils;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.debugTextView = (TextView)findViewById(R.id.hello_textview);
        this.serviceUtils = new ServiceUtils(this,
        		getSharedPreferences(BackendService.BRIDGEWALKER_PREFERENCES_FILE, 0),
        		getBaseContext());
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
		switch (msg.what) {
			case BackendService.MSG_CONNECTION_STATUS:
				int status = (Integer)msg.obj;
				if (status == BackendService.CONNECTION_STATE_COMPATIBILITY_CHECKED) {
					debugTextView.setText("Connected");
				}
				break;
		}
		return false;
	}
}

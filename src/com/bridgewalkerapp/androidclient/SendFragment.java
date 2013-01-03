package com.bridgewalkerapp.androidclient;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SendFragment extends BalanceFragment {
	private EditText recipientAddressEditText = null;
	private Button scanButton = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_send, container, false);
		
		this.progressBar = (ProgressBar)view.findViewById(R.id.send_fragment_progressbar);
		this.contentLinearLayout = (LinearLayout)view.findViewById(R.id.send_fragment_content_linearlayout);
		this.usdBalanceTextView = (TextView)view.findViewById(R.id.send_fragment_usd_balance_textview);
		this.pendingEventsTextView = (TextView)view.findViewById(R.id.send_fragment_pending_events_textview);
		this.recipientAddressEditText = (EditText)view.findViewById(R.id.receive_bitcoin_address_textview);
		this.scanButton = (Button)view.findViewById(R.id.scan_button);
		
		this.scanButton.setOnClickListener(this.scanButtonOnClickListener);
		
		return view;
	}

	@Override
	protected void displayStatusHook() {
		/* do nothing */
	}
	
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    	if (scanResult != null) {
    		BitcoinURI btcURI = BitcoinURI.parse(scanResult.getContents());
    		if (btcURI != null) {
    			this.recipientAddressEditText.setText(btcURI.getAddress());
    		}
    	}
    }	
	
	private OnClickListener scanButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
	    	IntentIntegrator integrator = new IntentIntegrator(getSherlockActivity());
	    	integrator.initiateScan();
		}
	};
}

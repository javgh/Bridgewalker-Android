package com.bridgewalkerapp.androidclient;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.RemoteException;
import android.text.ClipboardManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;
import com.bridgewalkerapp.androidclient.apidata.RequestStatus;
import com.bridgewalkerapp.androidclient.apidata.WSStatus;
import com.bridgewalkerapp.androidclient.apidata.WebsocketReply;
import com.bridgewalkerapp.androidclient.data.ParameterizedRunnable;
import com.bridgewalkerapp.androidclient.data.ReplyAndRunnable;

@SuppressWarnings("deprecation")	/* use old clipboard to work on API 8+ */
public class ReceiveFragment extends SherlockFragment implements Callback {
	private static final String TAG = "com.bridgewalkerapp";
	
	private ProgressBar progressBar = null;
	private LinearLayout contentLinearLayout = null;
	private TextView usdBalanceTextView = null;
	private TextView receiveBitcoinAddressTextView = null;
	private ImageView primaryBTCAddressQRCodeImageView = null;
	private Button copyAddressToClipboardButton = null;
	private ImageButton shareAddressButton = null;
	
	private BitcoinFragmentHost parentActivity = null;
	
	private WSStatus currentStatus = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_receive, container, false);
		this.progressBar = (ProgressBar)view.findViewById(R.id.receive_fragment_progressbar);
		this.contentLinearLayout = (LinearLayout)view.findViewById(R.id.receive_fragment_content_linearlayout);
		this.usdBalanceTextView = (TextView)view.findViewById(R.id.usd_balance_textview);
		this.receiveBitcoinAddressTextView = (TextView)view.findViewById(R.id.receive_bitcoin_address_textview);
		this.primaryBTCAddressQRCodeImageView = (ImageView)view.findViewById(R.id.primary_btc_address_qrcode_imageview);
		this.copyAddressToClipboardButton = (Button)view.findViewById(R.id.copy_address_to_clipboard_button);
		this.shareAddressButton = (ImageButton)view.findViewById(R.id.share_address_button);
		
		this.copyAddressToClipboardButton.setOnClickListener(this.copyAddressToClipboardButtonOnClickListener);
		
		return view; 
	}
	
	@Override
	public void onStart() {
		super.onStart();
		this.parentActivity = (BitcoinFragmentHost)getActivity();
		this.parentActivity.registerFragment(this);
		
		if (this.currentStatus == null) {
			try {
				this.parentActivity.getServiceUtils().sendCommand(BackendService.MSG_REQUEST_STATUS);
			} catch (RemoteException e) { /* ignore */ }
		} else {
			displayStatus();
		}
	}
	
	@Override
	public boolean handleMessage(Message msg) {
		switch (msg.what) {
			case BackendService.MSG_EXECUTE_RUNNABLE:
				ReplyAndRunnable randr = (ReplyAndRunnable)msg.obj;
				randr.getRunnable().run(randr.getReply());
				return true;
			case BackendService.MSG_CONNECTION_STATUS:
				int status = (Integer)msg.obj;
				Log.d(TAG, "Fragment: Connection state is: " + status);
				if (status == BackendService.CONNECTION_STATE_AUTHENTICATED) {
					requestStatus();
				} else {
					showProgressBar();
					this.currentStatus = null;
				}
				return true;
		}
		return false;
	}
	
	@Override
	public void onStop() {
		super.onStop();
		this.parentActivity.deregisterFragment(this);
	}
	
	private void requestStatus() {
		if (this.currentStatus == null) {
			Log.d(TAG, "Fragment: Requesting status from server");
			try {
				this.parentActivity.getServiceUtils().sendCommand(new RequestStatus(), new ParameterizedRunnable() {
					@Override
					public void run(WebsocketReply reply) {
						currentStatus = (WSStatus)reply;
						displayStatus();
					}
				});
			} catch (RemoteException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private void displayStatus() {
		if (this.currentStatus == null)
			return;
		
		this.usdBalanceTextView.setText(
				formatUSDBalance(this.currentStatus.getUsdBalance()));
		this.receiveBitcoinAddressTextView.setText("Use this Bitcoin address to fund your account:\n" +
				this.currentStatus.getPrimaryBTCAddress());
		
		Bitmap qrCode = QRCodeUtils.encodeAsBitmap(
				"bitcoin:" + this.currentStatus.getPrimaryBTCAddress(), 500);
		this.primaryBTCAddressQRCodeImageView.setImageBitmap(qrCode);
		
		hideProgressBar();
	}
	
	private String formatUSDBalance(long usdBalance) {
		double asDouble = (double)usdBalance / 100000.0;
		return String.format("Balance: %.5f USD", asDouble);
	}
	
	private void showProgressBar() {
		this.contentLinearLayout.setVisibility(View.INVISIBLE);
		this.progressBar.setVisibility(View.VISIBLE);
	}
	
	private void hideProgressBar() {
		this.progressBar.setVisibility(View.INVISIBLE);
		this.contentLinearLayout.setVisibility(View.VISIBLE);
	}

	private OnClickListener copyAddressToClipboardButtonOnClickListener = new OnClickListener() {
		@SuppressWarnings("deprecation")	/* use old clipboard to work on API 8+ */
		@Override
		public void onClick(View v) {
			if (currentStatus != null) {
				ClipboardManager clipboard = (ClipboardManager)
						getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(currentStatus.getPrimaryBTCAddress());
				Toast.makeText(getActivity().getBaseContext(),
						R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
			}
		}
	};
}

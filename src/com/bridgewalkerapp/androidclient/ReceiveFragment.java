package com.bridgewalkerapp.androidclient;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.view.Display;
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

@SuppressWarnings("deprecation")	/* use old clipboard and getWidth() to work on API 8+ */
public class ReceiveFragment extends BalanceFragment {
	private TextView receiveBitcoinAddressTextView = null;
	private ImageView primaryBTCAddressQRCodeImageView = null;
	private ImageView fullScreenImageView = null;
	private Button copyAddressToClipboardButton = null;
	private ImageButton shareAddressButton = null;
	private LinearLayout innerContentLinearLayout = null;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_receive, container, false);
		this.progressBar = (ProgressBar)view.findViewById(R.id.receive_fragment_progressbar);
		this.contentLinearLayout = (LinearLayout)view.findViewById(R.id.receive_fragment_content_linearlayout);
		this.usdBalanceTextView = (TextView)view.findViewById(R.id.receive_fragment_usd_balance_textview);
		this.exchangeRateTextView = (TextView)view.findViewById(R.id.receive_fragment_exchange_rate_textview);
		this.pendingEventsTextView = (TextView)view.findViewById(R.id.receive_fragment_pending_events_textview);
		this.receiveBitcoinAddressTextView = (TextView)view.findViewById(R.id.receive_bitcoin_address_textview);
		this.primaryBTCAddressQRCodeImageView = (ImageView)view.findViewById(R.id.primary_btc_address_qrcode_imageview);
		this.fullScreenImageView = (ImageView)view.findViewById(R.id.fullscreen_imageview);
		this.copyAddressToClipboardButton = (Button)view.findViewById(R.id.copy_address_to_clipboard_button);
		this.shareAddressButton = (ImageButton)view.findViewById(R.id.share_address_button);
		this.innerContentLinearLayout = (LinearLayout)view.findViewById(R.id.inner_content_linearlayout);
		
		this.copyAddressToClipboardButton.setOnClickListener(this.copyAddressToClipboardButtonOnClickListener);
		this.shareAddressButton.setOnClickListener(this.shareAddressButtonOnClickListener);
		this.primaryBTCAddressQRCodeImageView.setOnClickListener(this.primaryBTCAddressQRCodeOnClickListener);
		this.fullScreenImageView.setOnClickListener(this.fullScreenOnClickListener);
		
		return view; 
	}
	
	protected void displayStatusHook() {
		this.receiveBitcoinAddressTextView.setText("Use this Bitcoin address to fund your account:\n" +
				this.currentStatus.getPrimaryBTCAddress());
		Display display = getSherlockActivity().getWindowManager().getDefaultDisplay();
		int width = display.getWidth();
		Bitmap qrCode = QRCodeUtils.encodeAsBitmap(
				"bitcoin:" + this.currentStatus.getPrimaryBTCAddress(), Math.max(500, width));
		this.primaryBTCAddressQRCodeImageView.setImageBitmap(qrCode);
		this.fullScreenImageView.setImageBitmap(qrCode);
	}

	private OnClickListener copyAddressToClipboardButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (currentStatus != null) {
				ClipboardManager clipboard = (ClipboardManager)
						getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
				clipboard.setText(currentStatus.getPrimaryBTCAddress());
				Toast.makeText(getActivity(),
						R.string.copied_to_clipboard, Toast.LENGTH_SHORT).show();
			}
		}
	};
	
	private OnClickListener shareAddressButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			if (currentStatus != null) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, currentStatus.getPrimaryBTCAddress());
				startActivity(Intent.createChooser(intent, getSherlockActivity().getString(R.string.share_bitcoin_address)));
			}
		}
	};
	
	private OnClickListener primaryBTCAddressQRCodeOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			innerContentLinearLayout.setVisibility(View.GONE);
			fullScreenImageView.setVisibility(View.VISIBLE);
		}
	};
	
	private OnClickListener fullScreenOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			fullScreenImageView.setVisibility(View.GONE);
			innerContentLinearLayout.setVisibility(View.VISIBLE);
		}
	};

	@Override
	public void handleBitcoinURI(BitcoinURI btcURI) {
		/* ignore */
	}
}

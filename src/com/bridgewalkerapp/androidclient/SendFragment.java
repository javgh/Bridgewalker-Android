package com.bridgewalkerapp.androidclient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.bridgewalkerapp.androidclient.SendConfirmationDialogFragment.SendConfirmationDialogListener;
import com.bridgewalkerapp.androidclient.apidata.RequestQuote;
import com.bridgewalkerapp.androidclient.apidata.SendPayment;
import com.bridgewalkerapp.androidclient.apidata.WSQuote;
import com.bridgewalkerapp.androidclient.apidata.WSQuoteUnavailable;
import com.bridgewalkerapp.androidclient.apidata.WSSendFailed;
import com.bridgewalkerapp.androidclient.apidata.WSSendSuccessful;
import com.bridgewalkerapp.androidclient.apidata.WebsocketReply;
import com.bridgewalkerapp.androidclient.apidata.WebsocketRequest.AmountType;
import com.bridgewalkerapp.androidclient.data.Maybe;
import com.bridgewalkerapp.androidclient.data.ParameterizedRunnable;
import com.bridgewalkerapp.androidclient.data.SendPaymentCheck;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SendFragment extends BalanceFragment implements SendConfirmationDialogListener {
	// only send new requests when either this time has
	// passed or the previous result has been received
	private static int REPEAT_REQUEST_QUOTE_INTERVAL = 3 * 1000;
	private static String INFO_DIAGRAM = "file:///android_asset/info_diagram.html";
	
	private EditText recipientAddressEditText = null;
	private EditText amountEditText = null;
	private Button scanButton = null;
	private RadioButton btcRadioButton = null;
	private RadioButton usdRadioButton = null;
	private RadioGroup currencyRadioGroup = null;
	private CheckBox feesOnTop = null;
	private WebView infoWebView = null;
	private ProgressBar sendPaymentProgressBar = null;
	private LinearLayout sendPaymentLinearLayout = null;
	private Button sendPaymentButton = null;
	private TextView sendPaymentHintTextView = null;
	private TextView exchangeStatusTextView = null;
	
	private long nextRequestId = 0;
	private long lastRequestQuoteTimestamp = 0;
	private RequestQuote lastSuccessfulRequestQuote = null;
	private WSQuote lastSuccessfulQuote = null;		// note: can be null, when the last 'successful'
													// request returned 'quote unavailable'
	
	private SendPayment lastSendPayment = null;
	
	private BluetoothAdapter bluetoothAdapter = null;
	private String lastBluetoothAddress = null;
	private BlockingQueue<Maybe<String>> bluetoothSendTaskQueue = null;
	
	private List<RequestQuote> pendingRequests = new ArrayList<RequestQuote>();
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_send, container, false);

		this.progressBar = (ProgressBar)view.findViewById(R.id.send_fragment_progressbar);
		this.contentLinearLayout = (LinearLayout)view.findViewById(R.id.send_fragment_content_linearlayout);
		this.usdBalanceTextView = (TextView)view.findViewById(R.id.send_fragment_usd_balance_textview);
		this.exchangeRateTextView = (TextView)view.findViewById(R.id.send_fragment_exchange_rate_textview);
		this.pendingEventsTextView = (TextView)view.findViewById(R.id.send_fragment_pending_events_textview);
		this.recipientAddressEditText = (EditText)view.findViewById(R.id.recipient_address_edittext);
		this.amountEditText = (EditText)view.findViewById(R.id.amount_edittext);
		this.scanButton = (Button)view.findViewById(R.id.scan_button);
		this.btcRadioButton = (RadioButton)view.findViewById(R.id.btc_radiobutton);
		this.usdRadioButton = (RadioButton)view.findViewById(R.id.usd_radiobutton);
		this.currencyRadioGroup = (RadioGroup)view.findViewById(R.id.currency_radiogroup);
		this.feesOnTop = (CheckBox)view.findViewById(R.id.fees_on_top_checkbox);
		this.infoWebView = (WebView)view.findViewById(R.id.info_webview);
		this.sendPaymentProgressBar = (ProgressBar)view.findViewById(R.id.send_payment_progressbar);
		this.sendPaymentLinearLayout = (LinearLayout)view.findViewById(R.id.send_payment_linearlayout);
		this.sendPaymentButton = (Button)view.findViewById(R.id.send_payment_button);
		this.sendPaymentHintTextView = (TextView)view.findViewById(R.id.send_payment_hint_textview);
		this.exchangeStatusTextView = (TextView)view.findViewById(R.id.exchange_status_textview);
		
		this.scanButton.setOnClickListener(this.scanButtonOnClickListener);
		this.recipientAddressEditText.addTextChangedListener(this.recipientAddressTextWatcher);
		this.amountEditText.addTextChangedListener(this.amountTextWatcher);
		this.currencyRadioGroup.setOnCheckedChangeListener(this.currencyOnCheckedChangeListener);
		this.feesOnTop.setOnCheckedChangeListener(this.feesOnTopOnCheckedChangeListener);
		this.sendPaymentButton.setOnClickListener(this.sendPaymentButtonOnClickListener);
		
		this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		this.infoWebView.setFocusable(false);
		this.infoWebView.getSettings().setJavaScriptEnabled(true);
		this.infoWebView.loadUrl(INFO_DIAGRAM);
		
		return view;
	}

	@Override
	protected void displayStatusHook() {
		if (this.currentStatus.isExchangeAvailable()) {
			this.exchangeStatusTextView.setText("");
		} else {
			this.exchangeStatusTextView.setText(getString(R.string.exchange_unavailable));
		}
	}
	
	private void setSendPaymentControlsState(boolean enabled) {
		this.recipientAddressEditText.setEnabled(enabled);
		this.scanButton.setEnabled(enabled);
		this.amountEditText.setEnabled(enabled);
		this.feesOnTop.setEnabled(enabled);
		
		for (int i = 0; i < this.currencyRadioGroup.getChildCount(); i++) {
			RadioButton rbtn = (RadioButton)this.currencyRadioGroup.getChildAt(i);
			rbtn.setEnabled(enabled);
		}
		
		if (enabled) {
			this.sendPaymentProgressBar.setVisibility(View.GONE);
			this.sendPaymentLinearLayout.setVisibility(View.VISIBLE);
		} else {
			this.sendPaymentLinearLayout.setVisibility(View.GONE);
			this.sendPaymentProgressBar.setVisibility(View.VISIBLE);
		}
	}
	
	private double parseAmount() {
		String amountStr = this.amountEditText.getText().toString();
		double amount = 0;
		try {
			amount = Double.parseDouble(amountStr);
		} catch (NumberFormatException e) { /* ignore */ }
		
		return amount;
	}
	
	private RequestQuote compileRequestQuote() {
		double amount = parseAmount();
		long adjustedAmount = 0;
		AmountType type = getAmountType();
		
		if (type == AmountType.AMOUNT_BASED_ON_BTC) {
			adjustedAmount = Math.round(amount * BackendService.BTC_BASE_AMOUNT);
		} else {
			adjustedAmount = Math.round(amount * BackendService.USD_BASE_AMOUNT);
		}
		
		return new RequestQuote(this.nextRequestId, type, adjustedAmount);
	}
	
	private void displayAndOrRequestQuote() {
		displayAndOrRequestQuote(true);
	}
	
	private void displayAndOrRequestQuote(boolean beCareful) {
		RequestQuote rq = compileRequestQuote();
		long adjustedAmount = rq.getAmount();
		
		// display old data, if available
		if (rq.isSameRequest(this.lastSuccessfulRequestQuote)) {
			displayQuote(this.lastSuccessfulQuote);
		}
		
		// do not send any requests, if we are not yet fully resumed
		if (beCareful && !isResumed())
			return;
		
		// do not send requests too fast
		if (beCareful && System.currentTimeMillis() - this.lastRequestQuoteTimestamp
				< REPEAT_REQUEST_QUOTE_INTERVAL)
			return;
		
		// do not send the same request again
		if (beCareful && rq.isSameRequest(this.lastSuccessfulRequestQuote))
			return;
		
		// do not send requests for 0
		if (adjustedAmount == 0) {
			return;
		}
		
		// add to pending requests
		this.pendingRequests.add(rq);
		this.lastRequestQuoteTimestamp = System.currentTimeMillis();
		this.nextRequestId++;
		
		this.parentActivity.getServiceUtils().sendCommand(rq, new ParameterizedRunnable() {
			@Override
			public void run(WebsocketReply reply) {
				lastRequestQuoteTimestamp = 0;
				
				if (reply.getReplyType() == WebsocketReply.TYPE_WS_QUOTE_UNAVAILABLE) {
					WSQuoteUnavailable qu = (WSQuoteUnavailable)reply;
					removePendingRequests(qu.getId(), null);
					displayQuote(null);
				} else if (reply.getReplyType() == WebsocketReply.TYPE_WS_QUOTE) {
					WSQuote quote = (WSQuote)reply;
					removePendingRequests(quote.getId(), quote);
					displayQuote(quote);
				}
				
				displayAndOrRequestQuote();  // see if we need to fire of a new request,
											 // as the user might have entered new
											 // input in the meantime
			}
		});
	}
	
	private void displayQuote(WSQuote quote) {
		if (quote != null) {
			long difference = quote.getUsdAccount() - quote.getUsdRecipient();
			double actualFee = (double)difference / (double)quote.getUsdRecipient();
			
			String feeStr = getString(R.string.quote_fee, actualFee * 100, formatUSD(difference, Rounding.NO_ROUNDING));
			String jsCall = "update_diagram("
								+ "\"+ " + formatBTC(quote.getBtc()) + " BTC\", "
								+ quote.getUsdRecipient() + ", \"+ " + formatUSD(quote.getUsdRecipient(), Rounding.ROUND_DOWN) + " EUR\", "
								+ quote.getUsdAccount() + ", \"- " + formatUSD(quote.getUsdAccount(), Rounding.ROUND_DOWN) + " EUR\", "
								+ "\"" + feeStr + "\")";
			
			updateWebView(jsCall);
		} else {
			hideWebView();
		}
			
		updateSendPaymentButton();
	}
	
	private void updateWebView(final String jsCall) {
		infoWebView.setVisibility(View.VISIBLE);
		infoWebView.loadUrl("javascript:" + jsCall);
	}
	
	private void hideWebView() {
		infoWebView.setVisibility(View.GONE);
	}
	
	private SendPaymentCheck isReadyToSendPayment() {
		if (parseAmount() == 0)
			return new SendPaymentCheck(false, "");
		
		String address = recipientAddressEditText.getText().toString();
		if (address.equalsIgnoreCase(""))
			return new SendPaymentCheck(false, "");

		// see if we have quote data to do some additional checks
		RequestQuote rq = compileRequestQuote();
		if (rq.isSameRequest(this.lastSuccessfulRequestQuote) && lastSuccessfulQuote != null) {
			if (!this.lastSuccessfulQuote.hasSufficientBalance()) {
				String hint = getString(R.string.insufficient_balance);
				return new SendPaymentCheck(false, hint);
			}
		}
		
		return new SendPaymentCheck(true, "");
	}
	
	private void updateSendPaymentButton() {
		SendPaymentCheck check = isReadyToSendPayment();
		this.sendPaymentHintTextView.setText(check.getHint());
		this.sendPaymentButton.setEnabled(check.isReady());
	}
	
	private AmountType getAmountType() {
		if (this.currencyRadioGroup.getCheckedRadioButtonId() == R.id.usd_radiobutton) {
			if (this.feesOnTop.isChecked())
				return AmountType.AMOUNT_BASED_ON_USD_BEFORE_FEES;
			else
				return AmountType.AMOUNT_BASED_ON_USD_AFTER_FEES;
		} else {
			return AmountType.AMOUNT_BASED_ON_BTC;
		}
	}
	
	/*
	 * Delete matching requests and also remove any
	 * older requests as their answers might have gotten lost.
	 * Sets the provided quote as the last successful answer. 
	 */
	private void removePendingRequests(long id, WSQuote answer) {
		Iterator<RequestQuote> it = pendingRequests.iterator();
		while (it.hasNext()) {
			RequestQuote prq = it.next();
			
			if (prq.getId() == id) {
				lastSuccessfulRequestQuote = prq;
				lastSuccessfulQuote = answer;
			}
			
			// delete matching requests and also remove any
			// older requests as their answers might have
			// gotten lost
			if (prq.getId() <= id)
				it.remove();
		}		
	}	
	
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    	if (scanResult == null || scanResult.getContents() == null)
    		return;
    	
		BitcoinURI btcURI = BitcoinURI.parse(scanResult.getContents());
		handleBitcoinURI(btcURI);
    }
	
	@Override
	public void handleBitcoinURI(BitcoinURI btcURI) {
		if (btcURI == null) return;
		
		this.recipientAddressEditText.setText(btcURI.getAddress());
		if (btcURI.getAmount() > 0) {
			this.amountEditText.setText(formatBTCForEditText(btcURI.getAmount()));
			
			if (btcURI.getCurrency().equalsIgnoreCase("EUR")) {
				this.usdRadioButton.setChecked(true);
				this.feesOnTop.setChecked(true);
			} else {
				this.btcRadioButton.setChecked(true);
			}
			
			this.lastBluetoothAddress = btcURI.getBluetoothAddress();
			
			displayAndOrRequestQuote(false);
			updateSendPaymentButton();
		}
	}
	
	private android.widget.CompoundButton.OnCheckedChangeListener feesOnTopOnCheckedChangeListener = new android.widget.CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			displayAndOrRequestQuote();
			updateSendPaymentButton();
		}
	};
	
	private android.widget.RadioGroup.OnCheckedChangeListener currencyOnCheckedChangeListener = new android.widget.RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			feesOnTop.setEnabled(checkedId == R.id.usd_radiobutton);
			if (checkedId == R.id.btc_radiobutton)
				feesOnTop.setChecked(true);
			
			displayAndOrRequestQuote();
			updateSendPaymentButton();
		}
	};
	
	private TextWatcher recipientAddressTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			updateSendPaymentButton();
		}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			/* do nothing */
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			/* do nothing */
		}
	};
	
	private TextWatcher amountTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			if (parseAmount() == 0)
				hideWebView();
			displayAndOrRequestQuote();
			updateSendPaymentButton();
		}
		
		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
			/* do nothing */
		}
		
		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			/* do nothing */
		}
	};
	
	private OnClickListener scanButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
	    	IntentIntegrator integrator = new IntentIntegrator(getSherlockActivity());
	    	integrator.initiateScan();
		}
	};
	
	private OnClickListener sendPaymentButtonOnClickListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String address = recipientAddressEditText.getText().toString();
			double amount = parseAmount();
			long adjustedAmount = 0;
			AmountType type = getAmountType();
			
			if (type == AmountType.AMOUNT_BASED_ON_BTC) {
				adjustedAmount = Math.round(amount * BackendService.BTC_BASE_AMOUNT);
			} else {
				adjustedAmount = Math.round(amount * BackendService.USD_BASE_AMOUNT);
			}
			
			// double check; should not be necessary
			// though, as the button should be disabled then
			if (!isReadyToSendPayment().isReady())
				return;
			
			// always use 0 as request id; we will not track it
			SendPayment sp = new SendPayment(0, address, type, adjustedAmount);
			
			// prepare confirmation text
			String message = "";
			WSQuote quote = null;
			if (lastSuccessfulRequestQuote != null
					&& lastSuccessfulRequestQuote.isSimilarRequest(sp)) {
				quote = lastSuccessfulQuote;
			}
			
			switch (type) {
				case AMOUNT_BASED_ON_BTC:
					if (quote == null)
						message = getString(
								R.string.send_payment_confirmation_text_based_on_btc
								, formatBTC(adjustedAmount), address);
					else
						message = getString(
								R.string.send_payment_confirmation_text_based_on_btc_with_quote
								, formatBTC(adjustedAmount)
								, formatUSD(quote.getUsdRecipient(), Rounding.NO_ROUNDING), address);
					break;
				case AMOUNT_BASED_ON_USD_BEFORE_FEES:
					if (quote == null)
						message = getString(
								R.string.send_payment_confirmation_text_based_on_usd_before_fees
								, formatUSD(adjustedAmount, Rounding.NO_ROUNDING), address);
					else
						message = getString(
								R.string.send_payment_confirmation_text_based_on_usd_before_fees_with_quote
								, formatUSD(adjustedAmount, Rounding.NO_ROUNDING)
								, formatBTC(quote.getBtc()), address);
					break;
				case AMOUNT_BASED_ON_USD_AFTER_FEES:
					if (quote == null)
						message = getString(
								R.string.send_payment_confirmation_text_based_on_usd_after_fees
								, formatUSD(adjustedAmount, Rounding.NO_ROUNDING), address);
					else
						message = getString(
								R.string.send_payment_confirmation_text_based_on_usd_after_fees_with_quote
								, formatUSD(adjustedAmount, Rounding.NO_ROUNDING)
								, formatBTC(quote.getBtc()), address);
					break;
			}
			
			lastSendPayment = sp;
			SherlockDialogFragment dialog = SendConfirmationDialogFragment.newInstance(message);
			dialog.show(getActivity().getSupportFragmentManager(), "sendconfirmation");
		}
	};

	@Override
	public void onDialogPositiveClick() {
		setSendPaymentControlsState(false);
		if (lastBluetoothAddress != null)
			maybePrepareBluetoothBroadcast(lastBluetoothAddress);
		this.parentActivity.getServiceUtils().sendCommand(lastSendPayment, new ParameterizedRunnable() {
			@Override
			public void run(WebsocketReply reply) {
				String tx = null;
				
				if (reply.getReplyType() == WebsocketReply.TYPE_WS_SEND_FAILED) {
					WSSendFailed wsSF = (WSSendFailed)reply;
					String message = getString(R.string.send_payment_error)
											+ " " + wsSF.getReason(); 
					SherlockDialogFragment dialog = ErrorMessageDialogFragment.newInstance(message);
					dialog.show(getActivity().getSupportFragmentManager(), "errormessage");
				}
				
				if (reply.getReplyType() == WebsocketReply.TYPE_WS_SEND_SUCCESSFUL) {
					recipientAddressEditText.setText("");
					amountEditText.setText("");
					
					WSSendSuccessful wsSS = (WSSendSuccessful)reply;
					tx = wsSS.getTx();
					
					Toast.makeText(getActivity()
							, R.string.send_payment_success, Toast.LENGTH_SHORT).show();
				}

				maybeBroadcastViaBluetooth(tx);
				setSendPaymentControlsState(true);
			}
		});
	}
	
	private void maybePrepareBluetoothBroadcast(final String bluetoothAddress) {
		this.bluetoothSendTaskQueue = null;
		if (bluetoothAdapter == null)
			return;
		
		if (!bluetoothAdapter.isEnabled())
			return;
		
		this.bluetoothSendTaskQueue = new LinkedBlockingQueue<Maybe<String>>();
		Runnable runnable = new BluetoothSendTask(this.bluetoothSendTaskQueue, bluetoothAdapter, bluetoothAddress);
		new Thread(runnable).start();
	}
	
	private void maybeBroadcastViaBluetooth(final String tx) {
		try {
			if (this.bluetoothSendTaskQueue != null) {
				this.bluetoothSendTaskQueue.put(new Maybe<String>(tx));
			}
		} catch (InterruptedException e) { /* ignore */ }
	}
}
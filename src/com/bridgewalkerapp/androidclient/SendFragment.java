package com.bridgewalkerapp.androidclient;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.bridgewalkerapp.androidclient.SendConfirmationDialogFragment.SendConfirmationDialogListener;
import com.bridgewalkerapp.androidclient.apidata.RequestQuote;
import com.bridgewalkerapp.androidclient.apidata.SendPayment;
import com.bridgewalkerapp.androidclient.apidata.WSQuote;
import com.bridgewalkerapp.androidclient.apidata.WSQuoteUnavailable;
import com.bridgewalkerapp.androidclient.apidata.WSSendFailed;
import com.bridgewalkerapp.androidclient.apidata.WebsocketReply;
import com.bridgewalkerapp.androidclient.apidata.WebsocketRequest.AmountType;
import com.bridgewalkerapp.androidclient.data.ParameterizedRunnable;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.RemoteException;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
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
	
	private EditText recipientAddressEditText = null;
	private EditText amountEditText = null;
	private Button scanButton = null;
	private RadioButton btcRadioButton = null;
	private RadioGroup currencyRadioGroup = null;
	private CheckBox feesOnTop = null;
	private TextView infoTextView = null;
	private Button sendPaymentButton = null;
	
	private Resources resources;
	
	private long nextRequestId = 0;
	private long lastRequestQuoteTimestamp = 0;
	private RequestQuote lastSuccessfulRequestQuote = null;
	private WSQuote lastSuccessfulQuote = null;
	
	private SendPayment lastSendPayment = null;
	
	private List<RequestQuote> pendingRequests = new ArrayList<RequestQuote>();
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_send, container, false);
		
		this.progressBar = (ProgressBar)view.findViewById(R.id.send_fragment_progressbar);
		this.contentLinearLayout = (LinearLayout)view.findViewById(R.id.send_fragment_content_linearlayout);
		this.usdBalanceTextView = (TextView)view.findViewById(R.id.send_fragment_usd_balance_textview);
		this.pendingEventsTextView = (TextView)view.findViewById(R.id.send_fragment_pending_events_textview);
		this.recipientAddressEditText = (EditText)view.findViewById(R.id.recipient_address_edittext);
		this.amountEditText = (EditText)view.findViewById(R.id.amount_edittext);
		this.scanButton = (Button)view.findViewById(R.id.scan_button);
		this.btcRadioButton = (RadioButton)view.findViewById(R.id.btc_radiobutton);
		this.currencyRadioGroup = (RadioGroup)view.findViewById(R.id.currency_radiogroup);
		this.feesOnTop = (CheckBox)view.findViewById(R.id.fees_on_top_checkbox);
		this.infoTextView = (TextView)view.findViewById(R.id.info_textview);
		this.sendPaymentButton = (Button)view.findViewById(R.id.send_payment_button);
		
		this.scanButton.setOnClickListener(this.scanButtonOnClickListener);
		this.amountEditText.addTextChangedListener(this.amountTextWatcher);
		this.currencyRadioGroup.setOnCheckedChangeListener(this.currencyOnCheckedChangeListener);
		this.feesOnTop.setOnCheckedChangeListener(this.feesOnTopOnCheckedChangeListener);
		this.sendPaymentButton.setOnClickListener(this.sendPaymentButtonOnClickListener);
		
		this.resources = getResources();
		
		return view;
	}

	@Override
	protected void displayStatusHook() {
		/* do nothing */
	}
	
	private double parseAmount() {
		String amountStr = this.amountEditText.getText().toString();
		double amount = 0;
		try {
			amount = Double.parseDouble(amountStr);
		} catch (NumberFormatException e) { /* ignore */ }
		
		return amount;
	}
	
	private void maybeRequestQuote() {
		double amount = parseAmount();
		long adjustedAmount = 0;
		AmountType type = getAmountType();
		
		if (type == AmountType.AMOUNT_BASED_ON_BTC) {
			adjustedAmount = Math.round(amount * BackendService.BTC_BASE_AMOUNT);
		} else {
			adjustedAmount = Math.round(amount * BackendService.USD_BASE_AMOUNT);
		}
		RequestQuote rq = new RequestQuote(this.nextRequestId, type, adjustedAmount);
		
		// do not send requests too fast
		if (System.currentTimeMillis() - this.lastRequestQuoteTimestamp
				< REPEAT_REQUEST_QUOTE_INTERVAL)
			return;
		
		// do not send the same request again
		if (rq.isSameRequest(lastSuccessfulRequestQuote))
			return;
		
		// do not send requests for 0
		if (adjustedAmount == 0) {
			infoTextView.setText("");
			return;
		}
		
		// add to pending requests
		this.pendingRequests.add(rq);
		this.lastRequestQuoteTimestamp = System.currentTimeMillis();
		this.nextRequestId++;
		
		try {
			this.parentActivity.getServiceUtils().sendCommand(rq, new ParameterizedRunnable() {
				@Override
				public void run(WebsocketReply reply) {
					lastRequestQuoteTimestamp = 0;
					
					if (reply.getReplyType() == WebsocketReply.TYPE_WS_QUOTE_UNAVAILABLE) {
						WSQuoteUnavailable qu = (WSQuoteUnavailable)reply;
						removePendingRequests(qu.getId(), null);
						
						infoTextView.setText("Sorry, I was unable to get a quote.");
						sendPaymentButton.setEnabled(true);
					}
					
					if (reply.getReplyType() == WebsocketReply.TYPE_WS_QUOTE) {
						WSQuote quote = (WSQuote)reply;
						removePendingRequests(quote.getId(), quote);
						
						double actualFee =
								(double)(quote.getUsdAccount() - quote.getUsdRecipient())
									/ (double)quote.getUsdRecipient();
						String infoText = resources.getString(
								R.string.quote_info_text
								, formatBTC(quote.getBtc())
								, formatUSD(quote.getUsdRecipient())
								, formatUSD(quote.getUsdAccount())
								, actualFee * 100);
						infoTextView.setText(infoText);
						sendPaymentButton.setEnabled(quote.hasSufficientBalance());
					}
					
					maybeRequestQuote(); // see if we need to fire of a new request,
										 // as the user might have entered new
										 // input in the meantime
				}
			});
		} catch (RemoteException e) { /* ignore */ }
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
		if (btcURI != null) {
			this.recipientAddressEditText.setText(btcURI.getAddress());
			if (btcURI.getAmount() > 0) {
				this.amountEditText.setText(formatBTCForEditText(btcURI.getAmount()));
				this.btcRadioButton.setChecked(true);
			}
		}
    }
	
	private android.widget.CompoundButton.OnCheckedChangeListener feesOnTopOnCheckedChangeListener = new android.widget.CompoundButton.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			maybeRequestQuote();
		}
	};
	
	private android.widget.RadioGroup.OnCheckedChangeListener currencyOnCheckedChangeListener = new android.widget.RadioGroup.OnCheckedChangeListener() {
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			feesOnTop.setEnabled(checkedId == R.id.usd_radiobutton);
			if (checkedId == R.id.btc_radiobutton)
				feesOnTop.setChecked(true);
			
			maybeRequestQuote();
		}
	};
	
	private TextWatcher amountTextWatcher = new TextWatcher() {
		@Override
		public void afterTextChanged(Editable s) {
			maybeRequestQuote();
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
			
			// do some checks; should not be necessary
			// though, as the button should be disabled then
			if (address.equalsIgnoreCase(""))
				return;
			if (adjustedAmount == 0)
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
						message = resources.getString(
								R.string.send_payment_confirmation_text_based_on_btc
								, formatBTC(adjustedAmount), address);
					else
						message = resources.getString(
								R.string.send_payment_confirmation_text_based_on_btc_with_quote
								, formatBTC(adjustedAmount)
								, formatUSD(quote.getUsdRecipient()), address);
					break;
				case AMOUNT_BASED_ON_USD_BEFORE_FEES:
					if (quote == null)
						message = resources.getString(
								R.string.send_payment_confirmation_text_based_on_usd_before_fees
								, formatBTC(adjustedAmount), address);
					else
						message = resources.getString(
								R.string.send_payment_confirmation_text_based_on_usd_before_fees_with_quote
								, formatUSD(adjustedAmount)
								, formatBTC(quote.getBtc()), address);
					break;
				case AMOUNT_BASED_ON_USD_AFTER_FEES:
					if (quote == null)
						message = resources.getString(
								R.string.send_payment_confirmation_text_based_on_usd_after_fees
								, formatBTC(adjustedAmount), address);
					else
						message = resources.getString(
								R.string.send_payment_confirmation_text_based_on_usd_after_fees_with_quote
								, formatUSD(adjustedAmount)
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
		try {
			this.parentActivity.getServiceUtils().sendCommand(lastSendPayment, new ParameterizedRunnable() {
				@Override
				public void run(WebsocketReply reply) {
					if (reply.getReplyType() == WebsocketReply.TYPE_WS_SEND_FAILED) {
						WSSendFailed wsSF = (WSSendFailed)reply;
						String message = resources.getString(R.string.send_payment_error)
												+ " " + wsSF.getReason(); 
						SherlockDialogFragment dialog = ErrorMessageDialogFragment.newInstance(message);
						dialog.show(getActivity().getSupportFragmentManager(), "errormessage");
					}
					
					if (reply.getReplyType() == WebsocketReply.TYPE_WS_SEND_SUCCESSFUL) {
						Toast.makeText(getActivity()
								, R.string.send_payment_success, Toast.LENGTH_SHORT).show();
					}
				}
			});
		} catch (RemoteException e) {
			Toast.makeText(getActivity()
					, R.string.send_payment_generic_error, Toast.LENGTH_SHORT).show();
		}
	}
}
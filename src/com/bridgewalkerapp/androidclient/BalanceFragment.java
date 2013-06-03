package com.bridgewalkerapp.androidclient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.res.Resources;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.bridgewalkerapp.androidclient.apidata.WSStatus;
import com.bridgewalkerapp.androidclient.apidata.subcomponents.PendingTransaction;
import com.bridgewalkerapp.androidclient.data.ReplyAndRunnable;

abstract public class BalanceFragment extends SherlockFragment implements BitcoinFragment {
	private static final String TAG = "com.bridgewalkerapp";
	private static final boolean DEBUG_LOG = false;
	public static enum Rounding { ROUND_DOWN, NO_ROUNDING }; 
	
	protected ProgressBar progressBar = null;
	protected LinearLayout contentLinearLayout = null;
	protected TextView usdBalanceTextView = null;
	protected TextView pendingEventsTextView = null;
	
	protected BitcoinFragmentHost parentActivity = null;
		
	protected WSStatus currentStatus = null;
	
	protected Resources resources = null;
	
	@Override
	public void onStart() {
		super.onStart();
		this.parentActivity = (BitcoinFragmentHost)getActivity();
		this.parentActivity.registerFragment(this);
		
		requestStatus();	// always request current status, in case updates
							// happened, while the fragment was not displayed
		displayStatus();	// if we have status, display that already
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
				if (DEBUG_LOG) Log.d(TAG, "Fragment: Connection state is: " + status);
				if (status != BackendService.CONNECTION_STATE_AUTHENTICATED) {
					showProgressBar();
					this.currentStatus = null;
				}
				return true;
			case BackendService.MSG_ACCOUNT_STATUS:
				this.currentStatus = (WSStatus)msg.obj;
				displayStatus();
		}
		return false;
	}
	
	private void requestStatus() {
		this.parentActivity.getServiceUtils().sendCommand(BackendService.MSG_REQUEST_ACCOUNT_STATUS);
	}
	
	private void displayStatus() {
		if (this.currentStatus == null)
			return;
		
		this.usdBalanceTextView.setText(
				formatBalance(this.currentStatus.getUsdBalance(),
								this.currentStatus.getExchangeRate()));
		
		List<String> pendingEvents = new ArrayList<String>();
		String btcIn = formatBTCIn(this.currentStatus.getBtcIn(),
									this.currentStatus.getExchangeRate());
		if (btcIn != null) pendingEvents.add(btcIn);
		pendingEvents.addAll(formatPendingTxs(this.currentStatus.getPendingTxs(),
												this.currentStatus.getExchangeRate()));
		
		if (pendingEvents.size() > 0) {
			StringBuilder sb = new StringBuilder();
			String separator = "";
			for (String s: pendingEvents) {
				sb.append(separator).append(s);
				separator = "\n";
			}
			this.pendingEventsTextView.setText(sb.toString());
			this.pendingEventsTextView.setVisibility(View.VISIBLE);
		} else {
			this.pendingEventsTextView.setVisibility(View.GONE);
		}
		
		displayStatusHook();
		
		hideProgressBar();
	}
	
	abstract protected void displayStatusHook();
	
	private String formatBalance(long usdBalance, long exchangeRate) {
		if (exchangeRate != 0) {
			return this.resources.getString(R.string.balance
					, calcAndFormatBTCEquivalent(usdBalance, exchangeRate)
					, formatUSD(usdBalance, Rounding.ROUND_DOWN));
		} else {
			return this.resources.getString(R.string.balance
					, "?"
					, formatUSD(usdBalance, Rounding.ROUND_DOWN));
		}
	}
	
	private String formatBTCIn(long btcIn, long exchangeRate) {
		if (btcIn == 0) return null;
		
		if (exchangeRate != 0) {
			return this.resources.getString(R.string.waiting_for_exchange_with_usd
					, formatBTC(btcIn)
					, calcAndFormatUSDEquivalent(btcIn, exchangeRate));
		} else {
			return this.resources.getString(R.string.waiting_for_exchange, formatBTC(btcIn));
		}
	}
	
	private List<String> formatPendingTxs(List<PendingTransaction> pendingTxs, long exchangeRate) {
		List<String> result = new ArrayList<String>();
		for (PendingTransaction pendingTx : pendingTxs) {
			String msg = "";
			if (exchangeRate != 0) {
				msg = this.resources.getString(R.string.waiting_for_confirmation_with_usd
						, formatBTC(pendingTx.getAmount()) 
						, calcAndFormatUSDEquivalent(pendingTx.getAmount(), exchangeRate));
			} else {
				msg = this.resources.getString(R.string.waiting_for_confirmation
						, formatBTC(pendingTx.getAmount()));
			}
			
			result.add(msg);
		}
		return result;
	}
	
	protected String calcAndFormatBTCEquivalent(long usd, long exchangeRate) {
		long btcEquivalent = Math.round(
								((double)usd / (double)exchangeRate)
								* BackendService.BTC_BASE_AMOUNT);
		return formatBTC(btcEquivalent);
	}
	
	protected String calcAndFormatUSDEquivalent(long btc, long exchangeRate) {
		long usdEquivalent = Math.round(	
								((double)btc / BackendService.BTC_BASE_AMOUNT)
								* (double)exchangeRate);
		return formatUSD(usdEquivalent, Rounding.ROUND_DOWN);
	}
	
	protected String formatUSD(long usd, Rounding rounding) {
		// use US locale for now, as we only support English at the moment
		double asDouble = (double)usd / BackendService.USD_BASE_AMOUNT;
		switch (rounding) {
			case ROUND_DOWN:
				double rounded = Math.floor(asDouble * 100.0) / 100.0;
				return String.format(Locale.US, "%.2f", rounded);
			case NO_ROUNDING:
				String asString = String.format(Locale.US, "%.5f", asDouble);
				
				// remove up to three trailing zeroes
				for (int i=0; i<3 && asString.endsWith("0"); i++) { 
					asString = asString.substring(0, asString.length() - 1);
				}
				
				return asString;
			default:
				throw new RuntimeException("Unhandled case");
		}
	}	
	
	protected String formatBTC(long btc) {
		// use US locale for now, as we only support English at the moment
		double asDouble = (long)btc / BackendService.BTC_BASE_AMOUNT;
		String s = String.format(Locale.US, "%.8f", asDouble);
		return s.replaceAll("[.,]?0+$", "");
	}
	
	protected String formatBTCForEditText(long btc) {
		// EditText in Android is still locale-unaware, so always use US locale here
		double asDouble = (long)btc / BackendService.BTC_BASE_AMOUNT;
		String s = String.format(Locale.US, "%.8f", asDouble);
		return s.replaceAll("[.,]?0+$", "");
	}
	
	private void showProgressBar() {
		this.contentLinearLayout.setVisibility(View.INVISIBLE);
		this.progressBar.setVisibility(View.VISIBLE);
	}
	
	private void hideProgressBar() {
		this.progressBar.setVisibility(View.INVISIBLE);
		this.contentLinearLayout.setVisibility(View.VISIBLE);
	}
	
	public void onDialogPositiveClick() { /* default is to do nothing */ }
	
	public void onDialogNegativeClick() { /* default is to do nothing */ }
}

package com.bridgewalkerapp.androidclient;

import java.util.ArrayList;
import java.util.List;

import android.os.Message;
import android.os.RemoteException;
import android.os.Handler.Callback;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.bridgewalkerapp.androidclient.apidata.WSStatus;
import com.bridgewalkerapp.androidclient.apidata.subcomponents.PendingTransaction;
import com.bridgewalkerapp.androidclient.data.ReplyAndRunnable;

abstract public class BalanceFragment extends SherlockFragment implements Callback {
	private static final String TAG = "com.bridgewalkerapp";
	
	protected ProgressBar progressBar = null;
	protected LinearLayout contentLinearLayout = null;
	protected TextView usdBalanceTextView = null;
	protected TextView pendingEventsTextView = null;
	
	protected BitcoinFragmentHost parentActivity = null;
		
	protected WSStatus currentStatus = null;
	
	@Override
	public void onStart() {
		super.onStart();
		this.parentActivity = (BitcoinFragmentHost)getActivity();
		this.parentActivity.registerFragment(this);
		
		if (this.currentStatus == null) {
			try {
				this.parentActivity.getServiceUtils().sendCommand(BackendService.MSG_REQUEST_ACCOUNT_STATUS);
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
	
	private void displayStatus() {
		if (this.currentStatus == null)
			return;
		
		this.usdBalanceTextView.setText(
				formatUSDBalance(this.currentStatus.getUsdBalance()));
		
		List<String> pendingEvents = new ArrayList<String>();
		String btcIn = formatBTCIn(this.currentStatus.getBtcIn());
		if (btcIn != null) pendingEvents.add(btcIn);
		pendingEvents.addAll(formatPendingTxs(this.currentStatus.getPendingTxs()));
		
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
	
	private String formatUSDBalance(long usdBalance) {
		double asDouble = (double)usdBalance / 100000.0;
		return String.format("Balance: %.5f USD", asDouble);
	}
	
	private String formatBTCIn(long btcIn) {
		if (btcIn == 0) return null;
		
		return String.format("+ %s BTC waiting to be exchanged.", formatBTC(btcIn));
	}
	
	private List<String> formatPendingTxs(List<PendingTransaction> pendingTxs) {
		List<String> result = new ArrayList<String>();
		for (PendingTransaction pendingTx : pendingTxs) {
			result.add(String.format("+ %s BTC waiting to be confirmed.",
						formatBTC(pendingTx.getAmount())));
		}
		return result;
	}
	
	private String formatBTC(long btc) {
		double asDouble = (long)btc / 100000000.0;
		String s = String.format("%.8f", asDouble);
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
}

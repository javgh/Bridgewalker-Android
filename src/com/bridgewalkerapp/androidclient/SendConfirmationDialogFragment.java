package com.bridgewalkerapp.androidclient;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class SendConfirmationDialogFragment extends SherlockDialogFragment {
	private static final String MESSAGE = "message";
	
	private SendConfirmationDialogListener listener = null;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String message = getArguments().getString(MESSAGE);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
		builder.setMessage(message)
				.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						listener.onDialogPositiveClick();
					}
				})
				.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						listener.onDialogNegativeClick();
					}
				});
		return builder.create();
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.listener = (SendConfirmationDialogListener)activity;
	}
	
	public interface SendConfirmationDialogListener {
		public void onDialogPositiveClick();
		public void onDialogNegativeClick();
	}
	
	public static SendConfirmationDialogFragment newInstance(String message) {
		SendConfirmationDialogFragment fragment = new SendConfirmationDialogFragment();
		Bundle args = new Bundle();
		args.putString(MESSAGE, message);
		fragment.setArguments(args);
		return fragment;
	}
}

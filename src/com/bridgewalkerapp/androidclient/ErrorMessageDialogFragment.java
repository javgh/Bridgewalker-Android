package com.bridgewalkerapp.androidclient;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class ErrorMessageDialogFragment extends SherlockDialogFragment {
	private static final String MESSAGE = "message";
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String message = getArguments().getString(MESSAGE);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getSherlockActivity());
		builder.setMessage(message)
					.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							/* do nothing */
						}
					});
		return builder.create();
	}

	public static ErrorMessageDialogFragment newInstance(String message) {
		ErrorMessageDialogFragment fragment = new ErrorMessageDialogFragment();
		Bundle args = new Bundle();
		args.putString(MESSAGE, message);
		fragment.setArguments(args);
		return fragment;		
	}
}

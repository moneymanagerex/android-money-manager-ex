package com.money.manager.ex.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.money.manager.ex.R;
import com.money.manager.ex.core.CurrencyUtils;

public class InputAmountDialog extends SherlockDialogFragment {
	private static final String KEY_ID_VIEW = "InputAmountDialog:Id";
	private static final String KEY_AMOUNT = "InputAmountDialog:Amount";
	private static final String COMMA_DECIMAL = ".";
	
	// arrays id keynum button
	private int[] idButtonKeyNum = { R.id.buttonKeyNum0, R.id.buttonKeyNum1, R.id.buttonKeyNum2, R.id.buttonKeyNum3,
			R.id.buttonKeyNum4, R.id.buttonKeyNum5, R.id.buttonKeyNum6, R.id.buttonKeyNum7, R.id.buttonKeyNum8,
			R.id.buttonKeyNum9, R.id.buttonKeyNumDecimal };
	
	private int mIdView;
	private String mAmount = "0";
	private Integer mCurrencyId;
	private TextView txtAmount;
	private ImageButton imgDelete;
	
	public interface InputAmountDialogListener {
		public void onFinishedInputAmountDialog(int id, Float amount);
	}
	
	public static InputAmountDialog getInstance(int id, Float amount) {
		return new InputAmountDialog(id, amount);
	}
	
	public static InputAmountDialog getInstance(int id, Float amount, Integer currencyId) {
		InputAmountDialog dialog = new InputAmountDialog(id, amount);
		dialog.mCurrencyId = currencyId;
		return dialog;
	}
	
	public InputAmountDialog() {
		// empty constructor
	}
	
	public InputAmountDialog(int id, Float amount) {
		super();
		mIdView = id;
		if (!(amount == null || amount == 0)) {
			int iAmount = (int) (amount * 100);
			if (Math.abs(amount - (iAmount / 100)) == 0) {
				mAmount = Integer.toString(iAmount / 100);
			} else {
				mAmount = Float.toString(amount);
			}
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (savedInstanceState != null) {
			if (savedInstanceState.containsKey(KEY_AMOUNT))
				mAmount = savedInstanceState.getString(KEY_AMOUNT);
			if (savedInstanceState.containsKey(KEY_ID_VIEW))
				mIdView = savedInstanceState.getInt(KEY_ID_VIEW);
		}
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		LayoutInflater inflater = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
		View view = inflater.inflate(R.layout.input_amount_dialog, null);
		// create listener
		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				String parseAmountTry = mAmount;
				parseAmountTry += ((Button)v).getText();
				try {
					Float.parseFloat(parseAmountTry);
				} catch (Exception e) {
					Log.e(InputAmountDialog.class.getSimpleName(), e.getMessage());
					return;
				}
				// change amount
				mAmount = parseAmountTry;
				refreshAmount();
			}
		};
		// reference button click listener
		for(int id : idButtonKeyNum) {
			Button button = (Button)view.findViewById(id);
			button.setOnClickListener(clickListener);
		}
		// image button delete
		imgDelete = (ImageButton)view.findViewById(R.id.imageButtonCancel);
		imgDelete.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!TextUtils.isEmpty(mAmount)) {
					if (mAmount.endsWith(COMMA_DECIMAL)) {
						mAmount = mAmount.substring(0, mAmount.length() - 2);
					} else {
						mAmount = mAmount.substring(0, mAmount.length() - 1);
					}
				}
				refreshAmount();
			}
		});
		// reference TextView amount
		txtAmount = (TextView)view.findViewById(R.id.textViewAmount);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setView(view);
		builder.setCancelable(false);
		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dismiss();
			}
		});
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				((InputAmountDialogListener) getActivity()).onFinishedInputAmountDialog(mIdView, Float.parseFloat(mAmount));
				dismiss();
			}
		});
		
		Dialog dialog = builder.create();
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		return dialog;
	}
	
	@Override
	public void onResume() {
		super.onResume();
		refreshAmount();
	}
	
	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putString(KEY_AMOUNT, mAmount);
		savedInstanceState.putInt(KEY_ID_VIEW, mIdView);
	}
	
	public void refreshAmount() {
		float fAmount = !TextUtils.isEmpty(mAmount) ? Float.parseFloat(mAmount) : 0;
		
		CurrencyUtils currencyUtils = new CurrencyUtils(getSherlockActivity());
		
		if (mCurrencyId == null) {
			txtAmount.setText(currencyUtils.getBaseCurrencyFormatted(fAmount));
		} else {
			txtAmount.setText(currencyUtils.getCurrencyFormatted(mCurrencyId, fAmount));
		}
	}
}

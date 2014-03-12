package com.money.manager.ex.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.money.manager.ex.CategorySubCategoryExpandableListActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.TableSplitTransactions;

public class SplitItemFragment extends Fragment {
	public interface SplitItemFragmentCallbacks {
		public void onRemoveItem(TableSplitTransactions object);
	}
	
	private static final int REQUEST_PICK_CATEGORY = 1;
	public static final String KEY_SPLIT_TRANSACTION = "SplitItemFragment:SplitTransaction";
	
	public static SplitItemFragment newIstance(TableSplitTransactions split) {
		SplitItemFragment fragment = new SplitItemFragment();
		fragment.mSplitObject = split;
		return fragment;
	}
	private TableSplitTransactions mSplitObject;
	
	private SplitItemFragmentCallbacks mOnSplitItemCallback;
	private Button btnSelectCategory;
	private EditText edtAmount;
	
	private Spinner spinTransCode;

	/**
	 * @return the splitItemCallback
	 */
	public SplitItemFragmentCallbacks getOnSplitItemCallback() {
		return mOnSplitItemCallback;
	}
	
	public TableSplitTransactions getTableSplitTransactions() {
		String selectItem = spinTransCode.getSelectedItem().toString();
		if (!TextUtils.isEmpty(edtAmount.getText())) {
			mSplitObject.setSplitTransAmount(Float.parseFloat(edtAmount.getText().toString()) * (selectItem.equals(getString(R.string.withdrawal)) ? 1 : -1));
		} else {
			mSplitObject.setSplitTransAmount(0);
		}
		return mSplitObject;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_PICK_CATEGORY:
			Button btnSelectCategory = (Button) getView().findViewById(R.id.buttonSelectCategory);
			btnSelectCategory.setText(R.string.select_category);
			if ((resultCode == Activity.RESULT_OK) && (data != null)) {
				mSplitObject.setCategId(data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGID, -1));
				mSplitObject.setSubCategId(data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGID, -1));
				btnSelectCategory.setText(new Core(getActivity()).getCategSubName(mSplitObject.getCategId(), mSplitObject.getSubCategId()));
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null)
			return null;
		
		if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SPLIT_TRANSACTION)) {
			mSplitObject = savedInstanceState.getParcelable(KEY_SPLIT_TRANSACTION);
		}
		
		final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.item_splittransaction, null);
		if (layout != null) {
			// amount
			edtAmount = (EditText) layout.findViewById(R.id.editTextTotAmount);
			if (!(mSplitObject.getSplitTransAmount() == 0)) {
				edtAmount.setText(Float.toString(Math.abs(mSplitObject.getSplitTransAmount())));
			}
			// type
			spinTransCode = (Spinner) layout.findViewById(R.id.spinnerTransCode);
			String[] transCodeItems = getResources().getStringArray(R.array.split_transcode_items);
			ArrayAdapter<String> adapterTrans = new ArrayAdapter<String>(getActivity(), R.layout.sherlock_spinner_item, transCodeItems);
			adapterTrans.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinTransCode.setAdapter(adapterTrans);
			spinTransCode.setSelection(mSplitObject.getSplitTransAmount() >= 0 ? 0 : 1, true);
			Core core = new Core(getActivity());
			// category and subcategory
			btnSelectCategory = (Button) layout.findViewById(R.id.buttonSelectCategory);
			String buttonText = core.getCategSubName(mSplitObject.getCategId(), mSplitObject.getSubCategId());
			btnSelectCategory.setText(TextUtils.isEmpty(buttonText) ? getString(R.string.select_category) : buttonText);
			
			btnSelectCategory.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(getActivity(), CategorySubCategoryExpandableListActivity.class);
					intent.setAction(Intent.ACTION_PICK);
					startActivityForResult(intent, REQUEST_PICK_CATEGORY);
				}
			});
			// image button to remove a item
			ImageButton btnRemove = (ImageButton) layout.findViewById(R.id.imageButtonCancel);
			btnRemove.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
					transaction.remove(SplitItemFragment.this);
					transaction.commit();
					if (getOnSplitItemCallback() != null) {
						getOnSplitItemCallback().onRemoveItem(mSplitObject);
					}
				}
			});
			// tag class
			layout.setTag(mSplitObject);
		}

		return layout;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putParcelable(KEY_SPLIT_TRANSACTION, mSplitObject);
	}

	/**
	 * @param splitItemCallback the splitItemCallback to set
	 */
	public void setOnSplitItemCallback(SplitItemFragmentCallbacks splitItemCallback) {
		this.mOnSplitItemCallback = splitItemCallback;
	}
}
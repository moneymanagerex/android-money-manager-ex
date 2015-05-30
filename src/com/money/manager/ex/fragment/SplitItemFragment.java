/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.CategorySubCategoryExpandableListActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.SplitTransactionsActivity;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.interfaces.ISplitTransactionsDataset;

public class SplitItemFragment
        extends Fragment
        implements IInputAmountDialogListener {

    public static final String KEY_SPLIT_TRANSACTION = "SplitItemFragment:SplitTransaction";
    private static final int REQUEST_PICK_CATEGORY = 1;
    private ISplitTransactionsDataset mSplitTransaction;
    private SplitItemFragmentCallbacks mOnSplitItemCallback;
    private TextView txtSelectCategory;
    private TextView txtAmount;
    private Spinner spinTransCode;

    public static SplitItemFragment newInstance(ISplitTransactionsDataset split) {
        SplitItemFragment fragment = new SplitItemFragment();
        fragment.mSplitTransaction = split;
        return fragment;
    }

    /**
     * @return the splitItemCallback
     */
    public SplitItemFragmentCallbacks getOnSplitItemCallback() {
        return mOnSplitItemCallback;
    }

    /**
     * @param splitItemCallback the splitItemCallback to set
     */
    public void setOnSplitItemCallback(SplitItemFragmentCallbacks splitItemCallback) {
        this.mOnSplitItemCallback = splitItemCallback;
    }

    /**
     * Returns the Split Transaction created. Called from the activity that holds multiple
     * split-fragments.
     * @param parentTransactionType Parent transaction type. Required to determine the amount sign.
     *                              If the parent is Deposit then Deposit here is +,
     *                              Withdrawal is -.
     * @return Split Transaction
     */
    public ISplitTransactionsDataset getSplitTransaction(TransactionTypes parentTransactionType) {
        Object amount = txtAmount.getTag();

        // handle 0 values.
        if(amount == null) {
            mSplitTransaction.setSplitTransAmount(0);
            return mSplitTransaction;
        }

        // otherwise figure out which sign to use for the amount.

        // toString takes the localized text! Use value.
        int position = spinTransCode.getSelectedItemPosition();
        TransactionTypes transactionType = TransactionTypes.values()[position];

        if(!parentTransactionType.equals(transactionType)){
            // parent transaction type is different. Invert the amount. What if the amount is already negative?
            mSplitTransaction.setSplitTransAmount((double) amount * -1);
        } else {
            mSplitTransaction.setSplitTransAmount((double) amount);
        }

        return mSplitTransaction;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_CATEGORY:
                TextView txtSelectCategory = (TextView) getView().findViewById(R.id.textViewCategory);
                if (txtSelectCategory != null) {
                    txtSelectCategory.setText(null);
                    if ((resultCode == Activity.RESULT_OK) && (data != null)) {
                        mSplitTransaction.setCategId(data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_CATEGID, -1));
                        mSplitTransaction.setSubCategId(data.getIntExtra(CategorySubCategoryExpandableListActivity.INTENT_RESULT_SUBCATEGID, -1));
                        txtSelectCategory.setText(new Core(getActivity().getApplicationContext()).getCategSubName(mSplitTransaction.getCategId(), mSplitTransaction.getSubCategId()));
                    }
                }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (container == null)
            return null;

        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_SPLIT_TRANSACTION)) {
            mSplitTransaction = savedInstanceState.getParcelable(KEY_SPLIT_TRANSACTION);
        }

        Core core = new Core(getActivity().getApplicationContext());

        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.item_splittransaction, null);
        if (layout != null) {
            // amount
            txtAmount = (TextView) layout.findViewById(R.id.editTextTotAmount);
            double splitTransactionAmount = mSplitTransaction.getSplitTransAmount();
            if (!(splitTransactionAmount == 0)) {
                // Change the sign to positive.
                if(splitTransactionAmount < 0) splitTransactionAmount = Math.abs(splitTransactionAmount);

                core.formatAmountTextView(txtAmount, splitTransactionAmount);
            }
            txtAmount.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Double amount = (Double) ((TextView) v).getTag();
                    if (amount == null)
                        amount = 0d;

                    if (getActivity() instanceof SplitTransactionsActivity) {
                        SplitTransactionsActivity activity = (SplitTransactionsActivity) getActivity();
                        activity.setFragmentInputAmountClick(SplitItemFragment.this);
                    }

                    InputAmountDialog dialog = InputAmountDialog.getInstance(SplitItemFragment.this,
                            v.getId(), amount);
                    dialog.show(getActivity().getSupportFragmentManager(), dialog.getClass().getSimpleName());
                }
            });

            // type
            spinTransCode = (Spinner) layout.findViewById(R.id.spinnerTransCode);
            String[] transCodeItems = getResources().getStringArray(R.array.split_transcode_items);
            ArrayAdapter<String> adapterTrans = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, transCodeItems);
            adapterTrans.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinTransCode.setAdapter(adapterTrans);
            // find the split transaction type.
//            SpinTransCode.setSelection(mSplitTransaction.getSplitTransAmount() >= 0 ? 0 : 1, true);
            int transactionTypeSelection = getTransactionTypeSelection();
            spinTransCode.setSelection(transactionTypeSelection);

            // category and subcategory
            txtSelectCategory = (TextView) layout.findViewById(R.id.textViewCategory);
            String buttonText = core.getCategSubName(mSplitTransaction.getCategId(), mSplitTransaction.getSubCategId());
            txtSelectCategory.setText(buttonText);

            txtSelectCategory.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), CategorySubCategoryExpandableListActivity.class);
                    intent.setAction(Intent.ACTION_PICK);
                    startActivityForResult(intent, REQUEST_PICK_CATEGORY);
                }
            });
            // image button to remove an item
            ImageButton btnRemove = (ImageButton) layout.findViewById(R.id.imageButtonCancel);
            btnRemove.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                    transaction.remove(SplitItemFragment.this);
                    transaction.commit();
                    if (getOnSplitItemCallback() != null) {
                        getOnSplitItemCallback().onRemoveItem(mSplitTransaction);
                    }
                }
            });
            // tag class
            layout.setTag(mSplitTransaction);
        }

        return layout;
    }

    private int getTransactionTypeSelection(){
        // define the transaction type based on the amount and the parent type.

        // 0 = withdrawal, 1 = deposit.
        int transactionTypeSelection;

        SplitTransactionsActivity splitActivity = (SplitTransactionsActivity) getActivity();
//        boolean parentIsWithdrawal = splitActivity.mParentTransactionType.equals(getString(R.string.withdrawal));
        boolean parentIsWithdrawal = splitActivity.mParentTransactionType.equals(TransactionTypes.Withdrawal);
        double amount = mSplitTransaction.getSplitTransAmount();
        if(parentIsWithdrawal){
            // parent is Withdrawal.
            transactionTypeSelection = amount >= 0
                    ? TransactionTypes.Withdrawal.getCode() // 0
                    : TransactionTypes.Deposit.getCode(); // 1;
        } else {
            // parent is Deposit.
            transactionTypeSelection = amount >= 0
                    ? TransactionTypes.Deposit.getCode() // 1
                    : TransactionTypes.Withdrawal.getCode(); // 0;
        }

        return transactionTypeSelection;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(KEY_SPLIT_TRANSACTION, mSplitTransaction);
    }

    @Override
    public void onFinishedInputAmountDialog(int id, Double amount) {
        Core core = new Core(getActivity().getApplicationContext());
        if (txtAmount.getId() == id) {
            txtAmount.setTag(amount);
            mSplitTransaction.setSplitTransAmount(amount);
            core.formatAmountTextView(txtAmount, amount);
        }
    }

    public interface SplitItemFragmentCallbacks {
        void onRemoveItem(ISplitTransactionsDataset object);
    }
}
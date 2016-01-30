/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.money.manager.ex.account;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.R;
import com.money.manager.ex.domainmodel.Account;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FilterDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FilterDialogFragment
    extends DialogFragment {

    private static final String ARG_TX_FILTER = "transactionFilter";
    private static final String ARG_ACCOUNT = "account";
    private static final String ARG_RECORDS = "numberOfRecords";

    private TransactionFilter mTransactionFilter;
    private Account mAccount;
    private int mRecords;

    public FilterDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FilterDialogFragment.
     */
    public static FilterDialogFragment newInstance(TransactionFilter filter, Account account,
                                                   int numberOfRecords) {
        FilterDialogFragment fragment = new FilterDialogFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_TX_FILTER, filter);
        args.putParcelable(ARG_ACCOUNT, account);
        args.putInt(ARG_RECORDS, numberOfRecords);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTransactionFilter = getArguments().getParcelable(ARG_TX_FILTER);
            mAccount = getArguments().getParcelable(ARG_ACCOUNT);
            mRecords = getArguments().getInt(ARG_RECORDS);
        }

        // todo: load additional details

        // styling
//        setStyle(DialogFragment.STYLE_NORMAL, getTheme());
    }

//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Inflate the layout for this fragment
//        View view = inflater.inflate(R.layout.fragment_filter_dialog, container, false);
//
////        getDialog().setTitle(getActivity().getString(R.string.account));
//
//        // todo: add data
//
//        return view;
//    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        Dialog dialog = super.onCreateDialog(savedInstanceState);
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
            .title(getActivity().getString(R.string.account))
            .customView(R.layout.fragment_filter_dialog, true)
            .positiveText(android.R.string.ok)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    // todo: notify parent, send the filter back?
                }
            })
            .negativeText(android.R.string.cancel)
            .build();

        displayInfo(dialog.getCustomView());

        return dialog;
    }

    private void displayInfo(View view) {
        // Number of records
        TextView numberOfTransactions = (TextView) view.findViewById(R.id.numberOfTransactionsTextView);
        numberOfTransactions.setText(getActivity().getString(R.string.number_transaction_found, mRecords));

        // time frame
//        Spinner timeFrame = (Spinner) view.findViewById(R.id.timeFrameSpinner);
//        timeFrame.setAdapter();
    }
}

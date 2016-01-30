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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    private TransactionFilter mTransactionFilter;
    private Account mAccount;

    public FilterDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FilterDialogFragment.
     */
    public static FilterDialogFragment newInstance(TransactionFilter filter, Account account) {
        FilterDialogFragment fragment = new FilterDialogFragment();

        Bundle args = new Bundle();
        args.putParcelable(ARG_TX_FILTER, filter);
        args.putParcelable(ARG_ACCOUNT, account);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTransactionFilter = getArguments().getParcelable(ARG_TX_FILTER);
            mAccount = getArguments().getParcelable(ARG_ACCOUNT);
        }

        // todo: load additional details

        // styling
        setStyle(DialogFragment.STYLE_NORMAL, getTheme());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_filter_dialog, container, false);

        getDialog().setTitle(getActivity().getString(R.string.account));


        // todo: add data

        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);

        return dialog;
    }
}

/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.view.IconicsButton;
import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.datalayer.AccountRepository;
import com.money.manager.ex.domainmodel.Account;

import org.parceler.Parcels;

/**
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
        args.putParcelable(ARG_TX_FILTER, Parcels.wrap(filter));
        args.putParcelable(ARG_ACCOUNT, Parcels.wrap(account));
        args.putInt(ARG_RECORDS, numberOfRecords);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mTransactionFilter = Parcels.unwrap(getArguments().getParcelable(ARG_TX_FILTER));
            mAccount = Parcels.unwrap(getArguments().getParcelable(ARG_ACCOUNT));
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
//        Dialog binaryDialog = super.onCreateDialog(savedInstanceState);
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

        initializeControls(dialog.getCustomView());
        displayInfo(dialog.getCustomView());

        return dialog;
    }

    private void displayInfo(View view) {
        // Favourite
        showFavouriteStatus(view);

        // Default Account
//        CheckBox defaultAccountCheckbox = (CheckBox) view.findViewById(R.id.defaultAccountCheckbox);
//        AppSettings preferences = new AppSettings(getActivity());
//        Integer defaultAccountId = preferences.getGeneralSettings().getDefaultAccountId();
//        defaultAccountCheckbox.setChecked(mAccount.getId().equals(defaultAccountId));

        // Number of records
        TextView numberOfTransactions = (TextView) view.findViewById(R.id.numberOfTransactionsTextView);
        numberOfTransactions.setText(getActivity().getString(R.string.number_transaction_found, mRecords));

        // time frame
//        Spinner timeFrame = (Spinner) view.findViewById(R.id.timeFrameSpinner);
//        timeFrame.setAdapter();
    }

    private void initializeControls(View view) {
        // Edit account.

        Button editButton = view.findViewById(R.id.editButton);
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), AccountEditActivity.class);
                intent.putExtra(AccountEditActivity.KEY_ACCOUNT_ID, mAccount.getId());
                intent.setAction(Intent.ACTION_EDIT);
                startActivity(intent);
            }
        });

        UIHelper ui = new UIHelper(getActivity());
        editButton.setCompoundDrawablesWithIntrinsicBounds(ui.getIcon(FontAwesome.Icon.faw_pencil), null, null, null);

        // Favourite account

        IconicsButton favouriteButton = view.findViewById(R.id.favouriteButton);
        favouriteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // un/set favourite flag
                boolean favourite = mAccount.getFavorite();
                mAccount.setFavorite(!favourite);
                // update
                AccountRepository repo = new AccountRepository(getActivity());
                boolean updated = repo.save(mAccount);
                if (!updated) {
                    new Core(getActivity()).alert(R.string.error_saving_record);
                }

                showFavouriteStatus(v);
            }
        });
    }

    private void showFavouriteStatus(View view) {
        if (view == null) return;

        IconicsButton favouriteButton = view.findViewById(R.id.favouriteButton);

        UIHelper ui = new UIHelper(getActivity());
        IconicsDrawable icon = mAccount.getFavorite()
                ? ui.getIcon(FontAwesome.Icon.faw_star)
                : ui.getIcon(FontAwesome.Icon.faw_star_o);
        favouriteButton.setCompoundDrawablesWithIntrinsicBounds(icon, null, null, null);
    }
}

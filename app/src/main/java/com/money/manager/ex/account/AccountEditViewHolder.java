package com.money.manager.ex.account;

import android.app.Activity;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.view.RobotoTextView;
import com.shamanland.fonticon.FontIconView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * ViewHolder pattern for the account edit screen.
 */
public class AccountEditViewHolder {
    public AccountEditViewHolder(Activity parent) {
        ButterKnife.bind(this, parent);
    }

    @BindView(R.id.editTextAccountHeldAt) EditText edtAccountHeldAt;

    @BindView(R.id.spinnerAccountType) Spinner accountTypeSpinner;
    @BindView(R.id.defaultAccountCheckbox) CheckBox defaultAccountCheckbox;
    @BindView(R.id.defaultAccountText) RobotoTextView defaultAccountText;
    @BindView(R.id.imageViewAccountFav) FontIconView imageViewAccountFav;
    @BindView(R.id.favouriteAccountTextView) RobotoTextView favouriteAccountTextView;
    @BindView(R.id.editTextWebsite) EditText webSiteEditText;

    @BindView(R.id.editTextAccountName) EditText edtAccountName;
    @BindView(R.id.editTextAccountNumber) EditText edtAccountNumber;
    @BindView(R.id.editTextContact) EditText edtContact;
    @BindView(R.id.editTextAccessInfo) EditText edtAccessInfo;
    @BindView(R.id.spinnerAccountStatus) Spinner spinAccountStatus;
    @BindView(R.id.editTextNotes) EditText edtNotes;
    @BindView(R.id.spinnerSymbolInitialBalance) Spinner spinSymbolInitialBalance;
    @BindView(R.id.textViewSelectCurrency) TextView txtSelectCurrency;
    @BindView(R.id.editTextInitialBalance) TextView txtInitialBalance;

}

package com.money.manager.ex.account;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.money.manager.ex.view.RobotoTextView;
import com.shamanland.fonticon.FontIconView;

/**
 * ViewHolder pattern for the account edit screen.
 */
public class AccountEditViewHolder {
    public EditText edtAccountHeldAt;

    public Spinner accountTypeSpinner;
    public CheckBox defaultAccountCheckbox;
    public RobotoTextView defaultAccountText;
    public FontIconView imageViewAccountFav;
    public RobotoTextView favouriteAccountTextView;
    public EditText webSiteEditText;

    public EditText edtAccountName;
    public EditText edtAccountNumber;
    public EditText edtContact;
    public EditText edtAccessInfo;
    public EditText edtNotes;
    public Spinner spinSymbolInitialBalance;
    public TextView txtSelectCurrency, txtInitialBalance;

}

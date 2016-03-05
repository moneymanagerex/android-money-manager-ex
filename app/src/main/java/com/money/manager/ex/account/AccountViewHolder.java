package com.money.manager.ex.account;

import android.widget.CheckBox;
import android.widget.Spinner;

import com.money.manager.ex.view.RobotoTextView;
import com.shamanland.fonticon.FontIconView;

/**
 * ViewHolder pattern for the account edit screen.
 */
public class AccountViewHolder {
    public Spinner accountTypeSpinner;
    public CheckBox defaultAccountCheckbox;
    public RobotoTextView defaultAccountText;
    public FontIconView imageViewAccountFav;
    public RobotoTextView favouriteAccountTextView;
}

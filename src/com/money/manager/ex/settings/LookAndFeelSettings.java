package com.money.manager.ex.settings;

import android.content.Context;
import android.text.TextUtils;

import com.money.manager.ex.R;

/**
 * Look & Feel preferences
 * Created by Alen Siljak on 03/07/2015.
 */
public class LookAndFeelSettings
    extends SettingsBase {

    public LookAndFeelSettings(Context context) {
        super(context);

    }

    public boolean getHideReconciledAmounts() {
        String key = getSettingsKey(R.string.pref_transaction_hide_reconciled_amounts);
        return getBooleanSetting(key, false);
    }

    public String getShowTransactions() {
        return get(R.string.pref_show_transaction, "");
    }

    public boolean setShowTransactions(String value) {
        String key = getSettingsKey(R.string.pref_show_transaction);
        return set(key, value);
    }

    public boolean setShowTransactions(int resourceId) {
        String value = mContext.getString(resourceId);
        if (TextUtils.isEmpty(value)) return false;

        String key = getSettingsKey(R.string.pref_show_transaction);
        return set(key, value);
    }

}

package com.money.manager.ex.settings;

import android.content.Context;
import android.text.TextUtils;

import com.money.manager.ex.R;
import com.money.manager.ex.businessobjects.InfoService;
import com.money.manager.ex.core.DefinedDateRange;
import com.money.manager.ex.core.DefinedDateRangeName;
import com.money.manager.ex.core.DefinedDateRanges;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.NumericHelper;

import org.apache.commons.lang3.StringUtils;

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

    public DefinedDateRangeName getShowTransactions() {
        DefinedDateRangeName defaultValue = DefinedDateRangeName.LAST_7_DAYS;

        String value = get(R.string.pref_show_transaction, defaultValue.toString());

        DefinedDateRangeName result = null;

        // try directly first
        try {
            result = DefinedDateRangeName.valueOf(value);
        } catch (IllegalArgumentException e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "parsing default date range");
        }
        if (result != null) {
            return result;
        }

        // then try by the previous setting, localized range name
        DefinedDateRanges ranges = new DefinedDateRanges(mContext);
        DefinedDateRange range = ranges.getByLocalizedName(value);
        if (range != null) {
            return range.key;
        }

        // if still not found, initialize to a default value.
        setShowTransactions(defaultValue);
        return defaultValue;
    }

//    public boolean setShowTransactions(String value) {
//        String key = getSettingsKey(R.string.pref_show_transaction);
//        return set(key, value);
//    }

    public boolean setShowTransactions(DefinedDateRangeName value) {
//        String value = context.getString(resourceId);
//        if (TextUtils.isEmpty(value)) return false;

        String key = getSettingsKey(R.string.pref_show_transaction);
        return set(key, value.toString());
    }

    public boolean getViewOpenAccounts() {
//        return get(R.string.pref_account_open_visible, true);
        InfoService infoService = new InfoService(mContext);
        String value = infoService.getInfoValue(InfoService.SHOW_OPEN_ACCOUNTS);
        return Boolean.valueOf(value);
    }

    public void setViewOpenAccounts(Boolean value) {
        InfoService infoService = new InfoService(mContext);
        infoService.setInfoValue(InfoService.SHOW_OPEN_ACCOUNTS, value.toString());
    }

    public boolean getViewFavouriteAccounts() {
//        return get(R.string.pref_account_fav_visible, true);
        InfoService infoService = new InfoService(mContext);
        String value = infoService.getInfoValue(InfoService.SHOW_FAVOURITE_ACCOUNTS);
        return Boolean.valueOf(value);
    }

    public void setViewFavouriteAccounts(Boolean value) {
        InfoService infoService = new InfoService(mContext);
        infoService.setInfoValue(InfoService.SHOW_FAVOURITE_ACCOUNTS, value.toString());
    }

}

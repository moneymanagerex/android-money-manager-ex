package com.money.manager.ex.core;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.DonateActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.preferences.PreferencesConstant;

/**
 * Created by Alessandro Lazzari on 08/09/2014.
 */
public class DonateDialogUtils {
    /**
     * Show donate dialog
     *
     * @param context   Application
     * @param forceShow if boolean indicating whether you want to force the showing
     * @return true if shown
     */
    public static boolean showDonateDialog(final Context context, boolean forceShow) {
        int currentVersionCode = Core.getCurrentVersionCode(context);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int lastVersionCode = preferences.getInt(PreferencesConstant.PREF_DONATE_LAST_VERSION_KEY, -1);
        if (!(lastVersionCode == currentVersionCode) || forceShow) {
            preferences.edit().putInt(PreferencesConstant.PREF_DONATE_LAST_VERSION_KEY, currentVersionCode).commit();
            Core core = new Core(context);
            if (TextUtils.isEmpty(core.getInfoValue(Constants.INFOTABLE_SKU_ORDER_ID))) {
                //get text donate
                String donateText = context.getString(R.string.donate_header);
                //create dialog
                AlertDialog.Builder showDialog = new AlertDialog.Builder(context);
                showDialog.setCancelable(false);
                showDialog.setTitle(R.string.donate);
                showDialog.setIcon(R.drawable.ic_launcher);
                showDialog.setMessage(Html.fromHtml(donateText));
                showDialog.setNegativeButton(R.string.no_thanks, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                showDialog.setPositiveButton(R.string.donate_exlamation, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        context.startActivity(new Intent(context, DonateActivity.class));
                        dialog.dismiss();
                    }
                });
                // show dialog
                showDialog.create().show();
            }
            return true;
        } else
            return false;
    }
}

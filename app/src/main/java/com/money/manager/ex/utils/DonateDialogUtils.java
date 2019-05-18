/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.DonateActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.settings.PreferenceConstants;

import androidx.annotation.NonNull;

/**
 * Created by Alessandro Lazzari on 08/09/2014.
 */
public class DonateDialogUtils {
    /**
     * Show donate binaryDialog
     *
     * @param context   Application
     * @param forceShow if boolean indicating whether you want to force the showing
     * @return true if shown
     */
    public static boolean showDonateDialog(final Context context, boolean forceShow) {
        int currentVersionCode = new Core(context).getAppVersionCode();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int lastVersionCode = preferences.getInt(context.getString(PreferenceConstants.PREF_DONATE_LAST_VERSION_KEY), -1);
        if (!(lastVersionCode == currentVersionCode) || forceShow) {
            preferences.edit().putInt(context.getString(PreferenceConstants.PREF_DONATE_LAST_VERSION_KEY), currentVersionCode).commit();

            InfoService infoService = new InfoService(context);

            if (TextUtils.isEmpty(infoService.getInfoValue(InfoKeys.SKU_ORDER_ID))) {
                //get text donate
                String donateText = context.getString(R.string.donate_header);
                //create binaryDialog
                new MaterialDialog.Builder(context)
                    .cancelable(false)
                    .title(R.string.donate)
                    .iconRes(R.mipmap.ic_launcher)
                    .content(UIHelper.fromHtml(donateText))
                    .negativeText(R.string.no_thanks)
                    .onNegative(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            dialog.dismiss();
                        }
                    })
                        .positiveText(R.string.donate_exlamation)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                context.startActivity(new Intent(context, DonateActivity.class));
                                dialog.dismiss();
                            }
                        })
                .build().show();
            }
            return true;
        } else
            return false;
    }

    /**
     * Reset to force show donate binaryDialog
     *
     * @param context application
     */
    public static void resetDonateDialog(final Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        preferences.edit().putInt(context.getString(PreferenceConstants.PREF_DONATE_LAST_VERSION_KEY), -1).commit();
    }
}

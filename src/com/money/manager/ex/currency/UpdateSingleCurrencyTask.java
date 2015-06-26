/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.currency;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.utils.ActivityUtils;
import com.money.manager.ex.utils.CurrencyUtils;
import com.money.manager.ex.utils.DialogUtils;

/**
 * Background task that updates the exchange rate for a single currency.
 */
public class UpdateSingleCurrencyTask
        extends AsyncTask<Void, Integer, Boolean> {

    public UpdateSingleCurrencyTask(Context context, CurrencyUtils currencyUtils, int currencyId) {
        mContext = context;
        mCurrencyUtils = currencyUtils;
        mCurrencyId = currencyId;
    }

    private Context mContext;
    private CurrencyUtils mCurrencyUtils;
    private int mCurrencyId;

    private ProgressDialog dialog = null;
    private int mPrevOrientation;

    @Override
    protected void onPreExecute() {
            super.onPreExecute();

            if (mContext instanceof Activity) {
                Activity parent = (Activity) mContext;
                mPrevOrientation = ActivityUtils.forceCurrentOrientation(parent);
            }

            DropboxHelper.setAutoUploadDisabled(true);

            dialog = new ProgressDialog(mContext);
            // setting dialog
            // update_menu_currency_exchange_rates
            dialog.setMessage(mContext.getString(R.string.start_currency_exchange_rates));
            dialog.setIndeterminate(true);
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(false);
            // show dialog
            dialog.show();
            }

    @Override
    protected Boolean doInBackground(Void... params) {
        mCurrencyUtils.updateCurrencyRateFromBase(mCurrencyId);
        return Boolean.TRUE;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        try {
            if (dialog != null) {
                DialogUtils.closeProgressDialog(dialog);
            }
        } catch (Exception e) {
            Log.e(CurrencyFormatsListActivity.LOGCAT, e.getMessage());
        }
        if (result) {
            Toast.makeText(mContext, R.string.success_currency_exchange_rates, Toast.LENGTH_LONG).show();
        }

        DropboxHelper.setAutoUploadDisabled(false);
        DropboxHelper.notifyDataChanged();

        if (mContext instanceof Activity) {
            Activity parent = (Activity) mContext;
            ActivityUtils.restoreOrientation(parent, mPrevOrientation);
        }

        super.onPostExecute(result);
    }
}
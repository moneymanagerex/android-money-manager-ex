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

package com.money.manager.ex.investment.prices;

import android.app.ProgressDialog;
import android.content.Context;

import com.money.manager.ex.R;

import timber.log.Timber;

/**
 * Base class for price updaters. Contains some common and useful code.
 * Inherited by exchange rate and security price updaters.
 */
public class PriceUpdaterBase {
    public PriceUpdaterBase(Context context) {
        mContext = context;
    }

    private Context mContext;
    private ProgressDialog mDialog = null;

    public Context getContext() {
        return mContext;
    }

    protected void showProgressDialog(Integer max) {
        mDialog = new ProgressDialog(getContext());

        mDialog.setMessage(getContext().getString(R.string.starting_price_update));
        mDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        if (max != null) {
            mDialog.setMax(max);
        }
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    protected void setProgress(int progress) {
        mDialog.setProgress(progress);
    }

    protected void closeProgressDialog() {
        try {
            if (mDialog != null) {
                mDialog.dismiss();
//                DialogUtils.closeProgressDialog(mDialog);
            }
        } catch (Exception e) {
            Timber.e(e, "closing binaryDialog");
        }
    }
}

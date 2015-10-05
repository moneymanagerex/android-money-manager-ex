/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.currency;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.utils.DialogUtils;

/**
 */
public class ImportAllCurrenciesTask
    extends AsyncTask<Void, Void, Boolean> {

    public ImportAllCurrenciesTask(Context context) {
        mContext = context;
    }

    private Context mContext;

    ProgressDialog dialog = null;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog = ProgressDialog.show(mContext, null, mContext.getString(R.string.import_currencies_in_progress));
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        try {
            CurrencyService service = new CurrencyService(mContext);
            return service.importCurrenciesFromLocaleAvaible();
        } catch (Exception e) {
            throw new RuntimeException("Error in import all currencies", e);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        try {
            if (dialog != null) {
                DialogUtils.closeProgressDialog(dialog);
            }
        } catch (Exception e) {
            ExceptionHandler handler = new ExceptionHandler(mContext, this);
            handler.handle(e, "closing progress dialog");
        }
        super.onPostExecute(result);
    }
}

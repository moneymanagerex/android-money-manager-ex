package com.money.manager.ex.currency;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.TableCurrencyFormats;
import com.money.manager.ex.dropbox.DropboxHelper;
import com.money.manager.ex.utils.ActivityUtils;
import com.money.manager.ex.utils.CurrencyUtils;
import com.money.manager.ex.utils.DialogUtils;

import java.util.List;

/**
 * Update all currencies.
 * Asynchronous task.
 */
public class UpdateCurrenciesTask
    extends AsyncTask<Void, Integer, Boolean> {

    public UpdateCurrenciesTask(Context context, CurrencyUtils currencyUtils) {
        mContext = context;
        mCurrencyUtils = currencyUtils;
    }

    private Context mContext;
    private CurrencyUtils mCurrencyUtils;

    private ProgressDialog dialog = null;
    private int mCountCurrencies = 0;
    private TableCurrencyFormats mCurrencyFormat;

    private Core mCore;

    private int mPrevOrientation;

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        if (mContext instanceof Activity) {
            Activity parent = (Activity) mContext;
            mPrevOrientation = ActivityUtils.forceCurrentOrientation(parent);
        }

        mCore = new Core(mContext.getApplicationContext());
        DropboxHelper.setAutoUploadDisabled(true);

        //getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);

        dialog = new ProgressDialog(mContext);
        dialog.setMessage(mContext.getString(R.string.start_currency_exchange_rates));
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        CurrencyUtils currencyUtils = mCurrencyUtils;
        List<TableCurrencyFormats> currencyFormats = currencyUtils.getAllCurrencyFormats();
        mCountCurrencies = currencyFormats.size();
        for (int i = 0; i < currencyFormats.size(); i++) {
            mCurrencyFormat = currencyFormats.get(i);
            currencyUtils.updateCurrencyRateFromBase(mCurrencyFormat.getCurrencyId());
            publishProgress(i);
        }
        return Boolean.TRUE;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if (dialog != null) {
            dialog.setMax(mCountCurrencies);
            dialog.setProgress(values[0]);
            if (mCurrencyFormat != null) {
                dialog.setMessage(mCore.highlight(mCurrencyFormat.getCurrencyName(),
                        mContext.getString(R.string.update_currency_exchange_rates, mCurrencyFormat.getCurrencyName())));
            }
        }
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
        if (result)
            Toast.makeText(mContext, R.string.success_currency_exchange_rates, Toast.LENGTH_LONG).show();

        DropboxHelper.setAutoUploadDisabled(false);
        DropboxHelper.notifyDataChanged();

        if (mContext instanceof Activity) {
            Activity parent = (Activity) mContext;
            ActivityUtils.restoreOrientation(parent, mPrevOrientation);
        }

        super.onPostExecute(result);
    }
}

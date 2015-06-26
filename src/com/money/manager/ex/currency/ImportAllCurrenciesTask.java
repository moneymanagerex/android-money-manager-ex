package com.money.manager.ex.currency;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.utils.DialogUtils;

/**
 *
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
        Core core = new Core(mContext.getApplicationContext());
        return core.importCurrenciesFromLocaleAvaible();
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
        super.onPostExecute(result);
    }
}

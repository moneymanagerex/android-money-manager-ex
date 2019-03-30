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

package com.money.manager.ex.currency;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.money.manager.ex.R;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.events.CurrencyDeletionConfirmedEvent;
import com.money.manager.ex.currency.events.ExchangeRateUpdateConfirmedEvent;
import com.money.manager.ex.utils.DialogUtils;

import org.greenrobot.eventbus.EventBus;

import java.util.Date;
import java.util.concurrent.Callable;

import androidx.annotation.NonNull;
import info.javaperformance.money.Money;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * Currency UI-related code, shared across Currency fragments.
 */
public class CurrencyUIFeatures {

    public CurrencyUIFeatures(Context context) {
        this.context = context;
    }

    private Context context;
    private CurrencyService currencyService;

    public Context getContext() {
        return context;
    }

    public CurrencyService getService() {
        if (currencyService == null) {
            currencyService = new CurrencyService(getContext());
        }
        return currencyService;
    }

    public void notifyCurrencyCanNotBeDeleted() {
        new MaterialDialog.Builder(getContext())
                .title(R.string.attention)
                .icon(new UIHelper(getContext()).getIcon(GoogleMaterial.Icon.gmd_warning))
                .content(R.string.currency_can_not_deleted)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                })
                .build().show();
    }

    /**
     *
     * @return Indicator whether the rate was successfully updated.
     */
    public boolean onPriceDownloaded(String symbol, Money price, Date date) {
        // extract destination currency
        String baseCurrencyCode = getService().getBaseCurrencyCode();
        String destinationCurrency = symbol.replace(baseCurrencyCode, "");
        destinationCurrency = destinationCurrency.replace("=X", "");
        boolean success = false;

        try {
            // update exchange rate.
            success = getService().saveExchangeRate(destinationCurrency, price);
        } catch (Exception ex) {
            Timber.e(ex, "saving exchange rate");
        }

        if (!success) {
            String message = getContext().getString(R.string.error_update_currency_exchange_rate);
            message += " " + destinationCurrency;

            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
        return success;
    }

    public void showDialogDeleteCurrency(final int currencyId, final int itemPosition) {
        UIHelper ui = new UIHelper(getContext());

        new MaterialDialog.Builder(getContext())
                .title(R.string.delete_currency)
                .icon(ui.getIcon(FontAwesome.Icon.faw_question_circle))
                .content(R.string.confirmDelete)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        EventBus.getDefault().post(new CurrencyDeletionConfirmedEvent(currencyId, itemPosition));
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
        .build().show();
    }

    public void showDialogImportAllCurrencies() {
        UIHelper ui = new UIHelper(getContext());

        new MaterialDialog.Builder(getContext())
            .title(R.string.attention)
            .icon(ui.getIcon(FontAwesome.Icon.faw_question_circle))
            .content(R.string.question_import_currencies)
                .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        importCurrencies();
                    }
                })
                .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
            .build().show();
    }

    public void showDialogUpdateExchangeRates() {
        UIHelper ui = new UIHelper(getContext());

        new MaterialDialog.Builder(getContext())
            .title(R.string.download)
            .icon(ui.getIcon(FontAwesome.Icon.faw_question_circle))
            .content(R.string.question_update_currency_exchange_rates)
            .positiveText(android.R.string.ok)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        EventBus.getDefault().post(new ExchangeRateUpdateConfirmedEvent(true));
                    }
                })
            .negativeText(android.R.string.cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.cancel();
                    }
                })
            .build().show();
    }

    /**
     * Displays a prompt binaryDialog for choosing whether to update all or active-only currencies.
     */
    public void showActiveInactiveSelectorForUpdate() {
        // offer active and all currencies
        String[] options = new String[]{
            getContext().getString(R.string.active_currencies),
            getContext().getString(R.string.all_currencies
        )};

        new MaterialDialog.Builder(getContext())
                .title(R.string.update_menu_currency_exchange_rates)
                .items(options)
                .itemsCallbackSingleChoice(-1, new MaterialDialog.ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View view, int which, CharSequence text) {
                        // send selection (all/active)
                        EventBus.getDefault().post(new ExchangeRateUpdateConfirmedEvent(which == 1));
                        return true;
                    }
                })
                .positiveText(android.R.string.ok)
                .show();
    }

    public void startCurrencyEditActivity(Integer currencyId) {
        // create intent, set Account ID
        Intent intent = new Intent(getContext(), CurrencyEditActivity.class);
        // check transId not null
        if (currencyId != null) {
            intent.putExtra(CurrencyEditActivity.KEY_CURRENCY_ID, currencyId);
            intent.setAction(Intent.ACTION_EDIT);
        } else {
            intent.setAction(Intent.ACTION_INSERT);
        }
        // launch activity
        getContext().startActivity(intent);
    }

    private void importCurrencies() {
//        getService().importAllCurrencies();

        final ProgressDialog progress = ProgressDialog.show(getContext(), null,
                getContext().getString(R.string.import_currencies_in_progress));

        Observable.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return getService().importCurrenciesFromSystemLocales();
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {
                        DialogUtils.closeProgressDialog(progress);
                        // todo getContext().getContentResolver().notifyChange();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "importing currencies from the system");
                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        // should we import one by one?
                    }
                });
    }
}

/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Toast;

import com.afollestad.materialdialogs.AlertDialogWrapper;
import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.R;
import com.money.manager.ex.currency.events.CurrencyDeletionConfirmedEvent;
import com.money.manager.ex.currency.events.ExchangeRateUpdateConfirmedEvent;
import com.shamanland.fonticon.FontIconDrawable;

import org.greenrobot.eventbus.EventBus;

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
        new AlertDialogWrapper.Builder(getContext())
                .setTitle(R.string.attention)
                .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_alert))
                .setMessage(R.string.currency_can_not_deleted)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    public void showDialogDeleteCurrency(final int currencyId, final int itemPosition) {
        // config alert dialog
        AlertDialogWrapper.Builder alertDialog = new AlertDialogWrapper.Builder(getContext())
                .setTitle(R.string.delete_currency)
                .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
                .setMessage(R.string.confirmDelete);
        // set listener on positive button
        alertDialog.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventBus.getDefault().post(new CurrencyDeletionConfirmedEvent(currencyId, itemPosition));
                    }
                });
        // set listener on negative button
        alertDialog.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        // create dialog and show
        alertDialog.create().show();
    }

    public void showDialogImportAllCurrencies() {
        new AlertDialogWrapper.Builder(getContext())
            .setTitle(R.string.attention)
            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
            .setMessage(R.string.question_import_currencies)
            .setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        getService().importAllCurrencies();
                    }
                })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .create().show();
    }

    public void showDialogUpdateExchangeRates() {
        // config alert dialog
        new AlertDialogWrapper.Builder(getContext())
            .setTitle(R.string.download)
            .setIcon(FontIconDrawable.inflate(getContext(), R.xml.ic_question))
            .setMessage(R.string.question_update_currency_exchange_rates)
            .setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //updateExchangeRates();
                        EventBus.getDefault().post(new ExchangeRateUpdateConfirmedEvent(true));
                    }
                })
            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            })
            .create().show();
    }

    /**
     * Displays a prompt dialog for choosing whether to update all or active-only currencies.
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
}

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

package com.money.manager.ex.core;

import android.content.Context;
import android.content.Intent;
import android.os.Messenger;
import android.support.annotation.NonNull;

import com.money.manager.ex.common.CalculatorActivity;
import com.money.manager.ex.home.MainActivity;
import com.money.manager.ex.investment.PriceEditActivity;
import com.money.manager.ex.search.SearchActivity;
import com.money.manager.ex.search.SearchParameters;
import com.money.manager.ex.sync.SyncConstants;
import com.money.manager.ex.sync.SyncService;

import org.parceler.Parcels;

import info.javaperformance.money.Money;

/**
 * Generates Intents for common app functionality.
 */

public class IntentFactory {
    public static Intent getSyncServiceIntent(Context context, String action, String localFile,
                                              String remoteFile, Messenger messenger) {
        Intent syncServiceIntent = new Intent(context, SyncService.class);

        syncServiceIntent.setAction(action);

        syncServiceIntent.putExtra(SyncConstants.INTENT_EXTRA_LOCAL_FILE, localFile);
        syncServiceIntent.putExtra(SyncConstants.INTENT_EXTRA_REMOTE_FILE, remoteFile);

        if (messenger != null) {
            syncServiceIntent.putExtra(SyncService.INTENT_EXTRA_MESSENGER, messenger);
        }

        return syncServiceIntent;
    }

    public static Intent getIntentForSearch(Context context, SearchParameters parameters) {
        Intent intent = new Intent(context, SearchActivity.class);
        intent.putExtra(SearchActivity.EXTRA_SEARCH_PARAMETERS, Parcels.wrap(parameters));
        intent.setAction(Intent.ACTION_INSERT);
        //getContext().startActivity(intent);
        return intent;
    }

    /**
     * Creates the intent that will start the Main Activity, resetting the activity stack.
     * This will prevent going back to any previous activity.
     * @return intent
     */
    public static Intent getMainActivityNew(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

        // Clear the activity stack completely.
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);

        return intent;
    }

    public static Intent getPriceEditIntent(Context context) {
        Intent intent = new Intent(context, PriceEditActivity.class);

        return intent;
    }

    public static Intent getNumericInputIntent(@NonNull Context context, @NonNull Money amount) {
        Intent intent = new Intent(context, CalculatorActivity.class);
        // amount
        intent.putExtra(CalculatorActivity.EXTRA_AMOUNT, amount.toString());

        return intent;
    }

    public static Intent getNumericInputIntent(Context context, Money amount, int currencyId) {
        Intent intent = new Intent(context, CalculatorActivity.class);

        // currency
        intent.putExtra(CalculatorActivity.EXTRA_CURRENCY_ID, currencyId);
        // amount
        intent.putExtra(CalculatorActivity.EXTRA_AMOUNT, amount.toString());

        return intent;
    }

    public static Intent getNumericInputIntent(Context context, Money amount, int currencyId,
                                                  boolean roundToCurrency) {
        Intent intent = new Intent(context, CalculatorActivity.class);

        // currency
        intent.putExtra(CalculatorActivity.EXTRA_CURRENCY_ID, currencyId);
        // amount
        intent.putExtra(CalculatorActivity.EXTRA_AMOUNT, amount.toString());
        intent.putExtra(CalculatorActivity.EXTRA_ROUND_TO_CURRENCY, roundToCurrency);

        return intent;
    }

}

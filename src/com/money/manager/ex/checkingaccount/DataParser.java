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
package com.money.manager.ex.checkingaccount;

import android.content.Context;
import android.net.Uri;

import com.money.manager.ex.businessobjects.Category;
import com.money.manager.ex.businessobjects.Payee;
import com.money.manager.ex.database.AccountRepository;

/**
 * Used to get parameters from intent.getData
 * Tasker integration via intent.
 */
public class DataParser {
    public DataParser(Context context) {
        mContext = context;
    }

    // Keys for extra parameters in the Intent.
    public static final String PARAM_ACCOUNT = "account";
    public static final String PARAM_AMOUNT = "amount";
    public static final String PARAM_PAYEE = "payee";
    public static final String PARAM_CATEGORY = "category";

    private Context mContext;

    public IntentDataParameters parseData(Uri data) {
        IntentDataParameters parameters = new IntentDataParameters();

        // account
        String accountName = data.getQueryParameter(PARAM_ACCOUNT);
        AccountRepository account = new AccountRepository(mContext);
        int accountId = account.loadIdByName(accountName);
        parameters.accountId = accountId;

        parameters.payeeName = data.getQueryParameter(PARAM_PAYEE);
        Payee payee = new Payee(mContext);
        int payeeId = payee.loadIdByName(parameters.payeeName);
        parameters.payeeId = payeeId;

        String amount = data.getQueryParameter(PARAM_AMOUNT);
        parameters.amount = Double.parseDouble(amount);

        parameters.categoryName = data.getQueryParameter(PARAM_CATEGORY);
        Category category = new Category(mContext);
        int categoryId = category.loadIdByName(parameters.categoryName);
        parameters.categoryId = categoryId;

        return parameters;
    }
}

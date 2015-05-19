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
package com.money.manager.ex.businessobjects.qif;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.money.manager.ex.Constants;
import com.money.manager.ex.core.AccountTypes;
import com.money.manager.ex.database.MoneyManagerOpenHelper;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.database.TableAccountList;

import java.util.HashMap;

/**
 * Represents qif header record.
 */
public class QifHeader {
    public QifHeader(Context context) {
        mContext = context;
    }

    private Context mContext;

    public String parse(Cursor cursor) {
        StringBuilder builder = new StringBuilder();
        TableAccountList account = loadAccount(cursor);

        /* header from mmex desktop:
!Account
NCash (EUR)
TBank
D[EUR]
$57.300000
^
!Type:Cash
         */

        // header depends on the account type.

        builder.append("!Account");
        builder.append(System.lineSeparator());

        // name
        builder.append("N");
        String name = account.getAccountName();
        builder.append(name);
        builder.append(System.lineSeparator());

        // description
        String description = account.getNotes();
        if (!TextUtils.isEmpty(description)) {
            builder.append("D");
            builder.append(description);
            builder.append(System.lineSeparator());
        }

        // account type
        String accountType = getAccountType(account);
        builder.append("T");
        builder.append(accountType);
        builder.append(System.lineSeparator());

        // Limit, for credit cards only.
//        if (accountType.equals(Constants.ACCOUNT_TYPE_CREDIT_CARD)) {
//            builder.append("L");
//            builder.append();
//        builder.append(System.lineSeparator());
//        }

        // Header separator.
        builder.append("^");
        builder.append(System.lineSeparator());

        // also add the first line for transaction list.
        builder.append("!Type:");
        builder.append(accountType);
        builder.append(System.lineSeparator());

        return builder.toString();
    }

    private String getAccountType(TableAccountList account) {
        String accountType = account.getAccountType();

        // Translation table:
        HashMap<String, String> accountDictionary = new HashMap<>();
        accountDictionary.put(AccountTypes.CHECKING.toString(), "Bank");
        accountDictionary.put(AccountTypes.TERM.toString(), "Bank");
        accountDictionary.put(AccountTypes.CREDIT_CARD.toString(), "CCard");
        // !Type:Invst
        // Newer versions use Port instead of Invst.
        accountDictionary.put(AccountTypes.INVESTMENT.toString(), "Port");
        // Cash?

        String result = accountDictionary.get(accountType);

        return result;
    }

    private String createCreditCardHeader() {
        StringBuilder builder = new StringBuilder();

        // from quicken
        /*
N28 Degrees MasterCard
TCCard
L5,000.00
^
         */

        return "not implemented";
    }

    private TableAccountList loadAccount(Cursor cursor) {
        int accountId = cursor.getInt(cursor.getColumnIndex(QueryAllData.ACCOUNTID));
        TableAccountList account = MoneyManagerOpenHelper.getInstance(mContext)
                .getTableAccountList(accountId);
        return account;
    }
}

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

import com.money.manager.ex.database.QueryAccountBills;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the filter for transactions and provides a WHERE statement for the data selection.
 */
public class AccountTransactionsFilter {
    public AccountTransactionsFilter() {

    }

    private int mAccountId = -1;

    private StringBuilder mSelection;
    private List<String> mArguments;

    public void setAccountId(int accountId) {
        mAccountId = accountId;
    }

    public String getSelection() {
        // parse parameters and create a selection statement
        parseFilter();

        return mSelection.toString();
    }

    public String[] getSelectionArguments() {
        parseFilter();

        String[] result = new String[mArguments.size()];
        result = mArguments.toArray(result);
        return result;
    }

    private void parseFilter() {
        mSelection = new StringBuilder();
        mArguments = new ArrayList<>();

        if (mAccountId != -1) {
            mSelection.append(QueryAccountBills.ACCOUNTID);
            mSelection.append(" =? ");

            mArguments.add(Integer.toString(mAccountId));
        }

    }
}

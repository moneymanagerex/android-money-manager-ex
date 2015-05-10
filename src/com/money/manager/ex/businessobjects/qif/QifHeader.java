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

import android.database.Cursor;

import com.money.manager.ex.database.QueryAllData;

/**
 * Represents qif header record.
 */
public class QifHeader {

    public String parse(Cursor cursor) {
        // todo: implement
        StringBuilder builder = new StringBuilder();

        /*
        !Account
NCash (EUR)
TBank
D[EUR]
$57.300000
^
!Type:Cash
         */

        // todo: header depends on the account type.

        builder.append("!Account");
        builder.append(System.lineSeparator());

        // name

        builder.append("N");
        String name = cursor.getString(cursor.getColumnIndex(QueryAllData.AccountName));
        builder.append(name);
        builder.append(System.lineSeparator());

        // description

        builder.append("D");
        // todo: description
        builder.append(System.lineSeparator());

        // todo: account type

        builder.append("T");
        builder.append(System.lineSeparator());

        builder.append("^");
        builder.append(System.lineSeparator());

        // todo: also add the first line
        builder.append("!Type:Bank");
        // !Type:Invst
        builder.append(System.lineSeparator());

        return builder.toString();
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
    private String createBankHeader() {
        /*
NWestpac eSaver
TBank
^
         */
        return "not implemented";
    }
    private String createCashHeader() {
        /*
NCash (EUR)
TCash
^
         */
        return "not implemented";
    }
    private String createPortfolioHeader() {
        /*
NBawag Brokerage
TPort
^
         */
        return "not implemented";
    }
}

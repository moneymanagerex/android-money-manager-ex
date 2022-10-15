/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.datalayer;

import com.money.manager.ex.domainmodel.Currency;
import com.squareup.sqlbrite.BriteDatabase;

import javax.inject.Inject;

/**
 * Currency repository with Rx
 */

public class CurrencyRepositorySql
    extends SqlRepositoryBase<Currency> {

    public static final String TABLE_NAME = "currencyformats_v1";

    @Inject
    CurrencyRepositorySql(BriteDatabase db) {
        super(TABLE_NAME, db);
    }

    public boolean exists(String currencyCode) {
        Select query = new Select(Currency.CURRENCYID)
            .from(tableName)
            .where(Currency.CURRENCY_SYMBOL + "=?", currencyCode);

        return super.exists(query);
    }
}

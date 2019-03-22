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

package com.money.manager.ex.investment.watchlist;

import android.content.Context;

import com.money.manager.ex.R;
import com.money.manager.ex.datalayer.StockFields;
import com.money.manager.ex.domainmodel.StockHistory;
import com.money.manager.ex.database.Dataset;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.utils.MmxFileUtils;

/**
 * Watchlist query.
 */
public class WatchlistDataset
    extends Dataset {

    public WatchlistDataset(Context context) {
        super(MmxFileUtils.getRawAsString(context, R.raw.query_watchlist), DatasetType.QUERY,
                "watchlist");

        mContext = context;
    }

    private Context mContext;

    @Override
    public String[] getAllColumns() {
        return new String[] {
                StockFields.STOCKID + " AS _id",
                StockFields.STOCKID,
                StockFields.HELDAT,
                StockFields.STOCKNAME,
                StockFields.SYMBOL,
                StockHistory.DATE,
                StockHistory.VALUE
        };
    }

    public String getWatchlistSqlQuery() {
        String result = MmxFileUtils.getRawAsString(mContext, R.raw.query_watchlist);
        return result;
    }

}

/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
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
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.core;

import android.content.Context;

import com.money.manager.ex.R;

import java.util.HashMap;

/**
 * Pre-set collection of available date ranges in the app.
 *
 * Created by Alen Siljak on 23/09/2015.
 */
public class DefinedDateRanges {

    public DefinedDateRanges(Context context) {
        this.context = context;
        // todo: initialize the collection
        this.initialize();
    }

    private Context context;
    HashMap<DefinedDateRangeName, DefinedDateRange> dateRanges;

    public DefinedDateRange get(DefinedDateRangeName name) {
        return this.dateRanges.get(name);
    }

    public DefinedDateRange getByMenuId(int menuResourceId) {
        for (DefinedDateRange range : this.dateRanges.values()) {
            if (range.menuResourceId == menuResourceId) return range;
        }
        return null;
    }

    public DefinedDateRange getByNameId(int nameResourceId) {
        for (DefinedDateRange range : this.dateRanges.values()) {
            if (range.nameResourceId == nameResourceId) return range;
        }
        return null;
    }

    public DefinedDateRange getByLocalizedName(String localizedName) {
        String localizedValue;
        for (DefinedDateRange range : this.dateRanges.values()) {
            localizedValue = this.context.getString(range.nameResourceId);
            if (localizedValue.equalsIgnoreCase(localizedName)) return range;
        }
        return null;
    }

    public boolean contains(DefinedDateRangeName name) {
        return this.dateRanges.containsKey(name);
    }

    private HashMap<DefinedDateRangeName, DefinedDateRange> initialize() {
        this.dateRanges = new HashMap<>();

//        for (DefinedDateRangeName rangeName : DefinedDateRangeName.values()) {
//            DefinedDateRange range = new DefinedDateRange(this.context);
//
//            dateRanges.put(rangeName, range);
//        }

        // TODAY,
        DefinedDateRange range = create(DefinedDateRangeName.TODAY, R.string.today, R.id.menu_today);
        this.dateRanges.put(range.key, range);

        // LAST_7_DAYS
        range = create(DefinedDateRangeName.LAST_7_DAYS, R.string.last7days, R.id.menu_last7days);
        dateRanges.put(range.key, range);

        // LAST_2_WEEKS
        range = create(DefinedDateRangeName.LAST_2_WEEKS, R.string.last15days, R.id.menu_last15days);
        dateRanges.put(range.key, range);

        // CURRENT_MONTH
        range = create(DefinedDateRangeName.CURRENT_MONTH, R.string.current_month, R.id.menu_current_month);
        dateRanges.put(range.key, range);

        // LAST_30_DAYS
        range = create(DefinedDateRangeName.LAST_30_DAYS, R.string.last30days, R.id.menu_last30days);
        dateRanges.put(range.key, range);

        // LAST_3_MONTHS,
        range = create(DefinedDateRangeName.LAST_3_MONTHS, R.string.last3months, R.id.menu_last3months);
        dateRanges.put(range.key, range);

        // LAST_6_MONTHS,
        range = create(DefinedDateRangeName.LAST_6_MONTHS, R.string.last6months, R.id.menu_last6months);
        dateRanges.put(range.key, range);

        // CURRENT_YEAR,
        range = create(DefinedDateRangeName.CURRENT_YEAR, R.string.current_year, R.id.menu_current_year);
        dateRanges.put(range.key, range);

        // FUTURE_TRANSACTIONS,
        range = create(DefinedDateRangeName.FUTURE_TRANSACTIONS, R.string.future_transactions,
                R.id.menu_future_transactions);
        dateRanges.put(range.key, range);

        // ALL_TIME
        range = create(DefinedDateRangeName.ALL_TIME, R.string.all_time, R.id.menu_all_time);
        dateRanges.put(range.key, range);

        return dateRanges;
    }

    private DefinedDateRange create(DefinedDateRangeName key, int nameStringId, int menuResourceId) {
        DefinedDateRange range = new DefinedDateRange(this.context);

        range.key = key;
        range.nameResourceId = nameStringId;
        range.menuResourceId = menuResourceId;

        return range;
    }
}

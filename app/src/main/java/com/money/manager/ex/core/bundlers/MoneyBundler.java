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

package com.money.manager.ex.core.bundlers;

import android.os.Bundle;

import icepick.Bundler;
import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Custom bundler used to bundle the Money type and save instance state with IcePick.
 */

public class MoneyBundler
    implements Bundler<Money> {
    @Override
    public void put(String key, Money money, Bundle bundle) {
        bundle.putString(key, money.toString());
    }

    @Override
    public Money get(String key, Bundle bundle) {
        String value = bundle.getString(key);
        return MoneyFactory.fromString(value);
    }
}

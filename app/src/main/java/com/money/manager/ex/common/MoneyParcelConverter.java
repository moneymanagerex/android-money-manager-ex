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

package com.money.manager.ex.common;

import android.os.Parcel;

import org.parceler.ParcelConverter;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Converts Money type to parcel for Parceler.
 */
public class MoneyParcelConverter
    implements ParcelConverter<Money> {

    @Override
    public void toParcel(Money input, Parcel parcel) {
        // store as string
        parcel.writeString(input.toString());
    }

    @Override
    public Money fromParcel(Parcel parcel) {
        String amountString = parcel.readString();
        return MoneyFactory.fromString(amountString);
    }
}

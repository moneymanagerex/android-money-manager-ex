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
import android.os.Parcelable;

import com.money.manager.ex.investment.events.PriceDownloadedEvent;

import org.parceler.Parcels;

import icepick.Bundler;

/**
 * Bundler PriceDownloadedEvent with IcePick.
 */

public class PriceDownloadedEventBundler
        implements Bundler<PriceDownloadedEvent> {
    @Override
    public void put(String key, PriceDownloadedEvent event, Bundle bundle) {
        bundle.putParcelable(key, Parcels.wrap(event));
    }

    @Override
    public PriceDownloadedEvent get(String key, Bundle bundle) {
        Parcelable value = bundle.getParcelable(key);
        return Parcels.unwrap(value);
    }
}

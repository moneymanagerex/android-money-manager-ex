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
package com.money.manager.ex.inapp.util;

import android.widget.Spinner;

import java.util.Collection;
import java.util.HashMap;

/**
 * The class helps when using spinners that have a text and a value.
 * It holds two arrays and matches the text to a value.
 */
public class SpinnerValues {
    public SpinnerValues() {
        mContainer = new HashMap<>();
    }

    private HashMap<String, String> mContainer;

    public void add(String value, String text) {
        mContainer.put(value, text);
    }

    public Collection<String> getValues() {
        return mContainer.keySet();
    }

    public Collection<String> getTexts() {
        return mContainer.values();
    }

    public String[] getTextsArray() {
        String [] result = new String[mContainer.size()];
        result = mContainer.values().toArray(result);
        return result;
    }
}

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

    public String[] getValuesArray() {
        String[] result = new String[mContainer.size()];
        result = mContainer.keySet().toArray(result);
        return result;
    }

    /**
     * find the index of the given text
     * @param text
     * @return
     */
    public int getPositionOfValue(String text) {
        int result = -1;

        if (!mContainer.containsKey(text)) return result;

        int counter = 0;
        for(String content : mContainer.keySet()) {
            if (text.equalsIgnoreCase(content)) {
                result = counter;
                break;
            }
            counter += 1;
        }
        return result;
    }

    public int getPositionOfText(String text) {
        int result = -1;

        if (!mContainer.containsValue(text)) return result;

        int counter = 0;
        for(String content : mContainer.values()) {
            if (text.equalsIgnoreCase(content)) {
                result = counter;
                break;
            }
            counter += 1;
        }
        return result;
    }

    public String getValueAtPosition(int position) {
        String result = this.getValuesArray()[position];
        return result;
    }
}

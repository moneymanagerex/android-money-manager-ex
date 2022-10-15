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

package com.money.manager.ex.utils;

import java.util.List;

/**
 * Utilities that help with Lists.
 */

public class ListUtils {
    public final String SEPARATOR = ",";

    public String toCommaDelimitedString(List<String> list) {
        StringBuilder csvBuilder = new StringBuilder();

        for(String item : list){
            csvBuilder.append(item);
            csvBuilder.append(SEPARATOR);
        }

        String csv = csvBuilder.toString();
        return csv;
    }
}

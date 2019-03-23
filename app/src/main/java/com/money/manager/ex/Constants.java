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
package com.money.manager.ex;

public class Constants {
    // Java
    public static final String EMPTY_STRING = "";
    public static final int NOT_SET = -1;

    // The number of decimals used for parsing numbers in Money type.
    public static final int DEFAULT_PRECISION = 4;

    // Date/Time
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATE_SHORT_TIME_FORMAT = "yyyy-MM-dd HH:mm";
    //public static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    public static final String ISO_8601_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    public static final String LONG_DATE_PATTERN = "EEEE, dd MMMM yyyy";
    public static final String LONG_DATE_MEDIUM_DAY_PATTERN = "EEE, dd MMMM yyyy";

    // Database
    public static final String MOBILE_DATA_PATTERN = "%%mobiledata%%";
    public static final String DEFAULT_DB_FILENAME = "data.mmb";

    // Intent: Request
    public static final String INTENT_REQUEST_PREFERENCES_SCREEN = "SettingsActivity:PreferenceScreen";

    // Themes
    public static final String THEME_LIGHT = "Material Light";
    public static final String THEME_DARK = "Material Dark";

    public static final String EMAIL = "android@moneymanagerex.org";
    public static final String TIME_FORMAT = "HH:mm";

    // Amount formats
    public static final String PRICE_FORMAT = "0.00##";

    // UI
    public static final int NotificationIconSize = 25;
    public static final int NotificationBigIconSize = 48;
}

/*
 * Copyright (C) 2012-2014 Alessandro Lazzari
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

package com.money.manager.ex;

import android.content.Intent;

public class Constants {
    // Java
    public static final String EMPTY_STRING = "";
    // Database
    public static final String PATTERN_DB_DATE = "yyyy-MM-dd";
    // Transaction Type
    public static final String TRANSACTION_TYPE_WITHDRAWAL = "Withdrawal";
    public static final String TRANSACTION_TYPE_DEPOSIT = "Deposit";
    public static final String TRANSACTION_TYPE_TRANSFER = "Transfer";
    // Account Type
    public static final String ACCOUNT_TYPE_CHECKING = "Checking";
    public static final String ACCOUNT_TYPE_TERM = "Term";
    public static final String ACCOUNT_TYPE_CREDIT_CARD = "Credit Card";
    // Transaction Status
    public static final String TRANSACTION_STATUS_UNRECONCILED = "";
    public static final String TRANSACTION_STATUS_RECONCILED = "R";
    public static final String TRANSACTION_STATUS_VOID = "V";
    public static final String TRANSACTION_STATUS_FOLLOWUP = "F";
    public static final String TRANSACTION_STATUS_DUPLICATE = "D";
    // Info Table AppSettings
    public static final String INFOTABLE_USERNAME = "USERNAME";
    public static final String INFOTABLE_BASECURRENCYID = "BASECURRENCYID";
    public static final String INFOTABLE_DATEFORMAT = "DATEFORMAT";
    public static final String INFOTABLE_FINANCIAL_YEAR_START_DAY = "FINANCIAL_YEAR_START_DAY";
    public static final String INFOTABLE_FINANCIAL_YEAR_START_MONTH = "FINANCIAL_YEAR_START_MONTH";
    public static final String INFOTABLE_SKU_ORDER_ID = "SKU_ORDER_ID";
    // Intent: Action
    public static final String INTENT_ACTION_EDIT = Intent.ACTION_EDIT;
    public static final String INTENT_ACTION_INSERT = Intent.ACTION_INSERT;
    public static final String INTENT_ACTION_PASTE = Intent.ACTION_PASTE;
    // Intent: Request
    public static final String INTENT_REQUEST_PREFERENCES_SCREEN = "SettingsActivity:PreferenceScreen";
}

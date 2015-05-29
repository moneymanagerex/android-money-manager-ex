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

package com.money.manager.ex.settings;

import com.money.manager.ex.R;
import com.money.manager.ex.checkingaccount.IntentDataParameters;

public class PreferenceConstants {
    ///////////////////////////////////////////////////////////////////////////
    //                           PREFERENCES                                 //
    ///////////////////////////////////////////////////////////////////////////
    public static final Integer PREF_GENERAL = R.string.pref_general;
    public static final Integer PREF_LOOK_FEEL = R.string.pref_look_feel;
    public static final Integer PREF_SECURITY = R.string.pref_security;
    public static final Integer PREF_DATABASE = R.string.pref_database;
    public static final Integer PREF_SORT_PAYEE = R.string.pref_sort_payee;

    public static final Integer PREF_LAST_VERSION_KEY = R.string.pref_last_version_key;
    public static final Integer PREF_DONATE_LAST_VERSION_KEY = R.string.pref_donate_last_version_key;
    public static final Integer PREF_LAST_DB_PATH_SHOWN = R.string.pref_last_db_path_shown;

    // Database
    public static final Integer PREF_DATABASE_PATH = R.string.pref_database_path;

    // General
    public static final Integer PREF_USER_NAME = R.string.pref_user_name;
    public static final Integer PREF_DATE_FORMAT = R.string.pref_date_format;
    public static final Integer PREF_FINANCIAL_YEAR_STARTDATE = R.string.pref_financial_year_startdate;
    public static final Integer PREF_FINANCIAL_YEAR_STARTMONTH = R.string.pref_financial_year_startmonth;
    public static final Integer PREF_BASE_CURRENCY = R.string.pref_base_currency;
    public static final Integer PREF_ACCOUNT_OPEN_VISIBLE = R.string.pref_account_open_visible;
    public static final Integer PREF_ACCOUNT_FAV_VISIBLE = R.string.pref_account_fav_visible;
    public static final Integer PREF_DROPBOX_MODE = R.string.pref_dropbox_mode;
    public static final Integer PREF_THEME = R.string.pref_theme;
    public static final Integer PREF_SHOW_TRANSACTION = R.string.pref_show_transaction;
    public static final Integer PREF_HIDE_RECONCILED_AMOUNTS = R.string.pref_transaction_hide_reconciled_amounts;
    public static final Integer PREF_DEFAULT_ACCOUNT = R.string.pref_default_account;

    public static final Integer PREF_EDIT_PASSCODE = R.string.pref_edit_passcode;
    public static final Integer PREF_DISABLE_PASSCODE = R.string.pref_disable_passcode;
    public static final Integer PREF_ACTIVE_PASSCODE = R.string.pref_active_passcode;
    public static final Integer PREF_DROPBOX_LINK = R.string.pref_dropbox_link;
    public static final Integer PREF_DROPBOX_UNLINK = R.string.pref_dropbox_unlink;
    public static final Integer PREF_DROPBOX_LINKED_FILE = R.string.pref_dropbox_linked_file;
    public static final Integer PREF_DONATE = R.string.pref_donate;
    public static final Integer PREF_VERSION_NAME = R.string.pref_version_name;


    public static final Integer PREF_SQLITE_VERSION = R.string.pref_sqlite_version;
    public static final Integer PREF_DROPBOX_DOWNLOAD = R.string.pref_dropbox_download;
    public static final Integer PREF_DROPBOX_UPLOAD = R.string.pref_dropbox_upload;
    public static final Integer PREF_DROPBOX_HOWITWORKS = R.string.pref_dropbox_how_it_works;
    public static final Integer PREF_DROPBOX_TIMES_REPEAT = R.string.pref_dropbox_times_repeat_service;
    public static final Integer PREF_DROPBOX_UPLOAD_IMMEDIATE = R.string.pref_dropbox_upload_immediate;
    public static final Integer PREF_TRANSACTION_SHOWN_BALANCE = R.string.pref_transaction_shown_balance;
    public static final Integer PREF_DATABASE_BACKUP = R.string.pref_database_backup;
    public static final Integer PREF_APPLICATION_FONT = R.string.pref_application_font;
    public static final Integer PREF_APPLICATION_FONT_SIZE = R.string.pref_application_font_size;
    public static final Integer PREF_DEFAULT_STATUS = R.string.pref_default_status;
    public static final Integer PREF_DEFAULT_PAYEE = R.string.pref_default_payee;
    public static final Integer PREF_TEXT_SEARCH_TYPE = R.string.pref_text_search_type;
    public static final Integer PREF_LOCALE = R.string.pref_locale;
    // others preference setting don't display
    public static final String PREF_DROPBOX_ACCOUNT_PREFS_NAME = "com.money.manager.ex_dropbox_preferences";
    public static final String PREF_DROPBOX_ACCESS_KEY_NAME = "ACCESS_KEY";
    public static final String PREF_DROPBOX_ACCESS_SECRET_NAME = "ACCESS_SECRET";
    public static final String PREF_DROPBOX_REMOTE_FILE = "DROPBOX_REMOTE_FILE";
    // check repeating transacion
    public static final Integer PREF_REPEATING_TRANSACTION_CHECK = R.string.pref_repeating_transaction_check_time;
    public static final Integer PREF_REPEATING_TRANSACTION_NOTIFICATIONS = R.string.pref_repeating_transaction_notifications;
    // Wiki dropbox
    public static final Integer PREF_DROPBOX_WIKI = R.string.pref_dropbox_wiki;

    public static final Integer PREF_SHOW_TUTORIAL = R.string.pref_show_tutorial;
    public static final Integer PREF_DASHBOARD_GROUP_VISIBLE = R.string.pref_dashboard_group_visibility;
}

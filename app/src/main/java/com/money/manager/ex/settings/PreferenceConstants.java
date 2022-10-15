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

package com.money.manager.ex.settings;

import com.money.manager.ex.R;

public class PreferenceConstants {
    ///////////////////////////////////////////////////////////////////////////
    //                           PREFERENCES                                 //
    ///////////////////////////////////////////////////////////////////////////
    public static final Integer PREF_GENERAL = R.string.pref_general;
    public static final Integer PREF_LOOK_FEEL = R.string.pref_look_feel;
    public static final Integer PREF_SECURITY = R.string.pref_security;
    public static final Integer PREF_DATABASE = R.string.pref_database;

//    public static final Integer PREF_LAST_VERSION_KEY = R.string.pref_last_version_key;
    public static final Integer PREF_DONATE_LAST_VERSION_KEY = R.string.pref_donate_last_version_key;
    public static final Integer PREF_LAST_DB_PATH_SHOWN = R.string.pref_last_db_path_shown;

    // General
    public static final Integer PREF_FINANCIAL_YEAR_STARTDATE = R.string.pref_financial_year_startdate;
    public static final Integer PREF_FINANCIAL_YEAR_STARTMONTH = R.string.pref_financial_year_startmonth;
    public static final Integer PREF_BASE_CURRENCY = R.string.pref_base_currency;

    public static final Integer PREF_EDIT_PASSCODE = R.string.pref_edit_passcode;
    public static final Integer PREF_DISABLE_PASSCODE = R.string.pref_disable_passcode;
    public static final Integer PREF_ACTIVE_PASSCODE = R.string.pref_active_passcode;
//    public static final Integer PREF_DROPBOX_LINK = R.string.pref_dropbox_link;
//    public static final Integer PREF_DROPBOX_UNLINK = R.string.pref_dropbox_unlink;
    public static final Integer PREF_DONATE = R.string.pref_donate;
    public static final Integer PREF_VERSION_NAME = R.string.pref_version_name;


    public static final Integer PREF_SQLITE_VERSION = R.string.pref_sqlite_version;
//    public static final Integer PREF_DROPBOX_UPLOAD = R.string.pref_dropbox_upload;
    public static final Integer PREF_TRANSACTION_SHOWN_BALANCE = R.string.pref_transaction_shown_balance;
    public static final Integer PREF_DATABASE_BACKUP = R.string.pref_database_backup;
    public static final Integer PREF_APPLICATION_FONT = R.string.pref_application_font;
    public static final Integer PREF_APPLICATION_FONT_SIZE = R.string.pref_application_font_size;
    public static final Integer PREF_DEFAULT_STATUS = R.string.pref_default_status;
    public static final Integer PREF_DEFAULT_PAYEE = R.string.pref_default_payee;
    public static final Integer PREF_TEXT_SEARCH_TYPE = R.string.pref_text_search_type;

    // others preference setting don't display
    public static final String PREF_DROPBOX_ACCOUNT_PREFS_NAME = "com.money.manager.ex_dropbox_preferences";

    // check repeating transaction
    public static final Integer PREF_REPEATING_TRANSACTION_NOTIFICATIONS = R.string.pref_repeating_transaction_notifications;
    public static final Integer PREF_REPEATING_TRANSACTION_CHECK = R.string.pref_repeating_transaction_check_time;

    public static final Integer PREF_DASHBOARD_GROUP_VISIBLE = R.string.pref_dashboard_group_visibility;

    // Recent files preferences
    public static final String RECENT_DB_PREFERENCES = "com.money.manager.ex.recent_db";
    public static final String SYNC_PREFERENCES = "com.money.manager.ex.sync_preferences";

    //SMS Prefs
    public static final Integer PREF_SMS_AUTOMATIC_TRANSACTIONS = R.string.pref_sms_auto_trans;
    public static final Integer PREF_SMS_TRANS_STATUS_NOTIFICATION = R.string.pref_sms_trans_status_notification;

}

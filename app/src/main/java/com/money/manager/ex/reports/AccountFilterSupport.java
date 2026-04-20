/*
 * Copyright (C) 2012-2026 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.reports;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.R;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.settings.LookAndFeelSettings;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public final class AccountFilterSupport {

    private AccountFilterSupport() {
    }

    public interface OnAccountsSelectedListener {
        void onAccountsSelected(List<Long> selectedIds);
    }

    public static boolean isAccountFilterMenuItem(int itemId) {
        return itemId == R.id.menu_account_filter_all
                || itemId == R.id.menu_account_filter_open
                || itemId == R.id.menu_account_filter_favorite
                || itemId == R.id.menu_account_filter_custom;
    }

    public static int getFilterMode(LookAndFeelSettings settings, String modePrefKey, int defaultMode) {
        String storedValue = settings.get(modePrefKey, Integer.toString(defaultMode));
        try {
            return Integer.parseInt(storedValue);
        } catch (Exception e) {
            return defaultMode;
        }
    }

    public static void saveFilterMode(LookAndFeelSettings settings, String modePrefKey, int mode) {
        settings.set(modePrefKey, Integer.toString(mode));
    }

    public static ArrayList<Long> parseSelectedAccountIds(LookAndFeelSettings settings, String customPrefKey) {
        String raw = settings.get(customPrefKey, "");
        ArrayList<Long> result = new ArrayList<>();
        if (raw.trim().isEmpty()) {
            return result;
        }

        String[] ids = raw.split(",");
        for (String id : ids) {
            if (id.trim().isEmpty()) {
                continue;
            }
            try {
                result.add(Long.parseLong(id.trim()));
            } catch (Exception e) {
                Timber.w(e, "Invalid account id in filter: %s", id);
            }
        }
        return result;
    }

    public static void saveSelectedAccountIds(LookAndFeelSettings settings, String customPrefKey, List<Long> ids) {
        settings.set(customPrefKey, joinIds(ids));
    }

    public static String getSelectionForAccountIdColumn(int mode, List<Long> customAccountIds, String accountIdColumn) {
        if (mode == R.id.menu_account_filter_all) {
            return "";
        }
        if (mode == R.id.menu_account_filter_open) {
            return accountIdColumn
                    + " IN (SELECT ACCOUNTID FROM ACCOUNTLIST_V1 WHERE lower(STATUS) = 'open')";
        }
        if (mode == R.id.menu_account_filter_favorite) {
            return accountIdColumn
                    + " IN (SELECT ACCOUNTID FROM ACCOUNTLIST_V1 WHERE lower(FAVORITEACCT) = 'true')";
        }
        if (mode == R.id.menu_account_filter_custom) {
            if (customAccountIds.isEmpty()) {
                return "1=2";
            }
            return accountIdColumn + " IN (" + joinIds(customAccountIds) + ")";
        }
        return "";
    }

    public static String getSelectionForAccountIdColumn(int mode, LookAndFeelSettings settings,
            String customPrefKey, String accountIdColumn) {
        List<Long> selectedAccountIds = parseSelectedAccountIds(settings, customPrefKey);
        return getSelectionForAccountIdColumn(mode, selectedAccountIds, accountIdColumn);
    }

    public static String getWhereClauseForAccountIdColumn(int mode, LookAndFeelSettings settings,
            String customPrefKey, String accountIdColumn) {
        String selection = getSelectionForAccountIdColumn(mode, settings, customPrefKey, accountIdColumn);
        if (selection.trim().isEmpty()) {
            return "";
        }
        return "WHERE " + selection;
    }

    public static void showAndPersistAccountSelectionDialog(Context context, LookAndFeelSettings settings,
            String customPrefKey, Runnable onSelectionSaved) {
        List<Long> selected = parseSelectedAccountIds(settings, customPrefKey);
        showAccountSelectionDialog(context, selected, selectedIds -> {
            saveSelectedAccountIds(settings, customPrefKey, selectedIds);
            onSelectionSaved.run();
        });
    }

    public static void showAccountSelectionDialog(Context context, List<Long> selected,
            OnAccountsSelectedListener listener) {
        QueryAccountBills queryAccountBills = new QueryAccountBills(context);
        Cursor cursor = context.getContentResolver().query(
                queryAccountBills.getUri(),
                null,
                null,
                null,
                QueryAccountBills.ACCOUNTTYPE + ", upper(" + QueryAccountBills.ACCOUNTNAME + ")");

        if (cursor == null) {
            return;
        }

        final ArrayList<Long> accountIds = new ArrayList<>();
        final ArrayList<String> accountNames = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                long accountId = cursor.getLong(cursor.getColumnIndexOrThrow(QueryAccountBills.ACCOUNTID));
                String accountName = cursor.getString(cursor.getColumnIndexOrThrow(QueryAccountBills.ACCOUNTNAME));
                accountIds.add(accountId);
                accountNames.add(accountName);
            }
        } finally {
            cursor.close();
        }

        final boolean[] checkedItems = new boolean[accountIds.size()];
        for (int i = 0; i < accountIds.size(); i++) {
            checkedItems[i] = selected.contains(accountIds.get(i));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.menu_account_filter_custom);
        builder.setMultiChoiceItems(accountNames.toArray(new CharSequence[0]), checkedItems,
                (dialog, which, isChecked) -> checkedItems[which] = isChecked);
        builder.setPositiveButton(android.R.string.ok, (dialog, which) -> {
            ArrayList<Long> selectedIds = new ArrayList<>();
            for (int i = 0; i < accountIds.size(); i++) {
                if (checkedItems[i]) {
                    selectedIds.add(accountIds.get(i));
                }
            }
            listener.onAccountsSelected(selectedIds);
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.show();
    }

    private static String joinIds(List<Long> ids) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(ids.get(i));
        }
        return builder.toString();
    }
}

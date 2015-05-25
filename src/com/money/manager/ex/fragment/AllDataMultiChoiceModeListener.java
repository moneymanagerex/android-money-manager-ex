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
package com.money.manager.ex.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.money.manager.ex.CheckingAccountActivity;
import com.money.manager.ex.R;
import com.money.manager.ex.adapter.AllDataAdapter;
import com.money.manager.ex.adapter.DrawerMenuItem;
import com.money.manager.ex.adapter.DrawerMenuItemAdapter;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.database.QueryAllData;

import java.util.ArrayList;

/**
 * class to manage multi choice mode
 */
public class AllDataMultiChoiceModeListener
        implements AbsListView.MultiChoiceModeListener {

    public void setListener(IAllDataMultiChoiceModeListenerCallbacks callbacks) {
        mCallbacks = callbacks;
    }

    private IAllDataMultiChoiceModeListenerCallbacks mCallbacks;

    @Override
    public boolean onPrepareActionMode(ActionMode mode, android.view.Menu menu) {

        return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mCallbacks.onDestroyActionMode();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, android.view.Menu menu) {
        mCallbacks.onMultiChoiceCreated(menu);
        return true;
    }

    /**
     * Handle the toolbar icon click (delete, copy, etc.)
     * @param mode
     * @param item
     * @return
     */
    @Override
    public boolean onActionItemClicked(ActionMode mode, android.view.MenuItem item) {
        boolean result;

        switch (item.getItemId()) {
            case R.id.menu_select_all:
                mCallbacks.onSelectAllRecordsClicked();
                // do not finish the selection mode after this!
                result = false;
                break;
            case R.id.menu_change_status:
                mCallbacks.onChangeTransactionStatusClicked();
                result = true;
                break;
            case R.id.menu_duplicate_transactions:
                mCallbacks.onDuplicateTransactionsClicked();
                result = true;
                break;
            case R.id.menu_delete:
                mCallbacks.onDeleteClicked();
                result = true;
                break;
            case R.id.menu_none:
            case R.id.menu_reconciled:
            case R.id.menu_follow_up:
            case R.id.menu_duplicate:
            case R.id.menu_void:
                String status = Character.toString(item.getAlphabeticShortcut());
                mCallbacks.onTransactionStatusClicked(status);
                result = true;
                break;
            default:
                // nothing
                result = false;
        }

        if (result) {
            mode.finish();
        }

        return result;
    }

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
        mCallbacks.onItemCheckedStateChanged(position, checked);
    }
}

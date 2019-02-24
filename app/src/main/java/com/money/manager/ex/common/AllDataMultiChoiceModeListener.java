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
package com.money.manager.ex.common;

import android.view.ActionMode;
import android.widget.AbsListView;

import com.money.manager.ex.R;

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

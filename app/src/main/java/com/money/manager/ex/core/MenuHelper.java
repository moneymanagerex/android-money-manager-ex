/*
 * Copyright (C) 2012-2016 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.core;

import android.content.Context;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;

import com.money.manager.ex.R;

/**
 * Helper methods to work with menus.
 */
public class MenuHelper {

    public MenuHelper(Context context) {
        mContext = context;
    }

    private Context mContext;

    public void addEditToContextMenu(ContextMenu menu) {
        menu.add(Menu.NONE, ContextMenuIds.EDIT.getId(), Menu.NONE, getContext().getString(R.string.edit));
    }

    public void addDeleteToContextMenu(ContextMenu menu) {
        menu.add(Menu.NONE, ContextMenuIds.DELETE.getId(), Menu.NONE, getContext().getString(R.string.delete));
    }

    public MenuItem addToContextMenu(ContextMenuIds itemId, ContextMenu menu) {
        return menu.add(Menu.NONE, itemId.getId(), Menu.NONE, getItemText(itemId));
    }

    private String getItemText(ContextMenuIds menuId) {
        // todo add remaining items
        switch (menuId) {
            case DELETE:
                return getContext().getString(R.string.delete);
            case DownloadPrice:
                return getContext().getString(R.string.download_price);
            case EDIT:
                return getContext().getString(R.string.edit);
            case EditPrice:
                return getContext().getString(R.string.edit_price);
            case Print:
                return getContext().getString(R.string.print);
            case SaveAsHtml:
                return getContext().getString(R.string.save_as_html);
            default:
                return "N/A";
        }
    }

    private Context getContext() {
        return mContext;
    }
}

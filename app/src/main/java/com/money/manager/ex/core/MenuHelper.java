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

package com.money.manager.ex.core;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.money.manager.ex.R;

import androidx.core.view.MenuItemCompat;

/**
 * Helper methods to work with menus.
 */
public class MenuHelper {

    public static final int edit = 1;
    public static final int save = 2;

    public MenuHelper(Context context, Menu menu) {
        mContext = context;
        this.menu = menu;

        uiHelper = new UIHelper(context);
    }

    public UIHelper uiHelper;
    public Menu menu;

    private Context mContext;

    public void addEditToContextMenu() {
        menu.add(Menu.NONE, ContextMenuIds.EDIT.getId(), Menu.NONE, getContext().getString(R.string.edit));
    }

    public void addDeleteToContextMenu() {
        menu.add(Menu.NONE, ContextMenuIds.DELETE.getId(), Menu.NONE, getContext().getString(R.string.delete));
    }

    public MenuItem addToContextMenu(ContextMenuIds itemId) {
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

    public void addSaveToolbarIcon() {
        MenuItem item = menu.add(Menu.NONE, save, Menu.NONE, R.string.save);
        MenuItemCompat.setShowAsAction(item, MenuItem.SHOW_AS_ACTION_ALWAYS);

        IconicsDrawable icon = uiHelper.getIcon(GoogleMaterial.Icon.gmd_check)
            //.color(uiHelper.getPrimaryTextColor());
            .color(uiHelper.getToolbarItemColor());
        item.setIcon(icon);
    }

    public MenuItem add(int id, int titleResId, IIcon icon) {
        // group id, item id, order, title
        MenuItem item = menu.add(Menu.NONE, id, Menu.NONE, titleResId);

        item.setIcon(uiHelper.getIcon(icon));

        // allow further customization by the client.
        return item;
    }
}

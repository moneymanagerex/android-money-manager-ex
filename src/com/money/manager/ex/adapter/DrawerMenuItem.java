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

package com.money.manager.ex.adapter;

public class DrawerMenuItem {
    private int mId;
    private String mItemText;
    private Integer mIcon;
    private String mAlphabeticShortcut;

    public DrawerMenuItem(int id, String itemText) {
        setId(id);
        setItemText(itemText);
    }

    public DrawerMenuItem(int id, String itemText, Integer icon) {
        setId(id);
        setItemText(itemText);
        setIcon(icon);
    }

    public DrawerMenuItem(int id, String itemText, Integer icon, String alphabeticShortcut) {
        setId(id);
        setItemText(itemText);
        setIcon(icon);
        setAlphabeticShortcut(alphabeticShortcut);
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        this.mId = id;
    }

    public String getItemText() {
        return mItemText;
    }

    public void setItemText(String itemText) {
        this.mItemText = itemText;
    }

    public Integer getIcon() {
        return mIcon;
    }

    public void setIcon(Integer mIcon) {
        this.mIcon = mIcon;
    }

    public String getAlphabeticShortcut() {
        return mAlphabeticShortcut;
    }

    public void setAlphabeticShortcut(String mAlphabeticShortcut) {
        this.mAlphabeticShortcut = mAlphabeticShortcut;
    }
}

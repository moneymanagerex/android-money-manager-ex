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

package com.money.manager.ex.home;

import android.graphics.drawable.Drawable;

public class DrawerMenuItem {
    private Integer mId;
    private String mText;
    private Integer mIcon;
    private Drawable mIconDrawable;
    private String mShortcut;
    private Boolean mDivider = Boolean.FALSE;

    private Object mTag;

    public Integer getId() {
        return mId;
    }

    public DrawerMenuItem withId(Integer id) {
        this.mId = id;
        return this;
    }

    public String getText() {
        return mText;
    }

    public DrawerMenuItem withText(String text) {
        this.mText = text;
        return this;
    }

    public Integer getIcon() {
        return mIcon;
    }

    public Drawable getIconDrawable() {
        return mIconDrawable;
    }

    public DrawerMenuItem withIcon(Integer icon) {
        this.mIcon = icon;
        return this;
    }

    public DrawerMenuItem withIconDrawable(Drawable iconDrawable) {
        this.mIconDrawable = iconDrawable;
        return this;
    }

    public String getShortcut() {
        return mShortcut;
    }

    public DrawerMenuItem withShortcut(String shortcut) {
        this.mShortcut = shortcut;
        return this;
    }

    public Boolean hasDivider() {
        return mDivider;
    }

    public DrawerMenuItem withDivider(Boolean divider) {
        this.mDivider = divider;
        return this;
    }

    public void setTag(Object tag) {
        this.mTag = tag;
    }

    public Object getTag() {
        return this.mTag;
    }
}

/*
 * Copyright 2014 Mike Penz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mikepenz.mmex_icon_font_typeface_library;

import android.content.Context;
import android.graphics.Typeface;

import com.mikepenz.iconics.typeface.IIcon;
import com.mikepenz.iconics.typeface.ITypeface;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class MMXIconFont implements ITypeface {
    private static final String TTF_FILE = "mmex.ttf";
    private static Typeface typeface;
    private static HashMap<String, Character> mChars;

    @Override
    public IIcon getIcon(final String key) {
        return Icon.valueOf(key);
    }

    @Override
    public HashMap<String, Character> getCharacters() {
        if (null == mChars) {
            final HashMap<String, Character> aChars = new HashMap<>();
            for (final Icon v : Icon.values()) {
                aChars.put(v.name(), v.character);
            }
            mChars = aChars;
        }
        return mChars;
    }

    @Override
    public String getMappingPrefix() {
        return "mmx";
    }

    @Override
    public String getFontName() {
        return "MMEX Icon Font";
    }

    @Override
    public String getVersion() {
        return "1.0.0.0";
    }

    @Override
    public int getIconCount() {
        return mChars.size();
    }

    @Override
    public Collection<String> getIcons() {
        final Collection<String> icons = new LinkedList<String>();
        for (final Icon value : Icon.values()) {
            icons.add(value.name());
        }
        return icons;
    }

    @Override
    public String getAuthor() {
        return "Money Manager Ex Project";
    }

    @Override
    public String getUrl() {
        return "http://android.moneymanagerex.org";
    }

    @Override
    public String getDescription() {
        return "MMEX 4 Android custom icon font";
    }

    @Override
    public String getLicense() {
        return "LGPLv3";
    }

    @Override
    public String getLicenseUrl() {
        return "https://www.gnu.org/licenses/lgpl-3.0.en.html";
    }

    @Override
    public Typeface getTypeface(final Context context) {
        if (null == typeface) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + TTF_FILE);
            } catch (final Exception e) {
                return null;
            }
        }
        return typeface;
    }

    /**
     * Some icons removed as they are included in GMD: https://design.google.com/icons/
     */
    public enum Icon implements IIcon {
        mmx_law('\u0061'),
        mmx_briefcase('\u0067'),
        //		mmx_dropbox('\u0069'),
        mmx_magnifier('\u006a'),
        mmx_back_in_time('\u006b'),
        mmx_reports('\u006d'),
        mmx_temple('\u0070'),
        mmx_tag_empty('\u0073'),
        mmx_wallet('\u0074'),
        mmx_calculator('\u007a'),
        mmx_git_branch('\u0046'),
        mmx_clipboard('\u0049'),
        mmx_globe_outline('\u004b'),
        mmx_dollar_bill('\u004c'),
        mmx_hash('\u004d'),
        mmx_floppy_disk('\u004e'),
        mmx_calendar('\u004a'),
        mmx_money_banknote('\u004f'),
        mmx_share_square('\u0045'),
        mmx_chart_pie('\u0050'),
        mmx_filter('\u0052'),
        mmx_credit_card('\u0053'),
        mmx_report_page('\u0054'),
        mmx_sort_amount_asc('\u0055'),
        mmx_sort_amount_desc('\u0056'),
        mmx_print('\u0059'),
        mmx_check_5('\u0030'),
        mmx_arrow_down('\u0062'),
        mmx_arrow_left('\u0062'),
        mmx_arrow_right('\u0062'),
        mmx_arrow_up('\u0062');

        // remember the typeface so we can use it later
        private static ITypeface typeface;
        char character;

        Icon(final char character) {
            this.character = character;
        }

        public String getFormattedName() {
            return "{" + name() + "}";
        }

        public char getCharacter() {
            return character;
        }

        public String getName() {
            return name();
        }

        public ITypeface getTypeface() {
            if (null == typeface) {
                typeface = new MMXIconFont();
            }
            return typeface;
        }
    }
}

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

public class MMEXIconFont implements ITypeface {
    private static final String TTF_FILE = "mmex-icon-font-font-v1.0.0.0.ttf";
    private static Typeface typeface = null;
    private static HashMap<String, Character> mChars;

    @Override
    public IIcon getIcon(String key) {
        return Icon.valueOf(key);
    }

    @Override
    public HashMap<String, Character> getCharacters() {
        if (mChars == null) {
            HashMap<String, Character> aChars = new HashMap<String, Character>();
            for (Icon v : Icon.values()) {
                aChars.put(v.name(), v.character);
            }
            mChars = aChars;
        }
        return mChars;
    }

    @Override
    public String getMappingPrefix() {
        return "mme";
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
        Collection<String> icons = new LinkedList<String>();
        for (Icon value : Icon.values()) {
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
    public Typeface getTypeface(Context context) {
        if (typeface == null) {
            try {
                typeface = Typeface.createFromAsset(context.getAssets(), "fonts/" + TTF_FILE);
            } catch (Exception e) {
                return null;
            }
        }
        return typeface;
    }

    public enum Icon implements IIcon {
        mme_law('\u0061'),
		mme_icomoon('\u0062'),
		mme_settings('\u0063'),
		mme_building('\u0065'),
		mme_euro('\u0066'),
		mme_briefcase_case_two('\u0067'),
		mme_folder2('\u0068'),
		mme_dropbox('\u0069'),
		mme_magnifier('\u006a'),
		mme_back_in_time('\u006b'),
		mme_gift('\u0064'),
		mme_question('\u006c'),
		mme_reports('\u006d'),
		mme_ic_right_arrow('\u006e'),
		mme_chevron_right('\u006f'),
		mme_icomoon_2('\u0070'),
		mme_bookmark('\u0071'),
		mme_tag('\u0072'),
		mme_tag_empty('\u0073'),
		mme_vallet('\u0074'),
		mme_group('\u0076'),
		mme_chevron_down('\u0077'),
		mme_chevron_left('\u0078'),
		mme_chevron_up('\u0079'),
		mme_calculator('\u007a'),
		mme_dollar('\u0041'),
		mme_erase('\u0042'),
		mme_git_branch('\u0046'),
		mme_alert('\u0047'),
		mme_user('\u0048'),
		mme_clipboard('\u0049'),
		mme_globe_outline('\u004b'),
		mme_dollar_bill('\u004c'),
		mme_hash('\u004d'),
		mme_floppy_disk('\u004e'),
		mme_calendar('\u004a'),
		mme_money_banknote('\u004f'),
		mme_hospital_square('\u0043'),
		mme_minus_square('\u0044'),
		mme_share_square('\u0045'),
		mme_chart_pie('\u0050'),
		mme_star('\u0075'),
		mme_star_outline('\u0051'),
		mme_filter('\u0052'),
		mme_credit_card('\u0053'),
		mme_report_page('\u0054'),
		mme_sort_amount_asc('\u0055'),
		mme_sort_amount_desc('\u0056'),
		mme_pencil('\u0057'),
		mme_scissors('\u0058'),
		mme_print('\u0059'),
		mme_refresh('\u005a'),
		mme_check_5('\u0030');

        char character;

        Icon(char character) {
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

        // remember the typeface so we can use it later
        private static ITypeface typeface;

        public ITypeface getTypeface() {
            if (typeface == null) {
                typeface = new MMEXIconFont();
            }
            return typeface;
        }
    }
}

/*
 * Copyright (C) 2012-2024 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.tag;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;

import androidx.fragment.app.FragmentManager;

import com.money.manager.ex.R;
import com.money.manager.ex.common.BaseListFragment;
import com.money.manager.ex.common.MmxBaseFragmentActivity;

public class TagActivity
        extends MmxBaseFragmentActivity {

    public static final String INTENT_RESULT_TAGID = "TagActivity:TagId";
    public static final String INTENT_RESULT_TAGNAME = "TagActivity:TagName";
    private static final String FRAGMENTTAG = TagActivity.class.getSimpleName() + "_Fragment";

    TagListFragment listFragment = new TagListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_toolbar_activity);

        super.inizializeCommon(listFragment,FRAGMENTTAG);
        // process intent
        Intent intent = getIntent();
        String action = intent.getAction();

        if (!TextUtils.isEmpty(action)) {
            TagListFragment.mAction = action;
        }

    }
}

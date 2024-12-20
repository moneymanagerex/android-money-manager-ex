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
package com.money.manager.ex.payee;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.money.manager.ex.common.MmxBaseFragmentActivity;

public class PayeeActivity
    extends MmxBaseFragmentActivity {

    public static final String INTENT_RESULT_PAYEEID = "PayeeActivity:PayeeId";
    public static final String INTENT_RESULT_PAYEENAME = "PayeeActivity:PayeeName";
    private static final String FRAGMENTTAG = PayeeActivity.class.getSimpleName() + "_Fragment";

    PayeeListFragment listFragment = new PayeeListFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.inizializeCommon(listFragment,FRAGMENTTAG);

        Intent intent = getIntent();
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)) {
            PayeeListFragment.mAction = action;
        }

    }


}

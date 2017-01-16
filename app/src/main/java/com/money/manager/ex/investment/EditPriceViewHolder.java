/*
 * Copyright (C) 2012-2017 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.investment;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.view.View;

import com.money.manager.ex.R;
import com.money.manager.ex.view.DateDisplay;
import com.money.manager.ex.view.RobotoTextView;
import com.shamanland.fonticon.FontIconView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * View Holder pattern for edit price binaryDialog.
 */
public class EditPriceViewHolder {

    @BindView(R.id.amountTextView) public RobotoTextView amountTextView;
//    @BindView(R.id.dateTextView) public RobotoTextView dateTextView;
    @BindView(R.id.dateControl) public DateDisplay dateDisplay;
    @BindView(R.id.previousDayButton) @Nullable public FontIconView previousDayButton;
    @BindView(R.id.nextDayButton) @Nullable public FontIconView nextDayButton;

    public void bind(View view) {
        ButterKnife.bind(this, view);
    }

    public void bind(Activity activity) {
        ButterKnife.bind(this, activity);
    }
}

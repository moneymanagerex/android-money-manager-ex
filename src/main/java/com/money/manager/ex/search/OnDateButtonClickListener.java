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

package com.money.manager.ex.search;

import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.utils.DateTimeUtils;
import com.money.manager.ex.utils.DateUtils;

import java.util.Calendar;

import hirondelle.date4j.DateTime;

/**
 * Click listener
 */
public class OnDateButtonClickListener
    implements View.OnClickListener {

    public OnDateButtonClickListener(FragmentActivity parent, TextView txtFromDate) {
        super();

        mParent = parent;
        mTextView = txtFromDate;
    }

    public static final String DATEPICKER_TAG = "datepicker";

    private FragmentActivity mParent;
    private TextView mTextView;

    @Override
    public void onClick(View v) {
        Calendar calendar = Calendar.getInstance();
        if (!TextUtils.isEmpty(mTextView.getText())) {
            calendar.setTime(DateUtils.getDateFromUserString(mParent.getApplicationContext(), mTextView.getText().toString()));
        }

        CalendarDatePickerDialogFragment datePicker = new CalendarDatePickerDialogFragment()
                .setOnDateSetListener(mDateSetListener)
                .setPreselectedDate(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))
                .setThemeDark();
        datePicker.show(mParent.getSupportFragmentManager(), DATEPICKER_TAG);
    }

    private CalendarDatePickerDialogFragment.OnDateSetListener mDateSetListener = new CalendarDatePickerDialogFragment.OnDateSetListener() {
        @Override
        public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
            try {
                String dateString = FormatUtilities.getIsoDateFrom(year, monthOfYear + 1, dayOfMonth);

                // Save the actual value as tag.
                mTextView.setTag(dateString);

                DateTime date = new DateTime(dateString);
                String displayText = DateTimeUtils.getUserStringFromDateTime(mParent.getApplicationContext(), date);
                mTextView.setText(displayText);
            } catch (Exception e) {
                ExceptionHandler handler = new ExceptionHandler(mParent, this);
                handler.handle(e, "date selected in search");
            }
        }
    };
}

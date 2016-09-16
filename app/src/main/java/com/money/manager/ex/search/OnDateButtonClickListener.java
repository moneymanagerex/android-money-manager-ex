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
import com.money.manager.ex.Constants;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.log.ExceptionHandler;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import timber.log.Timber;

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
        DateTime dateTime = MmxDateTimeUtils.today();
        String calendarValue = mTextView.getText().toString();

        if (!TextUtils.isEmpty(calendarValue)) {
            String userDatePattern = new MmxDateTimeUtils(mParent.getApplicationContext()).getUserDatePattern();
            DateTimeFormatter formatter = DateTimeFormat.forPattern(userDatePattern);
            dateTime = formatter.parseDateTime(calendarValue);
        }

        CalendarDatePickerDialogFragment datePicker = new CalendarDatePickerDialogFragment()
            .setFirstDayOfWeek(MmxDateTimeUtils.getFirstDayOfWeek())
            .setOnDateSetListener(mDateSetListener)
            .setPreselectedDate(dateTime.getYear(), dateTime.getMonthOfYear() - 1, dateTime.getDayOfMonth());
        if (new UIHelper(mParent).isDarkTheme()) {
            datePicker.setThemeDark();
        }
        datePicker.show(mParent.getSupportFragmentManager(), DATEPICKER_TAG);
    }

    private CalendarDatePickerDialogFragment.OnDateSetListener mDateSetListener = new CalendarDatePickerDialogFragment.OnDateSetListener() {
        @Override
        public void onDateSet(CalendarDatePickerDialogFragment dialog, int year, int monthOfYear, int dayOfMonth) {
            try {
                DateTime date = new DateTime(year, monthOfYear + 1, dayOfMonth, 0, 0);

                // Save the string value as tag.
                String dateString = date.toString(Constants.ISO_DATE_FORMAT);
                mTextView.setTag(dateString);

                String displayText = new MmxDateTimeUtils(mParent.getApplicationContext()).getUserStringFromDateTime(date);
                mTextView.setText(displayText);
            } catch (Exception e) {
                Timber.e(e, "date selected in search");
            }
        }
    };
}

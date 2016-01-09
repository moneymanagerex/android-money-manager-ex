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

import com.fourmob.datetimepicker.date.DatePickerDialog;
import com.money.manager.ex.Constants;
import com.money.manager.ex.core.ExceptionHandler;
import com.money.manager.ex.core.FormatUtilities;
import com.money.manager.ex.utils.DateTimeUtils;
import com.money.manager.ex.utils.DateUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import hirondelle.date4j.DateTime;

/**
 * Click listener
 * Created by Alen on 12/07/2015.
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
        Calendar date = Calendar.getInstance();
        if (!TextUtils.isEmpty(mTextView.getText())) {
            date.setTime(DateUtils.getDateFromUserString(mParent.getApplicationContext(), mTextView.getText().toString()));
        }
        DatePickerDialog dialog = DatePickerDialog.newInstance(mDateSetListener,
                date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DATE));
        dialog.setCloseOnSingleTapDay(true);
        dialog.show(mParent.getSupportFragmentManager(), DATEPICKER_TAG);
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePickerDialog view, int year, int monthOfYear, int dayOfMonth) {
            try {
                String dateString = FormatUtilities.getIsoDateFrom(year, monthOfYear + 1, dayOfMonth);

                // Save the actual value as tag.
                mTextView.setTag(dateString);

//                Date date = new SimpleDateFormat(Constants.PATTERN_DB_DATE)
//                    .parse(dateString);
                // display the selected value in user-formatted pattern
//                mTextView.setText(DateUtils.getUserStringFromDate(mParent.getApplicationContext(), date));

                DateTime date = new DateTime(dateString);
                mTextView.setText(DateTimeUtils.getUserStringFromDateTime(mParent.getApplicationContext(), date));
            } catch (Exception e) {
                ExceptionHandler handler = new ExceptionHandler(mParent, this);
                handler.handle(e, "date selected in search");
            }

        }
    };
}

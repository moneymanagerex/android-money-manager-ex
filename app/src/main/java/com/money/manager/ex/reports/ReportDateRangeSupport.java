/*
 * Copyright (C) 2012-2026 The Android Money Manager Ex Project Team
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
package com.money.manager.ex.reports;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;

import com.money.manager.ex.core.DateRange;
import com.money.manager.ex.core.InfoKeys;
import com.money.manager.ex.R;
import com.money.manager.ex.servicelayer.InfoService;
import com.money.manager.ex.utils.MmxDate;
import com.money.manager.ex.utils.MmxDateTimeUtils;

import java.util.Date;
import java.util.Locale;

/**
 * Shared date-range picker behavior for report screens.
 */
public final class ReportDateRangeSupport {

    private ReportDateRangeSupport() {
    }

    public interface OnDateRangeSelectedListener {
        void onDateRangeSelected(Date fromDate, Date toDate);
    }

    public static DateRange resolveDateRange(Context context, int itemId) {
        if (itemId == R.id.menu_current_month) {
            return resolveCurrentMonthRange();
        } else if (itemId == R.id.menu_last_month) {
            return resolveLastMonthRange();
        } else if (itemId == R.id.menu_last_30_days) {
            return resolveLast30DaysRange();
        } else if (itemId == R.id.menu_current_year) {
            return resolveCurrentYearRange();
        } else if (itemId == R.id.menu_last_year) {
            return resolveLastYearRange();
        } else if (itemId == R.id.menu_current_fin_year) {
            return resolveFinancialYearRange(context, false);
        } else if (itemId == R.id.menu_last_fin_year) {
            return resolveFinancialYearRange(context, true);
        } else {
            return null;
        }
    }

    private static DateRange resolveCurrentMonthRange() {
        MmxDate dateTime = MmxDate.newDate();
        return new DateRange(dateTime.firstDayOfMonth().toDate(), dateTime.lastDayOfMonth().toDate());
    }

    private static DateRange resolveLastMonthRange() {
        MmxDate lastMonth = MmxDate.newDate().minusMonths(1);
        return new DateRange(lastMonth.firstDayOfMonth().toDate(), lastMonth.lastDayOfMonth().toDate());
    }

    private static DateRange resolveLast30DaysRange() {
        Date fromDate = MmxDate.newDate().minusDays(30).toDate();
        Date toDate = MmxDate.newDate().toDate();
        return new DateRange(fromDate, toDate);
    }

    private static DateRange resolveCurrentYearRange() {
        MmxDate dateTime = MmxDate.newDate();
        return new DateRange(dateTime.firstMonthOfYear().firstDayOfMonth().toDate(),
                dateTime.lastMonthOfYear().lastDayOfMonth().toDate());
    }

    private static DateRange resolveLastYearRange() {
        MmxDate lastYear = MmxDate.newDate().minusYears(1);
        return new DateRange(lastYear.firstMonthOfYear().firstDayOfMonth().toDate(),
                lastYear.lastMonthOfYear().lastDayOfMonth().toDate());
    }

    private static DateRange resolveFinancialYearRange(Context context, boolean previousFinancialYear) {
        InfoService infoService = new InfoService(context);
        int financialYearStartDay = Integer.parseInt(infoService.getInfoValue(InfoKeys.FINANCIAL_YEAR_START_DAY, "1"));
        int financialYearStartMonth = Integer.parseInt(infoService.getInfoValue(InfoKeys.FINANCIAL_YEAR_START_MONTH, "1")) - 1;
        if (financialYearStartMonth < 0) {
            financialYearStartMonth = 0;
        }

        MmxDate fiscalStart = MmxDate.newDate();
        MmxDate today = MmxDate.newDate();
        fiscalStart.setDate(financialYearStartDay);
        fiscalStart.setMonth(financialYearStartMonth);

        if (fiscalStart.toDate().after(today.toDate())) {
            fiscalStart.minusYears(1);
        }
        if (previousFinancialYear) {
            fiscalStart.minusYears(1);
        }

        return new DateRange(fiscalStart.toDate(), fiscalStart.addYear(1).minusDays(1).toDate());
    }

    public static String buildWhereClause(Date fromDate, Date toDate, String dateColumn) {
        if (fromDate == null || toDate == null) {
            return null;
        }

        return dateColumn + " >= '" + new MmxDate(fromDate).toIsoDateString()
                + "' AND " + dateColumn + " <= '" + new MmxDate(toDate).toIsoDateString() + "'";
    }

    public static void showCustomDateDialog(Context context, Date fromDate, Date toDate,
            OnDateRangeSelectedListener listener) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_choose_date_report, null);
        DatePicker fromDatePicker = dialogView.findViewById(R.id.datePickerFromDate);
        DatePicker toDatePicker = dialogView.findViewById(R.id.datePickerToDate);

        Date startDate = fromDate != null ? fromDate : new MmxDate().today().toDate();
        Date endDate = toDate != null ? toDate : new MmxDate().today().toDate();

        MmxDateTimeUtils dateTimeUtils = new MmxDateTimeUtils(Locale.getDefault());
        dateTimeUtils.setDatePicker(startDate, fromDatePicker);
        dateTimeUtils.setDatePicker(endDate, toDatePicker);

        new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, (dialog, which) ->
                        listener.onDateRangeSelected(dateTimeUtils.from(fromDatePicker), dateTimeUtils.from(toDatePicker)))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
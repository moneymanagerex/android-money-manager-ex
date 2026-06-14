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

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.money.manager.ex.R;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.SQLDataSet;
import com.money.manager.ex.domainmodel.Asset;
import com.money.manager.ex.utils.MmxDate;

import java.util.ArrayList;
import java.util.List;

import info.javaperformance.money.MoneyFactory;
import timber.log.Timber;

public class SummaryOfAssetsReportFragment extends Fragment {

    private TableLayout tableLayout;
    private TextView emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_summary_of_accounts_report, container, false);

        tableLayout = view.findViewById(R.id.summaryAccountsTable);
        emptyView = view.findViewById(R.id.summaryAccountsEmpty);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        loadReportAsync();
    }

    private void loadReportAsync() {
        new Thread(() -> {
            try {
                List<AssetRow> rows = buildModel();
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> renderModel(rows));
            } catch (Exception e) {
                Timber.e(e, "loading summary of assets report");
                if (!isAdded()) {
                    return;
                }
                requireActivity().runOnUiThread(() -> {
                    emptyView.setVisibility(View.VISIBLE);
                    tableLayout.setVisibility(View.GONE);
                });
            }
        }).start();
    }

    private List<AssetRow> buildModel() {
        List<AssetRow> rows = new ArrayList<>();
        // Fetch all columns needed for display and current value calculation
        String query = "SELECT " + Asset.ASSETNAME + ", " +
                Asset.ASSETTYPE + ", " +
                Asset.VALUE + ", " +
                Asset.STARTDATE + ", " +
                Asset.VALUECHANGEMODE + ", " +
                Asset.VALUECHANGE + ", " +
                Asset.VALUECHANGERATE +
                " FROM assets_v1 ORDER BY " + Asset.ASSETNAME;

        Cursor cursor = requireContext().getContentResolver().query(new SQLDataSet().getUri(), null,
                query, null, null);

        if (cursor != null) {
            try {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(Asset.ASSETNAME));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(Asset.ASSETTYPE));
                    double initialValue = cursor.getDouble(cursor.getColumnIndexOrThrow(Asset.VALUE));
                    String startDate = cursor.getString(cursor.getColumnIndexOrThrow(Asset.STARTDATE));
                    String mode = cursor.getString(cursor.getColumnIndexOrThrow(Asset.VALUECHANGEMODE));
                    String change = cursor.getString(cursor.getColumnIndexOrThrow(Asset.VALUECHANGE));
                    double rate = cursor.getDouble(cursor.getColumnIndexOrThrow(Asset.VALUECHANGERATE));

                    double currentValue = calculateCurrentAssetValue(initialValue, mode, change, rate, startDate);

                    rows.add(new AssetRow(name, type, currentValue, initialValue, rate));
                }
            } finally {
                cursor.close();
            }
        }
        return rows;
    }

    private double calculateCurrentAssetValue(double initialValue, String mode, String change, double rate, String startDateIso) {
        if (TextUtils.isEmpty(change) || "None".equalsIgnoreCase(change) || rate == 0) {
            return initialValue;
        }

        try {
            MmxDate start = new MmxDate(startDateIso);
            MmxDate now = new MmxDate();

            // Difference in years
            double years = (now.getMillis() - start.getMillis()) / (1000.0 * 60 * 60 * 24 * 365.25);
            if (years <= 0) return initialValue;

            boolean appreciates = "Appreciates".equalsIgnoreCase(change);

            if ("Percentage".equalsIgnoreCase(mode)) {
                double r = rate / 100.0;
                if (appreciates) {
                    return initialValue * Math.pow(1 + r, years);
                } else {
                    // Depreciates
                    return initialValue * Math.pow(1 - r, years);
                }
            } else {
                // Linear
                if (appreciates) {
                    return initialValue + (rate * years);
                } else {
                    return initialValue - (rate * years);
                }
            }
        } catch (Exception e) {
            Timber.e(e, "calculating current asset value");
            return initialValue;
        }
    }

    private void renderModel(List<AssetRow> rows) {
        tableLayout.removeAllViews();

        if (rows.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            tableLayout.setVisibility(View.GONE);
            return;
        }

        emptyView.setVisibility(View.GONE);
        tableLayout.setVisibility(View.VISIBLE);

        tableLayout.addView(createHeaderRow());

        CurrencyService currencyService = new CurrencyService(requireContext());
        double grandTotalInitial = 0;
        double grandTotalCurrent = 0;

        for (int i = 0; i < rows.size(); i++) {
            AssetRow row = rows.get(i);
            TableRow tableRow = new TableRow(requireContext());

            tableRow.addView(createCell(row.name, true, true));
            tableRow.addView(createCell(row.type, true, false));
            tableRow.addView(createCell(currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(row.initialValue)), false, false));
            tableRow.addView(createCell(currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(row.currentValue)), false, false));
            tableRow.addView(createCell(String.format("%.2f%%", row.rate), false, false));

            // Alternative row color
            if (i % 2 != 0) {
                applyRowBackground(tableRow, Color.parseColor("#e4f0e5"));
            }

            tableLayout.addView(tableRow);

            grandTotalInitial += row.initialValue;
            grandTotalCurrent += row.currentValue;
        }

        // Grand Total row
        TableRow totalRow = new TableRow(requireContext());
        totalRow.addView(createHeaderCell(getString(R.string.total), true, true));
        totalRow.addView(createCell("", true, false));
        totalRow.addView(createHeaderCell(currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(grandTotalInitial)), false, false));
        totalRow.addView(createHeaderCell(currencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(grandTotalCurrent)), false, false));
        totalRow.addView(createCell("", false, false));

        // Separate color for total row
        applyRowBackground(totalRow, Color.parseColor("#D0D0D0"));

        tableLayout.addView(totalRow);
    }

    private void applyRowBackground(TableRow row, int color) {
        row.setBackgroundColor(color);
        for (int i = 0; i < row.getChildCount(); i++) {
            row.getChildAt(i).setBackgroundColor(color);
        }
    }

    private TableRow createHeaderRow() {
        TableRow row = new TableRow(requireContext());
        row.addView(createHeaderCell(getString(R.string.asset_name), true, true));
        row.addView(createHeaderCell(getString(R.string.asset_type), true, false));
        row.addView(createHeaderCell(getString(R.string.asset_current_value), false, false));
        row.addView(createHeaderCell(getString(R.string.asset_initial_value), false, false));
        row.addView(createHeaderCell(getString(R.string.asset_rate_percent), false, false));

        //set colort for header
        applyRowBackground(row, Color.parseColor("#7c8f7e"));

        return row;
    }

    private TextView createHeaderCell(String text, boolean alignStart, boolean isFirstColumn) {
        TextView cell = createCell(text, alignStart, isFirstColumn);
        cell.setTypeface(null, android.graphics.Typeface.BOLD);
        return cell;
    }

    private TextView createCell(String text, boolean alignStart, boolean isFirstColumn) {
        TextView cell = new TextView(requireContext());
        cell.setText(text);
        cell.setPadding(16, 8, 16, 8);
        cell.setGravity(alignStart ? Gravity.START : Gravity.END);

        if (isFirstColumn) {
            cell.setSingleLine(true);
            cell.setEllipsize(TextUtils.TruncateAt.END);
            // Limit first column width (approx 150dp) to prevent it from pushing other columns off screen
            int maxWidthPx = (int) (200 * getResources().getDisplayMetrics().density);
            cell.setMaxWidth(maxWidthPx);
        }

        return cell;
    }

    private static class AssetRow {
        final String name;
        final String type;
        final double initialValue;
        final double currentValue;
        final double rate;

        AssetRow(String name, String type, double initialValue, double currentValue, double rate) {
            this.name = name;
            this.type = type;
            this.initialValue = initialValue;
            this.currentValue = currentValue;
            this.rate = rate;
        }
    }
}

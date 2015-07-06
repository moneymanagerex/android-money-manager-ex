package com.money.manager.ex.reports;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.core.Core;
import com.money.manager.ex.currency.CurrencyUtils;
import com.money.manager.ex.database.ViewMobileData;

/**
 * Adapter for the Categories report.
 * Created by Alen Siljak on 06/07/2015.
 */
public class CategoriesReportAdapter extends CursorAdapter {
    private LayoutInflater mInflater;

    @SuppressWarnings("deprecation")
    public CategoriesReportAdapter(Context context, Cursor c) {
        super(context, c);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView txtColumn1 = (TextView) view.findViewById(R.id.textViewColumn1);
        TextView txtColumn2 = (TextView) view.findViewById(R.id.textViewColumn2);
        Core core = new Core(context);
        double total = cursor.getDouble(cursor.getColumnIndex("TOTAL"));
        String column1;
        if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(ViewMobileData.Category)))) {
            column1 = "<b>" + cursor.getString(cursor.getColumnIndex(ViewMobileData.Category)) + "</b>";
            if (!TextUtils.isEmpty(cursor.getString(cursor.getColumnIndex(ViewMobileData.Subcategory)))) {
                column1 += " : " + cursor.getString(cursor.getColumnIndex(ViewMobileData.Subcategory));
            }
        } else {
            column1 = "<i>" + context.getString(R.string.empty_category);
        }
        txtColumn1.setText(Html.fromHtml(column1));

        CurrencyUtils currencyUtils = new CurrencyUtils(mContext);

        txtColumn2.setText(currencyUtils.getCurrencyFormatted(currencyUtils.getBaseCurrencyId(), total));
        if (total < 0) {
            txtColumn2.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_red_color_theme)));
        } else {
            txtColumn2.setTextColor(context.getResources().getColor(core.resolveIdAttribute(R.attr.holo_green_color_theme)));
        }

        //view.setBackgroundColor(core.resolveColorAttribute(cursor.getPosition() % 2 == 1 ? R.attr.row_dark_theme : R.attr.row_light_theme));
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup root) {
        return mInflater.inflate(R.layout.item_generic_report_2_columns, root, false);
    }
}

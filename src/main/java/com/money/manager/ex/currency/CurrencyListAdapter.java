package com.money.manager.ex.currency;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.CursorAdapter;
import android.widget.TextView;

import com.money.manager.ex.R;
import com.money.manager.ex.database.TableCurrencyFormats;

/**
 * Adapter for the list of currencies
 * Created by Alen Siljak on 13/07/2015.
 */
public class CurrencyListAdapter
    extends CursorAdapter {

    public CurrencyListAdapter(Context context, Cursor cursor) {
        super(context, cursor, -1);

        mContext = context;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View view = inflater.inflate(R.layout.item_currency, parent, false);

        // holder
        CurrencyListViewHolder holder = new CurrencyListViewHolder();

        holder.nameTextView = (TextView) view.findViewById(R.id.nameTextView);
        holder.rateTextView = (TextView) view.findViewById(R.id.rateTextView);

        // set holder to view
        view.setTag(holder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        CurrencyListViewHolder holder = (CurrencyListViewHolder) view.getTag();

        // name
        String name = cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.CURRENCYNAME));
        holder.nameTextView.setText(name);

        // price
        String rate = cursor.getString(cursor.getColumnIndex(TableCurrencyFormats.BASECONVRATE));
        holder.rateTextView.setText(rate);
    }
}

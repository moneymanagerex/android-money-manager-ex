package com.money.manager.ex.reports;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.List;

public class GeneralReportArrayAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> items;
    public GeneralReportArrayAdapter(Context context, List<String> items) {
        super(context, android.R.layout.simple_list_item_1, items); // Using default simple item layout
        this.context = context;
        this.items = items;
    }

    // You can override getView if you want to customize the item layout further
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        // Customize view if needed (e.g., add icons, change text color, etc.)
        return view;
    }
}

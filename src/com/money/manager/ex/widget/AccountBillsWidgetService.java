/*
 * Copyright (C) 2012-2015 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.money.manager.ex.widget;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.money.manager.ex.R;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.utils.CurrencyUtils;

public class AccountBillsWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new AllAccountBillsViewFactory(this.getApplicationContext(), intent);
    }

    public class AllAccountBillsViewFactory implements RemoteViewsService.RemoteViewsFactory {
        private Context mContext;
        private CurrencyUtils mCurrencyUtils;
        private Cursor mCursor;

        public AllAccountBillsViewFactory(Context context, Intent intent) {
            //appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            this.mContext = context;
            // create application
            mCurrencyUtils = new CurrencyUtils(context);
        }

        @Override
        public int getCount() {
            if (mCursor != null) {
                return mCursor.getCount();
            } else {
                return 0;
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public RemoteViews getViewAt(int position) {
            RemoteViews remoteViews = new RemoteViews(mContext.getPackageName(), R.layout.widget_item_account_bills);
            if (mCursor.moveToPosition(position)) {
                int colindex = mCursor.getColumnIndex(QueryAccountBills.ACCOUNTNAME);
                String accountname = mCursor.getString(colindex);
                remoteViews.setTextViewText(R.id.textViewItemAccountName, accountname);
                String value = mCurrencyUtils.getCurrencyFormatted(mCursor.getInt(mCursor.getColumnIndex(QueryAccountBills.CURRENCYID)),
                        mCursor.getDouble(mCursor.getColumnIndex(QueryAccountBills.TOTAL)));
                remoteViews.setTextViewText(R.id.textViewItemAccountTotal, value);
            }
            return remoteViews;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public void onCreate() {
            return;
        }

        @Override
        public void onDataSetChanged() {
            if (mCursor != null) {
                mCursor.close();
            }
            QueryAccountBills accountBills = new QueryAccountBills(mContext);
            String selection = accountBills.getFilterAccountSelection();
            // create a cursor
            mCursor = mContext.getContentResolver().query(accountBills.getUri(), null, selection, null, QueryAccountBills.ACCOUNTNAME);
        }

        @Override
        public void onDestroy() {
            if (mCursor != null) {
                mCursor.close();
            }
            return;
        }
    }
}

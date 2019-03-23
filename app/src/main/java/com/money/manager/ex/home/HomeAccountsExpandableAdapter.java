/*
 * Copyright (C) 2012-2019 The Android Money Manager Ex Project Team
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

package com.money.manager.ex.home;

import android.content.Context;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.mmex_icon_font_typeface_library.MMXIconFont;
import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryAccountBills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import info.javaperformance.money.MoneyFactory;

/**
 * Adapter for the Home screen expandable accounts list.
 */
public class HomeAccountsExpandableAdapter
    extends BaseExpandableListAdapter {

    private Context mContext;

    public HomeAccountsExpandableAdapter(Context context, List<String> accountTypes,
                                         HashMap<String, List<QueryAccountBills>> accountsByType,
                                         HashMap<String, QueryAccountBills> totalsByType,
                                         boolean hideReconciled) {
        mContext = context;
        mAccountTypes = accountTypes;
        mAccountsByType = accountsByType;
        mTotalsByType = totalsByType;
        mHideReconciled = hideReconciled;
        mCurrencyService = new CurrencyService(mContext);
    }

    private List<String> mAccountTypes = new ArrayList<>();
    private HashMap<String, List<QueryAccountBills>> mAccountsByType = new HashMap<>();
    private HashMap<String, QueryAccountBills> mTotalsByType = new HashMap<>();
    private boolean mHideReconciled;
    private CurrencyService mCurrencyService;

    @Override
    public int getGroupCount() {
        return mAccountTypes.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mAccountsByType.get(mAccountTypes.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mAccountTypes.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String accountType = mAccountTypes.get(groupPosition);
        List<QueryAccountBills> group = mAccountsByType.get(accountType);
        return group.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    /**
     * Creates a view for the group header row.
     */
    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        ViewHolderAccountBills holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_account_bills, null);

            holder = new ViewHolderAccountBills();

            holder.txtAccountName = (TextView) convertView.findViewById(R.id.textViewItemAccountName);
            holder.txtAccountName.setTypeface(null, Typeface.BOLD);

            holder.txtAccountTotal = (TextView) convertView.findViewById(R.id.textViewItemAccountTotal);
            holder.txtAccountTotal.setTypeface(null, Typeface.BOLD);

            holder.txtAccountReconciled = (TextView) convertView.findViewById(R.id.textViewItemAccountTotalReconciled);
            if(mHideReconciled) {
                holder.txtAccountReconciled.setVisibility(View.GONE);
            } else {
                holder.txtAccountReconciled.setTypeface(null, Typeface.BOLD);
            }

            holder.imgAccountType = (ImageView) convertView.findViewById(R.id.imageViewAccountType);

            convertView.setTag(holder);
        }
        holder = (ViewHolderAccountBills) convertView.getTag();

        // Show Totals
        String accountType = mAccountTypes.get(groupPosition);
        QueryAccountBills total = mTotalsByType.get(accountType);
        if (total != null) {
            // set account type value
            String totalDisplay = mCurrencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(total.getTotalBaseConvRate()));
            holder.txtAccountTotal.setText(totalDisplay);
            if(!mHideReconciled) {
                String reconciledDisplay = mCurrencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(total.getReconciledBaseConvRate()));
                holder.txtAccountReconciled.setText(reconciledDisplay);
            }
            // set account name
            holder.txtAccountName.setText(total.getAccountName());
        }
        // set image depending on the account type
        if (!TextUtils.isEmpty(accountType)) {
            UIHelper uiHelper = new UIHelper(getContext());
            int iconSize = 30;
            int iconColor = uiHelper.getSecondaryTextColor();

            if(AccountTypes.CASH.toString().equalsIgnoreCase(accountType)) {
                IconicsDrawable icon = uiHelper.getIcon(MMXIconFont.Icon.mmx_money_banknote)
                        .sizeDp(iconSize).color(iconColor);
                holder.imgAccountType.setImageDrawable(icon);
            } else if(AccountTypes.CHECKING.toString().equalsIgnoreCase(accountType)){
                IconicsDrawable icon = uiHelper.getIcon(MMXIconFont.Icon.mmx_temple)
                        .sizeDp(iconSize).color(iconColor);
                holder.imgAccountType.setImageDrawable(icon);
            } else if (AccountTypes.TERM.toString().equalsIgnoreCase(accountType)) {
                IconicsDrawable icon = uiHelper.getIcon(MMXIconFont.Icon.mmx_calendar)
                        .sizeDp(iconSize).color(iconColor);
                holder.imgAccountType.setImageDrawable(icon);
            } else if (AccountTypes.CREDIT_CARD.toString().equalsIgnoreCase(accountType)) {
                IconicsDrawable icon = uiHelper.getIcon(MMXIconFont.Icon.mmx_credit_card)
                        .sizeDp(iconSize).color(iconColor);
                holder.imgAccountType.setImageDrawable(icon);
            } else if (AccountTypes.INVESTMENT.toString().equalsIgnoreCase(accountType)) {
                IconicsDrawable icon = uiHelper.getIcon(MMXIconFont.Icon.mmx_briefcase)
                        .sizeDp(iconSize).color(iconColor);
                holder.imgAccountType.setImageDrawable(icon);
            } else if (AccountTypes.LOAN.toString().equalsIgnoreCase(accountType)) {
                IconicsDrawable icon = uiHelper.getIcon(MMXIconFont.Icon.mmx_back_in_time)
                        .sizeDp(iconSize).color(iconColor);
                holder.imgAccountType.setImageDrawable(icon);
            } else if (AccountTypes.SHARES.toString().equalsIgnoreCase(accountType)) {
                IconicsDrawable icon = uiHelper.getIcon(MMXIconFont.Icon.mmx_chart_pie)
                        .sizeDp(iconSize).color(iconColor);
                holder.imgAccountType.setImageDrawable(icon);
            }
        }

        return convertView;
    }

    /**
     * Creates a view for the group item row.
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        ViewHolderAccountBills holder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_account_bills, null);

            holder = new ViewHolderAccountBills();
            holder.txtAccountName = (TextView) convertView.findViewById(R.id.textViewItemAccountName);
            holder.txtAccountTotal = (TextView) convertView.findViewById(R.id.textViewItemAccountTotal);
            holder.txtAccountReconciled = (TextView) convertView.findViewById(R.id.textViewItemAccountTotalReconciled);
            holder.imgAccountType = (ImageView) convertView.findViewById(R.id.imageViewAccountType);

            holder.txtAccountTotal.setTypeface(null, Typeface.NORMAL);
            holder.imgAccountType.setVisibility(View.INVISIBLE);

            convertView.setTag(holder);
        }
        holder = (ViewHolderAccountBills) convertView.getTag();

        QueryAccountBills account = getAccountData(groupPosition, childPosition);

        // set account name
        holder.txtAccountName.setText(account.getAccountName());
        // import formatted
        String value = mCurrencyService.getCurrencyFormatted(account.getCurrencyId(), MoneyFactory.fromDouble(account.getTotal()));
        // set amount value
        holder.txtAccountTotal.setText(value);

        // reconciled
        if(mHideReconciled) {
            holder.txtAccountReconciled.setVisibility(View.GONE);
        } else {
            value = mCurrencyService.getCurrencyFormatted(account.getCurrencyId(), MoneyFactory.fromDouble(account.getReconciled()));
            holder.txtAccountReconciled.setText(value);
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public QueryAccountBills getAccountData(int groupPosition, int childPosition) {
        String accountType = mAccountTypes.get(groupPosition);
        QueryAccountBills account = mAccountsByType.get(accountType).get(childPosition);

        return account;
    }

    public Context getContext() {
        return mContext;
    }

    private class ViewHolderAccountBills {
        TextView txtAccountName;
        TextView txtAccountTotal;
        TextView txtAccountReconciled;
        ImageView imgAccountType;
    }
}

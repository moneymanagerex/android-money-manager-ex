/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
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

import androidx.core.content.ContextCompat;

import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.mmex_icon_font_typeface_library.MMXIconFont;
import com.money.manager.ex.R;
import com.money.manager.ex.account.AccountTypes;
import com.money.manager.ex.core.UIHelper;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryAccountBills;
import com.money.manager.ex.datalayer.StockRepository;
import com.money.manager.ex.domainmodel.Stock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Adapter for the Home screen expandable accounts list.
 */
public class HomeAccountsExpandableAdapter
    extends BaseExpandableListAdapter {

    private final Context mContext;
    private List<String> mAccountTypes = new ArrayList<>();
    private HashMap<String, List<QueryAccountBills>> mAccountsByType = new HashMap<>();
    private HashMap<String, QueryAccountBills> mTotalsByType = new HashMap<>();
    private final boolean mHideReconciled;
    private final CurrencyService mCurrencyService;
    private final HashMap<Long, InvestmentSummary> mInvestmentSummaries = new HashMap<>();

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
        String accountType = mAccountTypes.get(groupPosition);
        boolean investmentGroup = AccountTypes.INVESTMENT.toString().equalsIgnoreCase(accountType);
        View rowView = convertView;
        ViewHolderAccountBills holder;
        if (rowView == null || investmentGroup != isInvestmentChildView(rowView)) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            holder = new ViewHolderAccountBills();
            if (investmentGroup) {
                rowView = inflater.inflate(R.layout.item_account_bills_investment, null);
                holder.txtAccountName = rowView.findViewById(R.id.textViewItemAccountName);
                holder.txtAccountName.setTypeface(null, Typeface.BOLD);
                holder.imgAccountType = rowView.findViewById(R.id.imageViewAccountType);
                holder.txtCashBalance = rowView.findViewById(R.id.textViewCashBalance);
                holder.txtReconciled = rowView.findViewById(R.id.textViewReconciled);
                holder.txtMarketValue = rowView.findViewById(R.id.textViewMarketValue);
                holder.txtInvested = rowView.findViewById(R.id.textViewInvested);
                holder.txtGainLoss = rowView.findViewById(R.id.textViewGainLoss);
            } else {
                rowView = inflater.inflate(R.layout.item_account_bills, null);
                holder.txtAccountName = rowView.findViewById(R.id.textViewItemAccountName);
                holder.txtAccountName.setTypeface(null, Typeface.BOLD);
                holder.imgAccountType = rowView.findViewById(R.id.imageViewAccountType);
                holder.txtAccountTotal = rowView.findViewById(R.id.textViewItemAccountTotal);
                holder.txtAccountTotal.setTypeface(null, Typeface.BOLD);
                holder.txtAccountReconciled = rowView.findViewById(R.id.textViewItemAccountTotalReconciled);
                if (mHideReconciled) {
                    holder.txtAccountReconciled.setVisibility(View.GONE);
                } else {
                    holder.txtAccountReconciled.setTypeface(null, Typeface.BOLD);
                }
            }
            holder.isInvestmentLayout = investmentGroup;
            rowView.setTag(holder);
        }
        holder = (ViewHolderAccountBills) rowView.getTag();

        UIHelper uiHelper = new UIHelper(getContext());
        int iconSize = 30;
        int iconColor = uiHelper.getSecondaryTextColor();

        if (investmentGroup) {
            InvestmentSummary summary = getInvestmentGroupSummary(accountType);
            QueryAccountBills total = mTotalsByType.get(accountType);
            if (total != null) {
                holder.txtAccountName.setText(total.getAccountName());
            }
            holder.imgAccountType.setVisibility(View.VISIBLE);
            holder.imgAccountType.setImageDrawable(
                    uiHelper.getIcon(MMXIconFont.Icon.mmx_briefcase).sizeDp(iconSize).color(iconColor));
            holder.txtCashBalance.setTypeface(null, Typeface.BOLD);
            holder.txtCashBalance.setText(mCurrencyService.getBaseCurrencyFormatted(summary.cashBalance));
            if (holder.txtReconciled != null) {
                if (mHideReconciled) {
                    holder.txtReconciled.setVisibility(View.GONE);
                } else {
                    holder.txtReconciled.setVisibility(View.VISIBLE);
                    holder.txtReconciled.setTypeface(null, Typeface.BOLD);
                    holder.txtReconciled.setText(mCurrencyService.getBaseCurrencyFormatted(summary.reconciledCash));
                }
            }
            holder.txtMarketValue.setTypeface(null, Typeface.BOLD);
            holder.txtMarketValue.setText(mCurrencyService.getBaseCurrencyFormatted(summary.marketValue));
            holder.txtInvested.setTypeface(null, Typeface.BOLD);
            holder.txtInvested.setText(mCurrencyService.getBaseCurrencyFormatted(summary.invested));
            holder.txtGainLoss.setTypeface(null, Typeface.BOLD);
            holder.txtGainLoss.setText(formatGainLoss(summary.gainLoss, summary.invested));
            int gainLossColor = summary.gainLoss.toDouble() < 0
                    ? ContextCompat.getColor(mContext, R.color.red)
                    : ContextCompat.getColor(mContext, R.color.green);
            holder.txtGainLoss.setTextColor(gainLossColor);
        } else {
            QueryAccountBills total = mTotalsByType.get(accountType);
            if (total != null) {
                String totalDisplay = mCurrencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(total.getTotalBaseConvRate()));
                holder.txtAccountTotal.setText(totalDisplay);
                if (!mHideReconciled) {
                    String reconciledDisplay = mCurrencyService.getBaseCurrencyFormatted(MoneyFactory.fromDouble(total.getReconciledBaseConvRate()));
                    holder.txtAccountReconciled.setText(reconciledDisplay);
                }
                holder.txtAccountName.setText(total.getAccountName());
            }
            if (!TextUtils.isEmpty(accountType)) {
                if (AccountTypes.CASH.toString().equalsIgnoreCase(accountType)) {
                    holder.imgAccountType.setImageDrawable(
                            uiHelper.getIcon(MMXIconFont.Icon.mmx_money_banknote).sizeDp(iconSize).color(iconColor));
                } else if (AccountTypes.CHECKING.toString().equalsIgnoreCase(accountType)) {
                    holder.imgAccountType.setImageDrawable(
                            uiHelper.getIcon(MMXIconFont.Icon.mmx_temple).sizeDp(iconSize).color(iconColor));
                } else if (AccountTypes.TERM.toString().equalsIgnoreCase(accountType)) {
                    holder.imgAccountType.setImageDrawable(
                            uiHelper.getIcon(MMXIconFont.Icon.mmx_calendar).sizeDp(iconSize).color(iconColor));
                } else if (AccountTypes.CREDIT_CARD.toString().equalsIgnoreCase(accountType)) {
                    holder.imgAccountType.setImageDrawable(
                            uiHelper.getIcon(MMXIconFont.Icon.mmx_credit_card).sizeDp(iconSize).color(iconColor));
                } else if (AccountTypes.LOAN.toString().equalsIgnoreCase(accountType)) {
                    holder.imgAccountType.setImageDrawable(
                            uiHelper.getIcon(MMXIconFont.Icon.mmx_back_in_time).sizeDp(iconSize).color(iconColor));
                } else if (AccountTypes.SHARES.toString().equalsIgnoreCase(accountType)) {
                    holder.imgAccountType.setImageDrawable(
                            uiHelper.getIcon(MMXIconFont.Icon.mmx_chart_pie).sizeDp(iconSize).color(iconColor));
                }
            }
        }

        return rowView;
    }

    private InvestmentSummary getInvestmentGroupSummary(String accountType) {
        InvestmentSummary total = new InvestmentSummary();
        List<QueryAccountBills> accounts = mAccountsByType.get(accountType);
        if (accounts == null) return total;
        for (QueryAccountBills account : accounts) {
            InvestmentSummary s = getInvestmentSummary(account);
            total.cashBalance = total.cashBalance.add(s.cashBalance);
            total.reconciledCash = total.reconciledCash.add(s.reconciledCash);
            total.marketValue = total.marketValue.add(s.marketValue);
            total.invested = total.invested.add(s.invested);
            total.gainLoss = total.gainLoss.add(s.gainLoss);
        }
        return total;
    }

    private InvestmentSummary getInvestmentSummary(QueryAccountBills account) {
        InvestmentSummary cached = mInvestmentSummaries.get(account.getAccountId());
        if (cached != null) {
            return cached;
        }

        InvestmentSummary summary = new InvestmentSummary();
        StockRepository stockRepository = new StockRepository(mContext);
        long baseCurrencyId = mCurrencyService.getBaseCurrencyId();

        Money marketValue = MoneyFactory.fromDouble(0);
        Money invested = MoneyFactory.fromDouble(0);

        List<Stock> stocks = stockRepository.loadByAccount(account.getAccountId());
        for (Stock stock : stocks) {
            marketValue = marketValue.add(stock.getCurrentPrice().multiply(stock.getNumberOfShares()));
            // invested uses cost basis (VALUE field) which includes commission
            invested = invested.add(stock.getValue());
        }

        summary.totalBase = MoneyFactory.fromDouble(account.getTotalBaseConvRate());
        summary.marketValue = mCurrencyService.doCurrencyExchange(
                baseCurrencyId, marketValue, account.getCurrencyId());
        summary.invested = mCurrencyService.doCurrencyExchange(
                baseCurrencyId, invested, account.getCurrencyId());
        summary.cashBalance = summary.totalBase.subtract(summary.marketValue);
        summary.reconciledCash = MoneyFactory.fromDouble(account.getReconciledBaseConvRate()).subtract(summary.marketValue);
        summary.gainLoss = summary.marketValue.subtract(summary.invested);
        mInvestmentSummaries.put(account.getAccountId(), summary);
        return summary;
    }

    private String formatGainLoss(Money gainLoss, Money invested) {
        String amount = mCurrencyService.getBaseCurrencyFormatted(gainLoss);
        double investedDouble = invested.toDouble();
        if (investedDouble == 0) {
            return amount;
        }
        double pct = (gainLoss.toDouble() / investedDouble) * 100.0;
        return String.format("%s (%.2f%%)", amount, pct);
    }

    /**
     * Creates a view for the group item row.
     */
    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        String accountType = mAccountTypes.get(groupPosition);
        boolean investmentAccount = AccountTypes.INVESTMENT.toString().equalsIgnoreCase(accountType);
        View rowView = convertView;
        ViewHolderAccountBills holder;
        if (rowView == null || investmentAccount != isInvestmentChildView(rowView)) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(
                    investmentAccount ? R.layout.item_account_bills_investment : R.layout.item_account_bills,
                    null);

            holder = new ViewHolderAccountBills();
            holder.txtAccountName = rowView.findViewById(R.id.textViewItemAccountName);
            holder.txtAccountName.setTypeface(null, Typeface.NORMAL);

            if (investmentAccount) {
                holder.imgAccountType = rowView.findViewById(R.id.imageViewAccountType);
                holder.imgAccountType.setVisibility(View.INVISIBLE);
                holder.txtCashBalance = rowView.findViewById(R.id.textViewCashBalance);
                holder.txtReconciled = rowView.findViewById(R.id.textViewReconciled);
                holder.txtMarketValue = rowView.findViewById(R.id.textViewMarketValue);
                holder.txtInvested = rowView.findViewById(R.id.textViewInvested);
                holder.txtGainLoss = rowView.findViewById(R.id.textViewGainLoss);
            } else {
                holder.imgAccountType = rowView.findViewById(R.id.imageViewAccountType);
                holder.txtAccountTotal = rowView.findViewById(R.id.textViewItemAccountTotal);
                holder.txtAccountReconciled = rowView.findViewById(R.id.textViewItemAccountTotalReconciled);
                holder.txtAccountTotal.setTypeface(null, Typeface.NORMAL);
                holder.imgAccountType.setVisibility(View.INVISIBLE);
            }

            holder.isInvestmentLayout = investmentAccount;

            rowView.setTag(holder);
        }
        holder = (ViewHolderAccountBills) rowView.getTag();

        QueryAccountBills account = getAccountData(groupPosition, childPosition);

        // set account name
        holder.txtAccountName.setText(account.getAccountName());
        if (investmentAccount) {
            InvestmentSummary summary = getInvestmentSummary(account);
            if (holder.txtAccountName != null) {
                holder.txtAccountName.setTypeface(null, Typeface.NORMAL);
            }
            if (holder.txtCashBalance != null) {
                holder.txtCashBalance.setTypeface(null, Typeface.NORMAL);
                holder.txtCashBalance.setText(mCurrencyService.getBaseCurrencyFormatted(summary.cashBalance));
            }
            if (holder.txtReconciled != null) {
                if (mHideReconciled) {
                    holder.txtReconciled.setVisibility(View.GONE);
                } else {
                    holder.txtReconciled.setVisibility(View.VISIBLE);
                    holder.txtReconciled.setText(mCurrencyService.getBaseCurrencyFormatted(summary.reconciledCash));
                }
            }
            if (holder.txtMarketValue != null) {
                holder.txtMarketValue.setTypeface(null, Typeface.NORMAL);
                holder.txtMarketValue.setText(mCurrencyService.getBaseCurrencyFormatted(summary.marketValue));
            }
            if (holder.txtInvested != null) {
                holder.txtInvested.setTypeface(null, Typeface.NORMAL);
                holder.txtInvested.setText(mCurrencyService.getBaseCurrencyFormatted(summary.invested));
            }
            if (holder.txtGainLoss != null) {
                holder.txtGainLoss.setTypeface(null, Typeface.NORMAL);
                holder.txtGainLoss.setText(formatGainLoss(summary.gainLoss, summary.invested));
                int gainLossColor = summary.gainLoss.toDouble() < 0
                        ? ContextCompat.getColor(mContext, R.color.red)
                        : ContextCompat.getColor(mContext, R.color.green);
                holder.txtGainLoss.setTextColor(gainLossColor);
            }
        } else {
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
        }

        return rowView;
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

    private boolean isInvestmentChildView(View convertView) {
        Object tag = convertView.getTag();
        return tag instanceof ViewHolderAccountBills && ((ViewHolderAccountBills) tag).isInvestmentLayout;
    }

    private class ViewHolderAccountBills {
        TextView txtAccountName;
        TextView txtAccountTotal;
        TextView txtAccountReconciled;
        TextView txtCashBalance;
        TextView txtReconciled;
        TextView txtMarketValue;
        TextView txtInvested;
        TextView txtGainLoss;
        ImageView imgAccountType;
        boolean isInvestmentLayout;
    }

    private static class InvestmentSummary {
        private Money totalBase = MoneyFactory.fromDouble(0);
        private Money cashBalance = MoneyFactory.fromDouble(0);
        private Money reconciledCash = MoneyFactory.fromDouble(0);
        private Money marketValue = MoneyFactory.fromDouble(0);
        private Money invested = MoneyFactory.fromDouble(0);
        private Money gainLoss = MoneyFactory.fromDouble(0);
    }
}

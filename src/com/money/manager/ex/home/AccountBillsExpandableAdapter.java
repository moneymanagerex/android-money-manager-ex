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

import com.money.manager.ex.R;
import com.money.manager.ex.core.AccountTypes;
import com.money.manager.ex.currency.CurrencyService;
import com.money.manager.ex.database.QueryAccountBills;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Adapter for the Home screen expandable accounts list.
 * Created by Alen Siljak on 15/07/2015.
 */
public class AccountBillsExpandableAdapter
        extends BaseExpandableListAdapter {

    private Context mContext;

    public AccountBillsExpandableAdapter(Context context, List<String> accountTypes,
                                         HashMap<String, List<QueryAccountBills>> accountsByType,
                                         HashMap<String, QueryAccountBills> totalsByType,
                                         boolean hideReconciled) {
        mContext = context;
        mAccountTypes = accountTypes;
        mAccountsByType = accountsByType;
        mTotalsByType = totalsByType;
        mHideReconciled = hideReconciled;
    }

    private List<String> mAccountTypes = new ArrayList<>();
    private HashMap<String, List<QueryAccountBills>> mAccountsByType = new HashMap<>();
    private HashMap<String, QueryAccountBills> mTotalsByType = new HashMap<>();
    private boolean mHideReconciled;

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
        return mAccountsByType.get(mAccountTypes.get(groupPosition)).get(childPosition);
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
     * @param groupPosition
     * @param isExpanded
     * @param convertView
     * @param parent
     * @return
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

        CurrencyService currencyService = new CurrencyService(mContext.getApplicationContext());
        // Show Totals
        String accountType = mAccountTypes.get(groupPosition);
        QueryAccountBills total = mTotalsByType.get(accountType);
        if (total != null) {
            // set account type value
            holder.txtAccountTotal.setText(currencyService.getBaseCurrencyFormatted(total.getTotalBaseConvRate()));
            if(!mHideReconciled) {
                holder.txtAccountReconciled.setText(currencyService.getBaseCurrencyFormatted(total.getReconciledBaseConvRate()));
            }
            // set account name
            holder.txtAccountName.setText(total.getAccountName());
        }
        // set image depending on the account type
        if (!TextUtils.isEmpty(accountType)) {
            if(AccountTypes.CHECKING.toString().equalsIgnoreCase(accountType)){
                holder.imgAccountType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_money_safe));
            } else if (AccountTypes.TERM.toString().equalsIgnoreCase(accountType)) {
                holder.imgAccountType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_money_finance));
            } else if (AccountTypes.CREDIT_CARD.toString().equalsIgnoreCase(accountType)) {
                holder.imgAccountType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.ic_credit_card));
            } else if (AccountTypes.INVESTMENT.toString().equalsIgnoreCase(accountType)) {
                holder.imgAccountType.setImageDrawable(mContext.getResources().getDrawable(R.drawable.portfolio));
            }
        }

        return convertView;
    }

    /**
     * Creates a view for the group item row.
     * @param groupPosition
     * @param childPosition
     * @param isLastChild
     * @param convertView
     * @param parent
     * @return
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
        CurrencyService currencyService = new CurrencyService(mContext.getApplicationContext());

        // set account name
        holder.txtAccountName.setText(account.getAccountName());
        // import formatted
        String value = currencyService.getCurrencyFormatted(account.getCurrencyId(), account.getTotal());
        // set amount value
        holder.txtAccountTotal.setText(value);

        // reconciled
        if(mHideReconciled) {
            holder.txtAccountReconciled.setVisibility(View.GONE);
        } else {
            value = currencyService.getCurrencyFormatted(account.getCurrencyId(), account.getReconciled());
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

    private class ViewHolderAccountBills {
        TextView txtAccountName;
        TextView txtAccountTotal;
        TextView txtAccountReconciled;
        ImageView imgAccountType;
    }
}

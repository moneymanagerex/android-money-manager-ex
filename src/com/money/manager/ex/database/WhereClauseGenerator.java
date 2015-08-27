package com.money.manager.ex.database;

import android.content.Context;
import android.text.TextUtils;

import com.money.manager.ex.R;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * Generates WHERE statements for various conditions
 * Created by Alen Siljak on 27/08/2015.
 */
public class WhereClauseGenerator {

    public WhereClauseGenerator(Context context) {
        mContext = context.getApplicationContext();
    }

    private Context mContext;

    /**
     * Generate a period selector for the given string.
     *
     * @param period A value from show_transaction_values array.
     * @return list of where statements
     */
    public ArrayList<String> getWhereClauseForPeriod(String period) {
        ArrayList<String> result = new ArrayList<>();

        if (period.equalsIgnoreCase(mContext.getString(R.string.last7days))) {
            result.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 7)");
        } else if (period.equalsIgnoreCase(mContext.getString(R.string.last15days))) {
            result.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 14)");
        } else if (period.equalsIgnoreCase(mContext.getString(R.string.current_month))) {
            result.add(QueryAllData.Month + "=" + Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1));
            result.add(QueryAllData.Year + "=" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
        } else if (period.equalsIgnoreCase(mContext.getString(R.string.last30days))) {
            result.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 30)");
        } else if (period.equalsIgnoreCase(mContext.getString(R.string.last3months))) {
            result.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 90)");
        } else if (period.equalsIgnoreCase(mContext.getString(R.string.last6months))) {
            result.add("(julianday(date('now')) - julianday(" + QueryAllData.Date + ") <= 180)");
        } else if (period.equalsIgnoreCase(mContext.getString(R.string.current_year))) {
            result.add(QueryAllData.Year + "=" + Integer.toString(Calendar.getInstance().get(Calendar.YEAR)));
        } else if (period.equalsIgnoreCase(mContext.getString(R.string.future_transactions))) {
            // Future transactions
            result.add("date(" + QueryAllData.Date + ") > date('now')");
        }

        return result;
    }

    public String getWhereStatementFromClauses(ArrayList<String> whereClause) {
        String whereStatement = "";

        if (whereClause != null) {
            for (int i = 0; i < whereClause.size(); i++) {
                whereStatement += (!TextUtils.isEmpty(whereStatement) ? " AND " : "") +
                        whereClause.get(i);
            }
        }

        return whereStatement;
    }
}

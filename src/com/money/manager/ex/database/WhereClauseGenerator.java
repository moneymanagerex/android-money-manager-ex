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
    private ArrayList<String> mSelections;
    private ArrayList<String> mArguments;

    public String getWhereClauseForPeriod(String period) {
        ArrayList<String> whereClauses = this.getWhereClausesForPeriod(period);

        String transactionsFilter = this.getWhereStatementFromClauses(whereClauses);

        return transactionsFilter;
    }

    /**
     * Generate a period selector for the given string.
     *
     * @param period A value from show_transaction_values array.
     * @return list of where statements
     */
    public ArrayList<String> getWhereClausesForPeriod(String period) {
        ArrayList<String> result = new ArrayList<>();

        if (period.equalsIgnoreCase(mContext.getString(R.string.all_transaction))) {
            // All transactions. No filter needed.
        } else if (period.equalsIgnoreCase(mContext.getString(R.string.today))) {
            result.add("(julianday(date('now')) = julianday(" + QueryAllData.Date + "))");
        } else if (period.equalsIgnoreCase(mContext.getString(R.string.last7days))) {
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
        if (whereClause == null) {
            return whereStatement;
        }

        // todo: do we need the WHERE statement here?
//        if (whereClause.size() > 0) {
//            whereStatement = " WHERE ";
//        }

        for (String statement : whereClause) {
            whereStatement += (!TextUtils.isEmpty(whereStatement) ? " AND " : "") + statement;
        }

//        for (int i = 0; i < whereClause.size(); i++) {
//            whereStatement += (!TextUtils.isEmpty(whereStatement) ? " AND " : "") +
//                    whereClause.get(i);
//        }

        return whereStatement;
    }

    /**
     * Adds the selection criteria to the collection.
     * @param selection i.e. AccountId=?
     * @param arguments List of arguments. i.e. 3
     */
//    public void addSelection(String selection, String... arguments) {
//        getSelections().add(selection);
//        for (String argument:arguments) {
//            getArguments().add(argument);
//        }
//    }

    public void addSelection(String selection, String operator, String... arguments) {
        getSelections().add(selection + operator + "?");
        for (String argument:arguments) {
            getArguments().add(argument);
        }
    }

    public void addSelection(String selection, String operator, Integer... arguments) {
        getSelections().add(selection + operator + "?");
        for (Integer argument:arguments) {
            getArguments().add(Integer.toString(argument));
        }
    }

    public String getSelectionStatements() {
        String result = getWhereStatementFromClauses(mSelections);
        return result;
    }

    public String[] getSelectionArguments() {
        String[] result = new String[getArguments().size()];
        return getArguments().toArray(result);
    }

    private ArrayList<String> getSelections() {
        if (mSelections == null) {
            mSelections = new ArrayList<>();
        }
        return mSelections;
    }

    private ArrayList<String> getArguments() {
        if (mArguments == null) {
            mArguments = new ArrayList<>();
        }
        return mArguments;
    }
}

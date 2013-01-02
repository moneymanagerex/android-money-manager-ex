package com.money.manager.ex.database;

import android.content.Context;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;

public class QueryReportIncomeVsExpenses extends Dataset {
	//field name
	public static final String Year = "Year";
	public static final String Month = "Month";
	public static final String Income = "Income";
	public static final String Expenses = "Expenses";
	public static final String Transfers = "Transfers";
	
	public QueryReportIncomeVsExpenses(Context context) {
		super(MoneyManagerApplication.getRawAsString(context, R.raw.report_income_vs_expenses), DatasetType.QUERY, "report_income_vs_expenses");
	}
	
	@Override
	public String[] getAllColumns() {
		return new String[] {"ROWID AS _id", Year, Month, Income, Expenses, Transfers};
	}
}

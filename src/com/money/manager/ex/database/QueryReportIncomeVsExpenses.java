package com.money.manager.ex.database;

import android.content.Context;

import com.money.manager.ex.MoneyManagerApplication;
import com.money.manager.ex.R;

public class QueryReportIncomeVsExpenses extends Dataset {
	//field name
	public static final String Month = "Month";
	public static final String Year = "Year";
	public static final String TransactionType = "TransactionType";
	public static final String Total = "Total";
	
	public QueryReportIncomeVsExpenses(Context context) {
		super(MoneyManagerApplication.getRawAsString(context, R.raw.report_income_vs_expenses), DatasetType.QUERY, "report_income_vs_expenses");
	}
	
	@Override
	public String[] getAllColumns() {
		return new String[] {Month, Year, TransactionType, Total};
	}
}

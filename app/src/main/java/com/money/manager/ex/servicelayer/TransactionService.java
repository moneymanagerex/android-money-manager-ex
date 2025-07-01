package com.money.manager.ex.servicelayer;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;

import com.money.manager.ex.MmexApplication;
import com.money.manager.ex.budget.models.BudgetModel;
import com.money.manager.ex.database.QueryMobileData;
import com.money.manager.ex.database.WhereStatementGenerator;
import com.money.manager.ex.utils.MmxDate;
import com.squareup.sqlbrite3.BriteDatabase;

import java.util.Date;

import javax.inject.Inject;

import dagger.Lazy;

public class TransactionService extends ServiceBase {

    @Inject
    Lazy<BriteDatabase> databaseLazy;

    public TransactionService(Context context) {
        super(context);
        MmexApplication.getApp().iocComponent.inject(this);
    }

    public double getActualValueForCategoryAndPeriod(long categId, MmxDate dateFrom, MmxDate dateTo) {
        return internalGetActualValueForCategoryAndChildrenAndPeriod(categId, null, dateFrom, dateTo );
    }

    public double getActualValueForCategoryAndChildrenAndPeriod(long categId, String categoryName, MmxDate dateFrom, MmxDate dateTo ) {
        return internalGetActualValueForCategoryAndChildrenAndPeriod(categId, categoryName, dateFrom, dateTo);
    }

    private double internalGetActualValueForCategoryAndChildrenAndPeriod(
            long categId,
            String categoryName,
            MmxDate dateFrom,
            MmxDate dateTo
            ) {

        String[] projectionIn = new String[]{
                QueryMobileData.CATEGID,
                "SUM( " + QueryMobileData.AmountBaseConvRate + ") AS TOTAL"
        };

        WhereStatementGenerator where = new WhereStatementGenerator();
        where.addStatement(QueryMobileData.Status + "<>'V'");
        where.addStatement(QueryMobileData.TransactionType + " IN ('Withdrawal', 'Deposit')");

        if ( categoryName != null ) {
            String categoryNameTmp = categoryName;
            if ( categoryNameTmp.contains("'")) {
                categoryNameTmp = categoryNameTmp.replace("\"", "\"\"");
            }
            String localWhere = "( " +
                    QueryMobileData.CATEGID + " = " + categId
                    + " OR " +
                    QueryMobileData.Category + " LIKE \"" + categoryNameTmp +":%\" )";
            where.addStatement(localWhere);
        } else {
            where.addStatement(QueryMobileData.CATEGID + " = " + categId);
        }

        where.addStatement(QueryMobileData.Date + " BETWEEN '" + dateFrom.toIsoDateString() + "' AND '" + dateTo.toIsoDateString()+"'");

        SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
        QueryMobileData mobileData = new QueryMobileData(getContext());
        builder.setTables(mobileData.getSource());
        String sql = builder.buildQuery(projectionIn, where.getWhere(), QueryMobileData.CATEGID, null, null, null);
        Cursor cursor = databaseLazy.get().query(sql);

        if (cursor == null) return 0;
        // add all the categories and subcategories together.
        double total = 0;
        while (cursor.moveToNext()) {
            total += cursor.getDouble(cursor.getColumnIndexOrThrow("TOTAL"));
        }
        cursor.close();

        return total;
    }

}

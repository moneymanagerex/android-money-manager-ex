package com.money.manager.ex.datalayer;

import android.content.Context;
import android.database.Cursor;

import com.money.manager.ex.R;
import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.database.QueryAllData;
import com.money.manager.ex.domainmodel.AccountTransaction;
import com.money.manager.ex.utils.RawFileUtils;
import com.money.manager.ex.viewmodels.AccountTransactionDisplay;

/**
 * Account Transaction repository.
 */
public class QueryAllDataRepository
    extends RepositoryBase {

    public QueryAllDataRepository(Context context) {
        super(context, RawFileUtils.getRawAsString(context, R.raw.query_alldata), DatasetType.QUERY,
            "queryalldata");
    }

    @Override
    public String[] getAllColumns() {
        return new String[]{"ID AS _id", QueryAllData.ID, QueryAllData.TransactionType,
            QueryAllData.Date, QueryAllData.UserDate, QueryAllData.Year, QueryAllData.Month,
            QueryAllData.Day, QueryAllData.Category, QueryAllData.Subcategory, QueryAllData.Amount,
            QueryAllData.BaseConvRate, QueryAllData.CURRENCYID, QueryAllData.AccountName,
            QueryAllData.ACCOUNTID,
//                FromAccountName, FromAccountId, FromAmount, FromCurrencyId,
            QueryAllData.SPLITTED, QueryAllData.CategID, QueryAllData.SubcategID,
            QueryAllData.Payee, QueryAllData.PayeeID, QueryAllData.TransactionNumber,
            QueryAllData.Status, QueryAllData.Notes, QueryAllData.ToAccountName,
            QueryAllData.TOACCOUNTID, QueryAllData.ToAmount, QueryAllData.ToCurrencyId,
            QueryAllData.currency, QueryAllData.finyear};
    }

    public int add(AccountTransactionDisplay entity) {
        return insert(entity.contentValues);
    }

    public Cursor query(String selection, String sort) {
        return openCursor(null, selection, null, sort);
    }

}

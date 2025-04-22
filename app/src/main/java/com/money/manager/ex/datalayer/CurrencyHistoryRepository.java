package com.money.manager.ex.datalayer;

import android.content.Context;

import com.money.manager.ex.database.DatasetType;
import com.money.manager.ex.domainmodel.CurrencyHistory;

public class CurrencyHistoryRepository extends RepositoryBase<CurrencyHistory> {

    private static final String TABLE_NAME = "currencyhistory_v1";
    private static final String ID_COLUMN = CurrencyHistory.CURRHISTID;
    private static final String NAME_COLUMN = CurrencyHistory.CURRDATE;

    public CurrencyHistoryRepository(Context context) {
        super(context, TABLE_NAME, DatasetType.TABLE, "currencyhistory", ID_COLUMN, NAME_COLUMN);
    }

    @Override
    protected CurrencyHistory createEntity() {
        return new CurrencyHistory();
    }

    @Override
    public String[] getAllColumns() {
        return new String[] {
                ID_COLUMN + " AS _id",  // Mapping the CURRHISTID column as _id for SQLite database
                CurrencyHistory.CURRHISTID,
                CurrencyHistory.CURRENCYID,
                CurrencyHistory.CURRDATE,
                CurrencyHistory.CURRVALUE,
                CurrencyHistory.CURRUPDTYPE
        };
    }
}

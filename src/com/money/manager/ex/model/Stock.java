package com.money.manager.ex.model;

import android.database.Cursor;

import com.money.manager.ex.database.TableStock;
import com.money.manager.ex.utils.DateUtils;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;

/**
 * Stock entity.
 * Created by Alen on 5/09/2015.
 */
public class Stock
    extends EntityBase {

    public static Stock getInstance() {
        Stock stock = new Stock();
        // Set to today.
        stock.setPurchaseDate(Calendar.getInstance().getTime());
        stock.setNumberOfShares(BigDecimal.ZERO);

        return stock;
    }

    public Stock() {
        // default constructor.
    }

    public Stock(Cursor c) {
        super(c);

    }

    private Integer Id;
    private Date PurchaseDate;
    private BigDecimal NumberOfShares;

    // properties

    public int getId() {
        if (Id == null && mCursor != null) {
            Id = mCursor.getInt(mCursor.getColumnIndex(TableStock.STOCKID));
        }
        return Id;
    }

    public Date getPurchaseDate() {
        if (PurchaseDate == null && mCursor != null) {
            String dateString = mCursor.getString(mCursor.getColumnIndex(TableStock.PURCHASEDATE));
            PurchaseDate = DateUtils.getDateFromIsoString(dateString);
            if (PurchaseDate == null) {
                PurchaseDate = Calendar.getInstance().getTime();
            }
        }
        return PurchaseDate;
    }

    public void setPurchaseDate(Date purchaseDate) {
        PurchaseDate = purchaseDate;
    }

    public BigDecimal getNumberOfShares() {
        if (NumberOfShares == null && mCursor != null) {
            String dbValue = mCursor.getString(mCursor.getColumnIndex(TableStock.NUMSHARES));
            this.NumberOfShares = new BigDecimal(dbValue);
        }
        return this.NumberOfShares;
    }

    public void setNumberOfShares(BigDecimal numberOfShares) {
        this.NumberOfShares = numberOfShares;
    }
}

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
        stock.setPurchasePrice(BigDecimal.ZERO);
        stock.setCommission(BigDecimal.ZERO);
        // should this be null?
        stock.setCurrentPrice(BigDecimal.ZERO);

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
    private BigDecimal PurchasePrice;
    private BigDecimal Commission;
    private BigDecimal CurrentPrice;
    private BigDecimal Value;

    // properties

    public int getId() {
        if (Id == null && mCursor != null) {
            Id = mCursor.getInt(mCursor.getColumnIndex(TableStock.STOCKID));
        }
        return Id;
    }

    public BigDecimal getCurrentPrice() {
        return this.CurrentPrice;
    }

    public void setCurrentPrice(BigDecimal currentPrice) {
        this.CurrentPrice = currentPrice;
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

    public BigDecimal getPurchasePrice() {
        // todo: load

        return this.PurchasePrice;
    }

    public void setPurchasePrice(BigDecimal purchasePrice) {
        this.PurchasePrice = purchasePrice;
    }

    public BigDecimal getValue() {
        // value = current price * num shares
        this.Value = this.NumberOfShares.multiply(this.CurrentPrice);

        return this.Value;
    }

    public BigDecimal getCommission() {
        return this.Commission;
    }

    public void setCommission(BigDecimal commission) {
        this.Commission = commission;
    }
}

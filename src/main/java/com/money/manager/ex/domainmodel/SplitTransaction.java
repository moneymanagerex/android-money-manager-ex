package com.money.manager.ex.domainmodel;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import com.money.manager.ex.database.ISplitTransactionsDataset;

import org.apache.commons.lang3.StringUtils;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Incomplete
 * Split Category for checking account transaction.
 */
public class SplitTransaction
    extends EntityBase
    implements ISplitTransactionsDataset {

    public static String TABLE_NAME = "SPLITTRANSACTIONS_V1";

    public static final String SPLITTRANSID = "SPLITTRANSID";
    public static final String TRANSID = "TRANSID";
    public static final String CATEGID = "CATEGID";
    public static final String SUBCATEGID = "SUBCATEGID";
    public static final String SPLITTRANSAMOUNT = "SPLITTRANSAMOUNT";

    public final static Parcelable.Creator<SplitTransaction> CREATOR = new Parcelable.Creator<SplitTransaction>() {
        public SplitTransaction createFromParcel(Parcel source) {
            SplitTransaction splitTransactions = new SplitTransaction();
            splitTransactions.readToParcel(source);
            return splitTransactions;
        }

        @Override
        public SplitTransaction[] newArray(int size) {
            return new SplitTransaction[size];
        }
    };

    @Override
    public Uri getUri() {
        return null;
    }

    @Override
    public int getCategId() {
        return getInt(CATEGID);
    }

    @Override
    public Money getSplitTransAmount() {
        return getMoney(SPLITTRANSAMOUNT);
    }

    @Override
    public int getSplitTransId() {
        return getInt(SPLITTRANSID);
    }

    @Override
    public int getSubCategId() {
        return getInt(SUBCATEGID);
    }

    @Override
    public void setCategId(int categId) {
        setInteger(CATEGID, categId);
    }

    @Override
    public void setSplitTransAmount(Money splitTransAmount) {
        setMoney(SPLITTRANSAMOUNT, splitTransAmount);
    }

    @Override
    public void setSplitTransId(int splitTransId) {
        setInteger(SPLITTRANSID, splitTransId);
    }

    @Override
    public void setSubCategId(int subCategId) {
        setInteger(SUBCATEGID, subCategId);
    }

    public int getTransId() {
        return getInt(TRANSID);
    }

    public void setTransId(int value) {
        setInteger(TRANSID, value);
    }

    public void readToParcel(Parcel source) {
        setSplitTransId(source.readInt());
        setTransId(source.readInt());
        setCategId(source.readInt());
        setSubCategId(source.readInt());
        String amount = source.readString();
        if (StringUtils.isNotEmpty(amount)) {
            setSplitTransAmount(MoneyFactory.fromString(amount));
        }
    }

}

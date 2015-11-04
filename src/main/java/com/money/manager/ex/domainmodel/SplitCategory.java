package com.money.manager.ex.domainmodel;

import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Parcel;
import android.os.Parcelable;

import com.money.manager.ex.Constants;
import com.money.manager.ex.database.ISplitTransactionsDataset;

import org.apache.commons.lang3.StringUtils;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Split Category for checking account transaction.
 */
public class SplitCategory
    extends EntityBase
    implements ISplitTransactionsDataset {

    public static String TABLE_NAME = "SPLITTRANSACTIONS_V1";

    public static final String SPLITTRANSID = "SPLITTRANSID";
    public static final String TRANSID = "TRANSID";
    public static final String CATEGID = "CATEGID";
    public static final String SUBCATEGID = "SUBCATEGID";
    public static final String SPLITTRANSAMOUNT = "SPLITTRANSAMOUNT";

    public final static Parcelable.Creator<SplitCategory> CREATOR = new Parcelable.Creator<SplitCategory>() {
        public SplitCategory createFromParcel(Parcel source) {
            SplitCategory split = new SplitCategory();
            split.readFromParcel(source);
            return split;
        }

        @Override
        public SplitCategory[] newArray(int size) {
            return new SplitCategory[size];
        }
    };

    public static SplitCategory create(int transactionId, int categoryId, int subcategoryId,
                                             double amount) {
        SplitCategory entity = new SplitCategory();

        entity.setId(Constants.NOT_SET);
        entity.setCategId(categoryId);
        entity.setSubCategId(subcategoryId);
        entity.setSplitTransAmount(MoneyFactory.fromDouble(amount));
        entity.setTransId(transactionId);

        return entity;
    }

    public Integer getId() {
        return getInt(SPLITTRANSID);
    }

    public void setId(int value) {
        setInteger(SPLITTRANSID, value);
    }

    @Override
    public Integer getCategId() {
        return getInt(CATEGID);
    }

    @Override
    public Money getSplitTransAmount() {
        return getMoney(SPLITTRANSAMOUNT);
    }

    @Override
    public Integer getSubCategId() {
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
    public void setSubCategId(int subCategId) {
        setInteger(SUBCATEGID, subCategId);
    }

    @Override
    public void loadFromCursor(Cursor c) {
        super.loadFromCursor(c);

        DatabaseUtils.cursorDoubleToContentValuesIfPresent(c, contentValues, SPLITTRANSAMOUNT);
    }

    public int getTransId() {
        return getInt(TRANSID);
    }

    public void setTransId(int value) {
        setInteger(TRANSID, value);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getId());
        dest.writeInt(getTransId());
        dest.writeInt(getCategId());
        dest.writeInt(getSubCategId());
        dest.writeString(getSplitTransAmount().toString());
    }

    public void readFromParcel(Parcel source) {
        setId(source.readInt());
        setTransId(source.readInt());
        setCategId(source.readInt());
        setSubCategId(source.readInt());
        String amount = source.readString();
        if (StringUtils.isNotEmpty(amount)) {
            setSplitTransAmount(MoneyFactory.fromString(amount));
        }
    }

}

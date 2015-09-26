package com.money.manager.ex.search;

import android.os.Parcel;
import android.os.Parcelable;

import com.money.manager.ex.Constants;
import com.money.manager.ex.utils.DateUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import info.javaperformance.money.Money;
import info.javaperformance.money.MoneyFactory;

/**
 * Class that contains the search parameters.
 * Used as a DTO and to store the values.
 * Created by Alen on 13/07/2015.
 */
public class SearchParameters implements Parcelable {

    public static final String STRING_NULL_VALUE = "null";

    public SearchParameters() {
        // default constructor
        // explicitly set the null value
        this.status = STRING_NULL_VALUE;
    }

    // Account
    public int accountId = Constants.NOT_SET;

    // Transaction Type
    public boolean deposit;
    public boolean transfer;
    public boolean withdrawal;

    // Status
    public String status;

    // Amount
    public Money amountFrom;
    public Money amountTo;

    // Date
    public Date dateFrom;
    public Date dateTo;

    public Integer payeeId = null;
    public String payeeName;

    public CategorySub category;

    public String transactionNumber;
    public String notes;

    protected SearchParameters(Parcel in) {
        accountId = in.readInt();
        deposit = in.readByte() != 0;
        transfer = in.readByte() != 0;
        withdrawal = in.readByte() != 0;
        status = in.readString();

        String amountFromParcel = in.readString();
        if (StringUtils.isNotEmpty(amountFromParcel)) {
            amountFrom = MoneyFactory.fromString(amountFromParcel);
        }

        String amountToParcel = in.readString();
        if (StringUtils.isNotEmpty(amountToParcel)) {
            amountTo = MoneyFactory.fromString(amountToParcel);
        }

        String dateString = in.readString();
        if (!StringUtils.isEmpty(dateString)) {
            dateFrom = DateUtils.getDateFromIsoString(dateString);
        }

        dateString = in.readString();
        if (!StringUtils.isEmpty(dateString)) {
            dateTo = DateUtils.getDateFromIsoString(dateString);
        }

        payeeId = in.readInt();
        payeeName = in.readString();
        category = in.readParcelable(CategorySub.class.getClassLoader());
        transactionNumber = in.readString();
        notes = in.readString();
    }

    public static final Creator<SearchParameters> CREATOR = new Creator<SearchParameters>() {
        @Override
        public SearchParameters createFromParcel(Parcel in) {
            return new SearchParameters(in);
        }

        @Override
        public SearchParameters[] newArray(int size) {
            return new SearchParameters[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(this.accountId);

        parcel.writeByte((byte) (this.deposit ? 1 : 0));
        parcel.writeByte((byte) (this.transfer ? 1 : 0));
        parcel.writeByte((byte) (this.withdrawal ? 1 : 0));

        parcel.writeString(this.status);

        parcel.writeString(amountFrom != null ? amountFrom.toString() : null);
        parcel.writeString(amountTo != null ? amountTo.toString() : null);

        parcel.writeString(DateUtils.getIsoStringDate(dateFrom));
        parcel.writeString(DateUtils.getIsoStringDate(dateTo));

        if (payeeId == null) payeeId = Constants.NOT_SET;
        parcel.writeInt(payeeId);
        parcel.writeString(payeeName);

        parcel.writeParcelable(category, i);

        parcel.writeString(transactionNumber);
        parcel.writeString(notes);
    }

    // Parcelable implementation
}

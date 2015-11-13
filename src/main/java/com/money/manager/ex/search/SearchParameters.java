package com.money.manager.ex.search;

import android.os.Parcel;
import android.os.Parcelable;

import com.money.manager.ex.Constants;
import com.money.manager.ex.utils.DateUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import hirondelle.date4j.DateTime;
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
    public Integer accountId;

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
    public DateTime dateFrom;
    public Date dateTo;

    public Integer payeeId = null;
    public String payeeName;

    public CategorySub category;

    public String transactionNumber;
    public String notes;

    protected SearchParameters(Parcel in) {
        this.accountId = (Integer) in.readValue(null);

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
            dateFrom = new DateTime(dateString);
        }

        dateString = in.readString();
        if (!StringUtils.isEmpty(dateString)) {
            dateTo = DateUtils.getDateFromIsoString(dateString);
        }

        payeeId = (Integer) in.readValue(null);
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
        // this is a sample on how to read/write nullable objects.
        parcel.writeValue(this.accountId);

        parcel.writeByte((byte) (this.deposit ? 1 : 0));
        parcel.writeByte((byte) (this.transfer ? 1 : 0));
        parcel.writeByte((byte) (this.withdrawal ? 1 : 0));

        parcel.writeString(this.status);

        parcel.writeString(amountFrom != null ? amountFrom.toString() : null);
        parcel.writeString(amountTo != null ? amountTo.toString() : null);

        String dateString = dateFrom != null ? dateFrom.toString() : "";
        parcel.writeString(dateString);
        parcel.writeString(DateUtils.getIsoStringDate(dateTo));

        parcel.writeValue(payeeId);
        parcel.writeString(payeeName);

        parcel.writeParcelable(category, i);

        parcel.writeString(transactionNumber);
        parcel.writeString(notes);
    }

    // Parcelable implementation
}

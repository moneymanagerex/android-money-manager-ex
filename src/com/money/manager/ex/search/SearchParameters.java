package com.money.manager.ex.search;

import com.money.manager.ex.Constants;

/**
 * Class that contains the search parameters.
 * Used as a DTO and to store the values.
 * Created by Alen on 13/07/2015.
 */
public class SearchParameters {
    // Account
    public int accountId = Constants.NOT_SET;

    // Transaction Type
    public boolean deposit;
    public boolean transfer;
    public boolean withdrawal;

    // Status
    public String status;

    // Amount
    public String amountFrom;
    public String amountTo;

    // Date
    public String dateFrom;
    public String dateTo;

    public String payee;

    public CategorySub category;

    public String transactionNumber;
    public String notes;
}

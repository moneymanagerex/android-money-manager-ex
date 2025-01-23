/*
 * Copyright (C) 2012-2018 The Android Money Manager Ex Project Team
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.money.manager.ex.database;

import com.money.manager.ex.core.TransactionTypes;
import com.money.manager.ex.domainmodel.Taglink;

import java.util.ArrayList;
import java.util.Date;

import info.javaperformance.money.Money;

/**
 * Interface for Recurring/Checking transactions.
 * This is a common transaction (the common fields in Account & Recurring transactions).
 */
public interface ITransactionEntity {

    String ACCOUNTID = "ACCOUNTID";
    String CATEGID = "CATEGID";
    String NOTES = "NOTES";
    String PAYEEID = "PAYEEID";
    String STATUS = "STATUS";
    String TOACCOUNTID = "TOACCOUNTID";
    String TOTRANSAMOUNT = "TOTRANSAMOUNT";
    String TRANSAMOUNT = "TRANSAMOUNT";
    String TRANSCODE = "TRANSCODE";
    String TRANSACTIONNUMBER = "TRANSACTIONNUMBER";
    String TRANSDATE = "TRANSDATE";
    String FOLLOWUPID = "FOLLOWUPID";
    String COLOR = "COLOR";

    Long getId();
    void setId(Long value);
    boolean hasId();

    Long getAccountId();
    void setAccountId(Long value);

    Long getAccountToId();
    void setAccountToId(Long value);
    boolean hasAccountTo();

    Long getCategoryId();
    void setCategoryId(Long value);
    boolean hasCategory();

    /**
     * @return the splitTransAmount
     */
    Money getAmount();
    /**
     * @param value the splitTransAmount to set
     */
    void setAmount(Money value);

    Money getAmountTo();
    void setAmountTo(Money value);

    Date getDate();
    String getDateString();
    void setDate(Date value);

    String getNotes();
    void setNotes(String value);

    Long getPayeeId();
    void setPayeeId(Long value);
    boolean hasPayee();

    String getStatus();
    void setStatus(String value);

    String getTransactionNumber();
    void setTransactionNumber(String value);

    public String getTransactionCode();
    TransactionTypes getTransactionType();
    void setTransactionType(TransactionTypes value);

    public String getTransactionModel();

    public void setTags(ArrayList<Taglink> tags) ;
    public ArrayList<Taglink> getTags() ;

    public void setColor(int value) ;
    public int getColor() ;

}

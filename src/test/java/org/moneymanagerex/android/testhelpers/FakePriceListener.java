package org.moneymanagerex.android.testhelpers;

import com.money.manager.ex.investment.IPriceUpdaterFeedback;

import java.util.Date;

import info.javaperformance.money.Money;

/**
 * Receives the notification about downloaded price. Used in unit testing.
 * Created by Alen on 26/09/2015.
 */
public class FakePriceListener
    implements IPriceUpdaterFeedback {

    public String symbol;
    public Money price;
    public Date date;

    @Override
    public void onPriceDownloaded(String symbol, Money price, Date date) {
        this.symbol = symbol;
        this.price = price;
        this.date = date;
    }
}

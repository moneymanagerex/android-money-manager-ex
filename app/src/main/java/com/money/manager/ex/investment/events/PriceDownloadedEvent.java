package com.money.manager.ex.investment.events;

import org.joda.time.DateTime;

import java.util.Date;

import info.javaperformance.money.Money;

/**
 * Raised when a price is downloaded. Used for currencies or stocks.
 */
public class PriceDownloadedEvent {

    public PriceDownloadedEvent(String symbol, Money price, DateTime date) {
        this.symbol = symbol;
        this.price = price;
        this.date = date;
    }

    public String symbol;
    public Money price;
    public DateTime date;
}

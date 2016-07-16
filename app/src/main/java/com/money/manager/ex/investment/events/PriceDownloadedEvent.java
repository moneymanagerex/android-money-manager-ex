package com.money.manager.ex.investment.events;

import org.joda.time.DateTime;
import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import info.javaperformance.money.Money;

/**
 * Raised when a price is downloaded. Used for currencies or stocks.
 */
@Parcel
public class PriceDownloadedEvent {

    @ParcelConstructor
    public PriceDownloadedEvent(String symbol, Money price, DateTime date) {
        this.symbol = symbol;
        this.price = price;
        this.date = date;
    }

    public String symbol;
    public Money price;
    public DateTime date;
}

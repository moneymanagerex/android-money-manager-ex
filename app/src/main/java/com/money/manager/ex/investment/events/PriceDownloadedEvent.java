package com.money.manager.ex.investment.events;

import org.parceler.Parcel;
import org.parceler.ParcelConstructor;

import java.util.Date;

import info.javaperformance.money.Money;

/**
 * Raised when a price is downloaded. Used for currencies or stocks.
 */
@Parcel
public class PriceDownloadedEvent {

    @ParcelConstructor
    public PriceDownloadedEvent(String symbol, Money price, Date date) {
        this.symbol = symbol;
        this.price = price;
        this.date = date;
    }

    public String symbol;
    public Money price;
    public Date date;
}

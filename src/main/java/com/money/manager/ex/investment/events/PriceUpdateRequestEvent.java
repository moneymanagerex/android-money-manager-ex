package com.money.manager.ex.investment.events;

/**
 * Price update requested for a security.
 */
public class PriceUpdateRequestEvent {

    public PriceUpdateRequestEvent(String symbol) {
        this.symbol = symbol;
    }

    public String symbol;
}

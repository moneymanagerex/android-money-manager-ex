package com.money.manager.ex.home.events;

/**
 * Request for the display of Watchlist fragment (in Main Activity).
 */
public class RequestWatchlistFragmentEvent {
    public RequestWatchlistFragmentEvent(int accountId) {
        this.accountId = accountId;
    }

    public int accountId;
}

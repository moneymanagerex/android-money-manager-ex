package com.money.manager.ex.investment;

/**
 * Interface for security price updater functionality.
 */
public interface ISecurityPriceUpdater {
    /**
     * Download prices for all the securities and update the values.
     * Store the values in the history table.
     */
    boolean updatePrices();

    /**
     * Update individual price
     * @param symbol Symbol of the security to import. Provider specific.
     */
    boolean updatePrice(String symbol);
}

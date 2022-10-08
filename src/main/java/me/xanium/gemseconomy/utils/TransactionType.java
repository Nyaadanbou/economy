package me.xanium.gemseconomy.utils;

public enum TransactionType {

    /**
     * Use DEPOSIT for adding currency to a player
     */
    DEPOSIT,
    /**
     * Use WITHDRAW for removing currency from a player
     */
    WITHDRAW,
    /**
     * Use SET for setting a player's currency balance
     */
    SET,
    /**
     * Use CONVERSION for currency exchanges
     */
    CONVERSION

}

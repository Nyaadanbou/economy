package me.xanium.gemseconomy.message;

/**
 * Whether to update {@link me.xanium.gemseconomy.account.Account} or {@link me.xanium.gemseconomy.currency.Currency}.
 */
public enum Action {
    UPDATE_ACCOUNT,
    DELETE_ACCOUNT,
    CREATE_ACCOUNT,
    UPDATE_CURRENCY,
    DELETE_CURRENCY,
    CREATE_CURRENCY
}

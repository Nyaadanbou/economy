package cc.mewcraft.economy.api;

import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.ApiStatus;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Economy {
    /**
     * Get an account, or create one if not existing.
     *
     * @param uuid account's unique ID
     * @return an account object with specific unique ID
     */
    @ApiStatus.Experimental
    @NonNull Account pullAccount(@NonNull UUID uuid);

    /**
     * Get an account, or return null if not existing.
     *
     * @param uuid account's unique ID
     * @return an account object with specific unique ID
     */
    @ApiStatus.Experimental
    @Nullable Account getAccount(@NonNull UUID uuid);

    /**
     * Check if specific account exists in database.
     *
     * @param uuid account's unique ID
     * @return true if account with specific unique ID exists in database
     */
    boolean hasAccount(@NonNull UUID uuid);

    /**
     * Deposit specified amount into specific account.
     *
     * @param uuid   the account's unique ID
     * @param amount the amount of default currency
     */
    void deposit(@NonNull UUID uuid, double amount);

    /**
     * Deposit specified amount into specific account.
     *
     * @param uuid     the account's unique ID
     * @param amount   the amount of specific currency
     * @param currency the specific currency
     */
    void deposit(@NonNull UUID uuid, double amount, @NonNull Currency currency);

    /**
     * Withdraw specific amount from specific account.
     *
     * @param uuid   the account's unique ID
     * @param amount the amount of default currency
     */
    void withdraw(@NonNull UUID uuid, double amount);

    /**
     * Withdraw specific amount from specific account.
     *
     * @param uuid     the account's unique ID
     * @param amount   the amount of specific currency
     * @param currency the currency you withdraw from
     */
    void withdraw(@NonNull UUID uuid, double amount, @NonNull Currency currency);

    /**
     * Lookup the balance of specific account.
     *
     * @param uuid the account's unique ID
     * @return the balance of default currency
     */
    double getBalance(@NonNull UUID uuid);

    /**
     * Lookup the balance of specific account.
     *
     * @param uuid     the account's unique ID
     * @param currency the amount of default currency
     * @return the balance of specific currency
     */
    double getBalance(@NonNull UUID uuid, @NonNull Currency currency);

    /**
     * @param name the currency name
     * @return a currency object of specific name
     */
    @ApiStatus.Experimental
    @Nullable Currency getCurrency(@NonNull String name);

    /**
     * @return the default currency
     */
    @ApiStatus.Experimental
    @NonNull Currency getDefaultCurrency();

    /**
     * @return an unmodifiable list of loaded currencies
     */
    @ApiStatus.Experimental
    @NonNull List<Currency> getLoadedCurrencies();
}

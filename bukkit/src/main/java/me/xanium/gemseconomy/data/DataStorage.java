/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.data;

import me.lucko.helper.promise.Promise;
import me.xanium.gemseconomy.GemsEconomyPlugin;
import me.xanium.gemseconomy.api.Account;
import me.xanium.gemseconomy.api.Currency;
import me.xanium.gemseconomy.listener.EconomyListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.Contract;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public abstract class DataStorage {

    public final GemsEconomyPlugin plugin = GemsEconomyPlugin.getInstance();

    private final StorageType storageType;
    private final boolean topSupported;
    private static final List<DataStorage> methods = new ArrayList<>();

    public DataStorage(@NonNull StorageType storageType, boolean topSupported) {
        this.storageType = storageType;
        this.topSupported = topSupported;
    }

    /**
     * Gets an instance of given storage method.
     *
     * @param method a method
     * @return an instance of given storage method
     */
    public static @Nullable DataStorage getMethod(final @NonNull StorageType method) {
        for (DataStorage store : getMethods()) {
            if (store.getStorageType() == method) {
                return store;
            }
        }
        return null;
    }

    /**
     * Returns available storage methods.
     *
     * @return available storage methods.
     */
    public static @NonNull List<DataStorage> getMethods() {
        return methods;
    }

    /**
     * Do all the necessary initialization stuff here.
     */
    public abstract void initialize();

    /**
     * Shutdown and clean up.
     */
    public abstract void close();

    /**
     * Loads, and returns all currencies from database.
     */
    public abstract List<Currency> loadCurrencies();

    /**
     * Loads, and returns specific currency from database.
     *
     * @param uuid the uuid of specific currency
     */
    public abstract @Nullable Currency loadCurrency(final @NonNull UUID uuid);

    /**
     * Saves given currency to database.
     *
     * @param currency the currency to save to database
     */
    @Contract(pure = true)
    public abstract void saveCurrency(final @NonNull Currency currency);

    /**
     * Deletes given currency from database.
     *
     * @param currency the currency to delete from database
     */
    @Contract(pure = true)
    public abstract void deleteCurrency(final @NonNull Currency currency);

    /**
     * Updates specific account.
     *
     * @param account the account to be updated
     * @return the same account reference with states being updated
     */
    @Contract("null -> null; !null -> !null")
    public abstract @Nullable Account updateAccount(final @Nullable Account account);

    /**
     * Loads, and returns an account with specific name from database.
     * <p>
     * This method will return null if specific name doesn't exist in database.
     *
     * @param name the account name
     * @return an account with specific name
     */
    public abstract @Nullable Account loadAccount(final @NonNull String name);

    /**
     * Loads an account with the specific uuid from database, and returns it.
     * <p>
     * This method will return null if specific uuid doesn't exist in database.
     *
     * @param uuid the account uuid
     * @return an account with the specific uuid
     */
    public abstract @Nullable Account loadAccount(final @NonNull UUID uuid);

    /**
     * Saves the specific account to database.
     *
     * @param account the account to save to database
     */
    @Contract(pure = true)
    public abstract void saveAccount(final @NonNull Account account);

    /**
     * Creates a new record of specific account in database.
     * <p>
     * The specific account should be a freshly created instance.
     *
     * @param account the new account to save to database
     * @see EconomyListener
     */
    @Contract(pure = true)
    public abstract void createAccount(final @NonNull Account account);

    /**
     * Deletes specific account from database.
     *
     * @param account the account to delete from database
     */
    @Contract(pure = true)
    public abstract void deleteAccount(final @NonNull Account account);

    /**
     * Deletes the account with specific uuid from database.
     *
     * @param uuid the account with specific uuid to delete from database
     */
    public abstract void deleteAccount(final @NonNull UUID uuid);

    /**
     * Deletes the account with specific name from database.
     *
     * @param name the account with specific name to delete from database
     */
    public abstract void deleteAccount(final @NonNull String name);

    /**
     * Loads, and returns ALL accounts in database.
     *
     * @return all accounts in database
     */
    public abstract @NonNull List<Account> getOfflineAccounts();

    /**
     * Gets a {@link Promise} containing list of ALL offline balances for specific Currency.
     * <p>
     * The implementation should not store any data in memory for long time.
     *
     * @param currency the currency which the balances are fetched from
     * @return a promise
     */
    @Contract(pure = true)
    public @NonNull Promise<List<TransientBalance>> getTransientBalances(final @NonNull Currency currency) {
        return Promise.completed(new ArrayList<>());
    }

    /**
     * Returns the storage type of this database.
     *
     * @return the storage type of this database
     */
    public @NonNull StorageType getStorageType() {
        return this.storageType;
    }

    /**
     * Checks if this storage supports "balance top".
     *
     * @return true if this storage supports "balance top"
     */
    public boolean isTopSupported() {
        return this.topSupported;
    }

    /**
     * @see #loadAccount(UUID)
     */
    public Promise<Account> loadAccountAsync(final @NonNull UUID uuid) {
        return Promise.supplyingAsync(() -> loadAccount(uuid));
    }

}


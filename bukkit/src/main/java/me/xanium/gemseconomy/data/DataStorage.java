/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.data;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.currency.CachedTopListEntry;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.listeners.EconomyListener;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.Contract;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public abstract class DataStorage {

    public final GemsEconomy plugin = GemsEconomy.getInstance();

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
     *
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
     * Gets the balance top list of given currency, then performs an action to the fetched top list.
     *
     * @param currency the Currency from which the top list is derived
     * @param start    the start index of the top list
     * @param amount   the amount of Account to fetch
     * @param action   the action applied to the top list
     */
    @Contract(pure = true)
    public abstract void getTopList(final @NonNull Currency currency, int start, int amount, final @NonNull Consumer<LinkedList<CachedTopListEntry>> action);

    /**
     * Loads all currencies into memory from database.
     */
    public abstract void loadCurrencies();

    /**
     * Loads, and returns specific Currency from database.
     *
     * @param uuid the uuid of specific Currency
     */
    public abstract @Nullable Currency loadCurrency(final @NonNull UUID uuid);

    /**
     * Saves given Currency to database.
     *
     * @param currency the Currency to save to database
     */
    @Contract(pure = true)
    public abstract void saveCurrency(final @NonNull Currency currency);

    /**
     * Deletes given Currency from database.
     *
     * @param currency the currency to delete from database
     */
    @Contract(pure = true)
    public abstract void deleteCurrency(final @NonNull Currency currency);

    /**
     * Loads, and returns an Account with given name from database.
     * <p>
     * This method will return null if given name doesn't exist in database.
     *
     * @param name the account name
     *
     * @return an account with given name
     */
    public abstract @Nullable Account loadAccount(final @NonNull String name);

    /**
     * Loads an account with the specific uuid from database, and returns it.
     * <p>
     * This method will return null if given uuid doesn't exist in database.
     *
     * @param uuid the account uuid
     *
     * @return an account with the specific uuid
     */
    public abstract @Nullable Account loadAccount(final @NonNull UUID uuid);

    /**
     * Saves the specific Account to database.
     *
     * @param account the Account to save to database
     */
    @Contract(pure = true)
    public abstract void saveAccount(final @NonNull Account account);

    /**
     * Creates a new record of given Account in database.
     * <p>
     * The given Account should be a freshly created instance.
     *
     * @param account the new Account to save to database
     *
     * @see EconomyListener
     */
    @Contract(pure = true)
    public abstract void createAccount(final @NonNull Account account);

    /**
     * Deletes given Account from database.
     *
     * @param account the account to delete from database
     */
    @Contract(pure = true)
    public abstract void deleteAccount(final @NonNull Account account);

    /**
     * Deletes the Account with given uuid from database.
     *
     * @param uuid the account with given uuid to delete from database
     */
    public abstract void deleteAccount(final @NonNull UUID uuid);

    /**
     * Deletes the Account with given name from database.
     *
     * @param name the account with given name to delete from database
     */
    public abstract void deleteAccount(final @NonNull String name);

    /**
     * Loads, and returns all the accounts in database.
     *
     * @return all the accounts in database
     */
    public abstract @NonNull List<Account> getOfflineAccounts();

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
    public void loadAccount(final @NonNull UUID uuid, final @NonNull Consumer<Account> action) {
        Account account = loadAccount(uuid);
        action.accept(account);
    }

}


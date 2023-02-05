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

@SuppressWarnings("DefaultAnnotationParam")
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
     * Gets an instance of the given storage method.
     *
     * @param method a method
     *
     * @return an instance of the given storage method
     */
    public static @Nullable DataStorage getMethod(@NonNull StorageType method) {
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
     * Loads all currencies into memory from database.
     */
    public abstract void loadCurrencies();

    /**
     * Updates the given Currency from database.
     * <p>
     * This will load the Currency data from database, then modify the states of the given Currency so that its internal
     * states are synced with the data in database.
     *
     * @param currency the Currency to update
     */
    @Contract(pure = false)
    public abstract void updateCurrencyLocally(@NonNull Currency currency);

    /**
     * Saves the given Currency to database.
     *
     * @param currency the Currency to save to database
     */
    @Contract(pure = true)
    public abstract void saveCurrency(@NonNull Currency currency);

    /**
     * Deletes the given Currency from database.
     *
     * @param currency the currency to delete from database
     */
    @Contract(pure = true)
    public abstract void deleteCurrency(@NonNull Currency currency);

    @Contract(pure = true)
    public abstract void getTopList(@NonNull Currency currency, int offset, int amount, @NonNull Consumer<LinkedList<CachedTopListEntry>> action);

    /**
     * Loads, and returns an Account with the given name from database.
     * <p>
     * This method will return null if the given name doesn't exist in database, <b>except</b> for some special cases.
     * Specifically, this should return a non-null Account for the following special cases:
     * <ul>
     *     <li>the names are Towny accounts, starting with "town-", "nation-" or "towny-"</li>
     * </ul>
     * <p>
     * The uuids for these special Accounts should be "stable". That is, the same name always generates the Accounts
     * with the same uuid. The algorithm used to generate the uuids is preferred using the one used to generate offline
     * Minecraft accounts.
     *
     * @param name the account name
     *
     * @return an account with the given name
     */
    public abstract @Nullable Account loadAccount(@NonNull String name);

    /**
     * Loads an account with the specific uuid from database, and returns it.
     * <p>
     * This method will always return a non-null Account. Specifically, if the uuid does not map to an online Minecraft
     * account, this will still create an offline Minecraft Account with the specific uuid.
     *
     * @param uuid the account uuid
     *
     * @return an account with the specific uuid
     */
    public abstract @NonNull Account loadAccount(@NonNull UUID uuid);

    /**
     * Saves the specific Account to database.
     *
     * @param account the Account to save to database
     */
    @Contract(pure = true)
    public abstract void saveAccount(@NonNull Account account);

    /**
     * Creates a new record of the given Account in database.
     * <p>
     * The given Account should be a freshly created instance.
     *
     * @param account the new Account to save to database
     *
     * @see EconomyListener
     */
    @Contract(pure = false)
    public abstract void createAccount(@NonNull Account account);

    /**
     * Deletes the given Account from database.
     *
     * @param account the account to delete from database
     */
    @Contract(pure = true)
    public abstract void deleteAccount(@NonNull Account account);

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
    public void loadAccount(@NonNull UUID uuid, @NonNull Consumer<Account> action) {
        Account account = this.loadAccount(uuid);
        action.accept(account);
    }

}


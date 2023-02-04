/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.account;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.data.DataStorage;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class AccountManager {

    private final @NonNull GemsEconomy plugin;
    private final @NonNull List<Account> accounts; // A collection of accounts loaded in memory

    public AccountManager(@NonNull GemsEconomy plugin) {
        this.plugin = plugin;
        this.accounts = new ArrayList<>();
    }

    // TODO verify whether we really need this
    // /**
    //  * Creates, and loads an account into the memory.
    //  * <p>
    //  * If the account with the specific name is already loaded, this method will simply do nothing.
    //  *
    //  * @param nickname the nickname of this account
    //  */
    // public synchronized void createAccount(@NonNull String nickname) {
    //     @Nullable Account account = getAccount(nickname);
    //
    //     if (account == null) {
    //         // Get the UUID of the name by using the Mojang offline player method
    //         // so that we can ensure same nicknames always point to the same UUID.
    //         account = new Account(OfflineModeProfiles.getUniqueId(nickname), nickname);
    //         cacheAccount(account);
    //
    //         switch (plugin.getDataStore().getStorageType()) {
    //             case MYSQL -> plugin.getDataStore().createAccount(account);
    //         }
    //     }
    // }

    public synchronized @NonNull Account getAccount(@NonNull Player player) {
        return getAccount(player.getUniqueId());
    }

    public synchronized @NonNull Account getAccount(@NonNull UUID uuid) {
        for (Account account : this.accounts) {
            if (account.getUuid().equals(uuid))
                return account;
        }
        Account account = plugin.getDataStore().loadAccount(uuid);
        this.cacheAccount(account);
        return account;
    }

    public synchronized @Nullable Account getAccount(@NonNull String name) {
        for (Account account : this.accounts) {
            if (name.equalsIgnoreCase(account.getNickname()))
                return account;
        }
        Account account = plugin.getDataStore().loadAccount(name);
        if (account == null)
            return null;
        else
            this.cacheAccount(account);
        return account;
    }

    /**
     * Loads an account into the memory.
     *
     * @param account the account to be loaded into memory
     */
    public synchronized void cacheAccount(@NonNull Account account) {
        if (!this.accounts.contains(account))
            this.accounts.add(account);
    }

    /**
     * Unloads the account from memory.
     *
     * @param uuid the account uuid
     */
    public synchronized void flushAccount(@NonNull UUID uuid) {
        ListIterator<Account> iterator = this.accounts.listIterator();
        while (iterator.hasNext()) {
            Account next = iterator.next();
            if (next.getUuid().equals(uuid)) {
                iterator.remove();
                break;
            }
        }
    }

    /**
     * Returns all the accounts that are currently loaded in memory.
     *
     * @return all the accounts loaded in memory
     */
    public synchronized @NonNull List<Account> getCachedAccounts() {
        return accounts;
    }

    /**
     * @return all the accounts in the database
     *
     * @see DataStorage#getOfflineAccounts()
     */
    public synchronized @NonNull List<Account> getOfflineAccounts() {
        return plugin.getDataStore().getOfflineAccounts();
    }

}


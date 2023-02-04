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

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AccountManager {

    private final @NonNull GemsEconomy plugin;
    private final @NonNull Map<UUID, Account> accounts; // A collection of accounts loaded in memory

    public AccountManager(@NonNull GemsEconomy plugin) {
        this.plugin = plugin;
        this.accounts = new ConcurrentHashMap<>();
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

    public @NonNull Account getAccount(@NonNull Player player) {
        return getAccount(player.getUniqueId());
    }

    public @NonNull Account getAccount(@NonNull UUID uuid) {
        if (accounts.containsKey(uuid))
            return accounts.get(uuid);
        Account account = plugin.getDataStore().loadAccount(uuid);
        this.cacheAccount(account);
        return account;
    }

    public @Nullable Account getAccount(@NonNull String name) {
        for (final Account account : accounts.values()) {
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
     * <p>
     * If the account is already cached, this method will simply do nothing.
     *
     * @param account the account to be loaded into memory
     */
    public void cacheAccount(@NonNull Account account) {
        accounts.putIfAbsent(account.getUuid(), account);
    }

    /**
     * Unloads the account from memory.
     *
     * @param uuid the account uuid
     */
    public void flushAccount(@NonNull UUID uuid) {
        accounts.remove(uuid);
    }

    /**
     * Returns all the accounts that are currently loaded in memory.
     *
     * @return all the accounts loaded in memory
     */
    public @NonNull Collection<Account> getCachedAccounts() {
        return accounts.values();
    }

    /**
     * @return all the accounts in the database
     *
     * @see DataStorage#getOfflineAccounts()
     */
    public @NonNull Collection<Account> getOfflineAccounts() {
        return plugin.getDataStore().getOfflineAccounts();
    }

}


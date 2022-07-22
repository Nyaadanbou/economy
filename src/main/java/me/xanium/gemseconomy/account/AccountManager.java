/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.account;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.utils.OfflineModeProfiles;
import me.xanium.gemseconomy.utils.UtilServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountManager {

    private final GemsEconomy plugin;
    private final List<Account> accounts;

    public AccountManager(GemsEconomy plugin) {
        this.plugin = plugin;
        this.accounts = new ArrayList<>();
    }

    public void createAccount(String nickname) {
        // no more async
        // we need to check the other plugins
        // in case other plugins call it sync

        Account account = getAccount(nickname);

        if (account == null) {
            // get the UUID of the name by using Mojang offline player way
            // so that we can ensure same nicknames always point to the same UUID
            account = new Account(OfflineModeProfiles.getUniqueId(nickname), nickname);

            account.setCanReceiveCurrency(true);
            addAccount(account);

            switch (plugin.getDataStore().getStorageType()) {
                case YAML -> plugin.getDataStore().saveAccount(account);
                case MYSQL -> plugin.getDataStore().createAccount(account);
            }

            UtilServer.consoleLog("New account created for: " + account.getDisplayName());
        }
    }

    public synchronized void createAccount(UUID uuid) {
        Account account = getAccount(uuid);
        String playerName = Bukkit.getOfflinePlayer(uuid).getName();

        if (playerName == null || playerName.isEmpty())
            playerName = "Unknown";
        if (account == null) {
            account = new Account(uuid, playerName);
            account.setCanReceiveCurrency(true);
            addAccount(account);

            switch (plugin.getDataStore().getStorageType()) {
                case YAML -> plugin.getDataStore().saveAccount(account);
                case MYSQL -> plugin.getDataStore().createAccount(account);
            }

            UtilServer.consoleLog("New account created for: " + account.getDisplayName() + "[" + account.getUuid().toString() + "]");
        }
    }

    public Account getAccount(Player player) {
        return getAccount(player.getUniqueId());
    }

    public Account getAccount(String name) {
        for (Account account : this.accounts) {
            if (account.getNickname() == null || !account.getNickname().equalsIgnoreCase(name))
                continue;
            return account;
        }
        return plugin.getDataStore().loadAccount(name);
    }

    public Account getAccount(UUID uuid) {
        for (Account account : this.accounts) { // TODO This throws CME randomly
            if (!account.getUuid().equals(uuid)) continue;
            return account;
        }
        return plugin.getDataStore().loadAccount(uuid);
    }

    public void removeAccount(UUID uuid) {
        for (int i = 0; i < this.accounts.size(); i++) {
            Account a = getAccounts().get(i);
            if (a.getUuid().equals(uuid)) {
                accounts.remove(i);
                break;
            }
        }
    }

    public void addAccount(Account account) {
        if (this.accounts.contains(account)) return;
        this.accounts.add(account);
    }

    public List<Account> getAccounts() {
        return accounts;
    }

    public List<Account> getAllAccounts() {
        return plugin.getDataStore().getOfflineAccounts();
    }
}


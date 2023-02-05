/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.listeners;

import me.lucko.helper.Schedulers;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static java.util.Objects.requireNonNull;

public class EconomyListener implements Listener {

    private final GemsEconomy plugin = GemsEconomy.getInstance();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED)
            return;
        Schedulers.async().run(() -> { // TODO use redis to sync data
            if (!plugin.getAccountManager().hasAccount(player))
                plugin.getAccountManager().createAccount(player); // Create a new Account if it did not exist

            plugin.getAccountManager().refreshAccount(player.getUniqueId()); // Grabs the latest data from database
            Account account = requireNonNull(plugin.getAccountManager().fetchAccount(player)); // Get and cache the Account

            String playerName = player.getName();
            if (!playerName.equals(account.getNickname())) {
                account.setNickname(playerName);
                plugin.getLogger().info("Account name changes detected, updating: " + playerName);
                plugin.getDataStore().saveAccount(account);
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getAccountManager().flushAccount(event.getPlayer().getUniqueId());
    }

}
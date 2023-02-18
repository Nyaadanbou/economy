/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.listener;

import me.lucko.helper.Schedulers;
import me.lucko.helper.terminable.Terminable;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static java.util.Objects.requireNonNull;

public class EconomyListener implements Listener, Terminable {

    private final GemsEconomy plugin = GemsEconomy.getInstance();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED)
            return;

        Player player = event.getPlayer();

        Schedulers.async().run(() -> {
            if (!plugin.getAccountManager().hasAccount(player))
                plugin.getAccountManager().createAccount(player); // Create a new Account if it did not exist

            Account account = plugin.getAccountManager().fetchAccount(player); // Get and cache the Account

            requireNonNull(account, "account"); // Should never be null as we've already checked (and created if needed)

            String playerName = player.getName();
            if (!playerName.equals(account.getNickname())) { // Update nickname when the player changed their name
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

    @Override public void close() {
        HandlerList.unregisterAll(this);
    }

}
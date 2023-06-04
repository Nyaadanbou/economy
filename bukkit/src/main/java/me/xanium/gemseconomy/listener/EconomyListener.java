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
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) {
            return;
        }

        Schedulers.async().run(() -> {
            // If the player has never come to this server, we create one for him.
            // If the player already has an account, we simply load it from database.

            Player player = event.getPlayer();
            plugin.getAccountManager().createAccount(player); // It will create a new account if it does not exist
            Account account = requireNonNull(plugin.getAccountManager().fetchAccount(player), "account");

            // Update nickname of the Account
            String playerName = player.getName();
            if (!playerName.equals(account.getNickname())) {
                account.setNickname(playerName);
                plugin.getDataStore().saveAccount(account);
                plugin.getLogger().info("Account name changes detected, updating: " + playerName);
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
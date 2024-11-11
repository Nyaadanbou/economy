/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package cc.mewcraft.economy.listener;

import cc.mewcraft.economy.EconomyPlugin;
import cc.mewcraft.economy.api.Account;
import me.lucko.helper.terminable.Terminable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class EconomyListener implements Listener, Terminable {

    private final EconomyPlugin plugin = EconomyPlugin.getInstance();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(AsyncPlayerPreLoginEvent event) {
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return;
        }

        // If the player has never come to this server, we create one for him.
        // If the player already has an account, we simply load it from database.

        final UUID uuid = event.getUniqueId();
        final String name = event.getName();
        final Account account = plugin.getAccountManager().createAccount(uuid, name);

        // Update nickname of the account
        if (!name.equals(account.getNickname())) {
            account.setNickname(name);
            plugin.getDataStore().saveAccount(account);
            plugin.getLogger().info("Account name changes detected, updating: " + name);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        //plugin.getAccountManager().flushAccount(event.getPlayer().getUniqueId()); // LoadingCache will remove it automatically
    }

    @Override public void close() {
        HandlerList.unregisterAll(this);
    }

}
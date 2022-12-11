/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.listeners;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import me.xanium.gemseconomy.utils.UtilServer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EconomyListener implements Listener {

    private final GemsEconomy plugin = GemsEconomy.getInstance();

    @EventHandler(priority = EventPriority.LOWEST)
    public void onLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED) return;
        SchedulerUtils.runAsync(() -> {
            Account account = plugin.getAccountManager().getAccount(player.getUniqueId());
            if (account == null) {
                plugin.getAccountManager().createAccount(player.getUniqueId());
            } else {
                plugin.getAccountManager().addAccount(account);
                String name = player.getName();
                if (account.getNickname() == null || !account.getNickname().equalsIgnoreCase(name)) {
                    account.setNickname(name);
                    UtilServer.consoleLog("Account name changes detected, updating: " + name);
                    plugin.getDataStore().saveAccount(account);
                }
            }
        });
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getAccountManager().removeAccount(player.getUniqueId());
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        SchedulerUtils.runLater(40L, () -> {
            if (plugin.getCurrencyManager().getDefaultCurrency() == null && (player.isOp() || player.hasPermission("gemseconomy.command.currency"))) {
                GemsEconomy.lang().sendComponent(player, "err_ask_to_setup_currency");
            }
        });
    }

}


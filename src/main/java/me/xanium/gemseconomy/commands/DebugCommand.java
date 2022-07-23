/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.commands;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.file.F;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class DebugCommand implements CommandExecutor {

    private final GemsEconomy plugin = GemsEconomy.getInstance();

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, @NotNull Command command, @NotNull String s, final String[] args) {
        SchedulerUtils.runAsync(() -> {
            if (!sender.hasPermission("gemseconomy.command.debug")) {
                sender.sendMessage(F.getNoPerms());
                return;
            }
            plugin.setDebug(!plugin.isDebug());
            sender.sendMessage(F.getDebugStatus().replace("{status}", plugin.isDebug() + ""));
        });
        return true;
    }

}





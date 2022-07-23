/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.utils;

import me.xanium.gemseconomy.GemsEconomy;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;

import java.util.Optional;

public class UtilServer {

    private static final String Console_Prefix = "§2[GemsEconomy] §f";
    private static final String Error_Prefix = "§c[G-Eco-Error] §f";

    private static Server getServer() {
        return Bukkit.getServer();
    }

    public static void consoleLog(String message) {
        if (GemsEconomy.getInstance().isDebug()) {
            StackWalker walker = StackWalker.getInstance();
            Optional<String> walk = walker.walk(frameStream -> frameStream
                    .skip(1)
                    .map(f -> f.getClassName() + ":" + f.getMethodName())
                    .filter(s -> !(s.contains("minecraft") || s.contains("bukkit")))
                    .reduce((e1, e2) -> e1 + " <- " + e2));
            getServer().getConsoleSender().sendMessage(Console_Prefix + colorize(message));
            getServer().getConsoleSender().sendMessage(Console_Prefix + walk.orElse(""));
        }
    }

    public static void consoleLog(Throwable message) {
        getServer().getConsoleSender().sendMessage(Error_Prefix + message);
    }

    private static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}

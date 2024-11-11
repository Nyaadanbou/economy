/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package cc.mewcraft.economy.utils;

import cc.mewcraft.economy.EconomyPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;

public class UtilServer {

    private static final String CONSOLE_PREFIX = "§2[Economy] §f";
    private static final String ERROR_PREFIX = "§c[Economy] §f";

    private static Server getServer() {
        return Bukkit.getServer();
    }

    public static void consoleLog(String message) {
        if (EconomyPlugin.getInstance().isDebug()) {
            // StackWalker walker = StackWalker.getInstance();
            // Optional<String> walk = walker.walk(frameStream -> frameStream
            //         .skip(1)
            //         .map(f -> f.getClassName() + ":" + f.getMethodName())
            //         .filter(s -> !(s.contains("minecraft")))
            //         .reduce((e1, e2) -> e1 + " <- " + e2));
            // getServer().getConsoleSender().sendMessage(Console_Prefix + walk.orElse(""));
            getServer().getConsoleSender().sendMessage(CONSOLE_PREFIX + colorize(message));
        }
    }

    public static void consoleLog(Throwable message) {
        getServer().getConsoleSender().sendMessage(ERROR_PREFIX + message);
    }

    private static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

}

/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package cc.mewcraft.economy.vault;

import cc.mewcraft.economy.EconomyPlugin;
import cc.mewcraft.economy.utils.UtilServer;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;

public class VaultHandler {
    private final EconomyPlugin plugin;
    private VaultHook economy;

    public VaultHandler(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    public void hook() {
        try {
            if (economy == null) {
                economy = new VaultHook();
            }

            plugin.getCurrencyManager().getDefaultCurrency();
            Bukkit.getServicesManager().register(Economy.class, economy, plugin, ServicePriority.Highest);
            UtilServer.consoleLog("Vault link enabled.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unhook() {
        if (economy != null) {
            Bukkit.getServicesManager().unregister(Economy.class, economy);
            economy = null;
        }
    }
}

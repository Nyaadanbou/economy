/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.cheque;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.utils.UtilString;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("deprecation")
public class ChequeManager {

    private final GemsEconomy plugin;
    private final ItemStack chequeBaseItem;

    public ChequeManager(GemsEconomy plugin) {
        this.plugin = plugin;

        ItemStack item = new ItemStack(Material.valueOf(plugin.getConfig().getString("cheque.material")), 1);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(UtilString.colorize(plugin.getConfig().getString("cheque.name")));
        meta.setLore(UtilString.colorize(plugin.getConfig().getStringList("cheque.lore")));
        item.setItemMeta(meta);
        chequeBaseItem = item;
    }

    public @Nullable ItemStack write(String creatorName, Currency currency, double amount) {
        if (!currency.isPayable()) return null;

        List<String> format = new ArrayList<>();
        for (String baseLore : requireNonNull(chequeBaseItem.getItemMeta().getLore())) {
            format.add(baseLore
                    .replace("{value}", currency.format(amount))
                    .replace("{account}", creatorName)
            );
        }
        ItemStack ret = chequeBaseItem.clone();
        ItemMeta meta = ret.getItemMeta();
        meta.setLore(format);
        ChequeStorage storage = new ChequeStorage(creatorName, currency.getSymbolOrEmpty(), amount);
        meta.getPersistentDataContainer().set(ChequeStorage.key, ChequeStorageType.INSTANCE, storage);
        ret.setItemMeta(meta);
        return ret;
    }

    public boolean isValid(ItemStack itemstack) {
        ChequeStorage storage = ChequeStorage.read(itemstack);
        return storage != null && StringUtils.isNotBlank(storage.getCurrency()) && StringUtils.isNotBlank(storage.getIssuer());
    }

    public double getValue(ItemStack itemstack) {
        ChequeStorage storage = ChequeStorage.read(itemstack);
        return storage != null ? storage.getValue() : 0;
    }

    /**
     * @param itemStack - The cheque item
     *
     * @return Currency it represents
     */
    public @Nullable Currency getCurrency(ItemStack itemStack) {
        ChequeStorage storage = ChequeStorage.read(itemStack);
        return storage != null
                ? plugin.getCurrencyManager().getCurrency(storage.getCurrency()) // Might be null if the currency is deleted from database
                : plugin.getCurrencyManager().getDefaultCurrency(); // Should not be null as it was checked during plugin startup
    }

}
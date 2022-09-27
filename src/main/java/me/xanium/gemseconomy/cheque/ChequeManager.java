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
import me.xanium.gemseconomy.file.F;
import me.xanium.gemseconomy.utils.UtilString;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

    public ItemStack write(String creatorName, Currency currency, double amount) {
        if (!currency.isPayable()) return null;

        if (creatorName.equals("CONSOLE")) {
            creatorName = F.consoleName();
        }
        List<String> formatLore = new ArrayList<>();

        for (String baseLore2 : Objects.requireNonNull(chequeBaseItem.getItemMeta().getLore())) {
            formatLore.add(baseLore2.replace("{value}", currency.format(amount)).replace("{player}", creatorName));
        }
        ItemStack ret = chequeBaseItem.clone();
        ItemMeta meta = ret.getItemMeta();
        meta.setLore(formatLore);
        ChequeStorage storage = new ChequeStorage(creatorName, currency.getPlural(), amount);
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
     * @param itemstack - The Cheque.
     * @return - Currency it represents.
     */
    public Currency getCurrency(ItemStack itemstack) {
        ChequeStorage storage = ChequeStorage.read(itemstack);
        return storage != null
                ? plugin.getCurrencyManager().getCurrency(storage.getCurrency())
                : plugin.getCurrencyManager().getDefaultCurrency();
    }
}

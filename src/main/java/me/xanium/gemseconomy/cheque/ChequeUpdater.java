package me.xanium.gemseconomy.cheque;

import de.tr7zw.nbtapi.NBTItem;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.file.F;
import me.xanium.gemseconomy.utils.UtilString;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

public class ChequeUpdater {
    private static final boolean nbtApiInstalled;

    static {
        nbtApiInstalled = Bukkit.getPluginManager().getPlugin("NBTAPI") != null;
    }

    public static void tryApplyFallback(ItemStack stack, ChequeStorage storage){
        if (!nbtApiInstalled) return;
            NBTItem nbtItem = new NBTItem(stack);
            nbtItem.setString("issuer", storage.getIssuer());
            nbtItem.setString("value", String.valueOf(storage.getValue()));
            nbtItem.setString("currency", storage.getCurrency());
            nbtItem.applyNBT(stack);
    }

    public static ChequeStorage tryUpdate(ItemStack stack) {
        if (!nbtApiInstalled) return null;
        try {
            NBTItem nbtItem = new NBTItem(stack);
            String issuer = nbtItem.getString("issuer");
            String value = nbtItem.getString("value");
            String currency = nbtItem.getString("currency");

            if (StringUtils.isEmpty(issuer) && StringUtils.isEmpty(value) && StringUtils.isEmpty(currency)) {
                return null;
            }

            if (StringUtils.isEmpty(issuer)) {
                issuer = F.consoleName();
            }
            if (StringUtils.isEmpty(value)) {
                value = "0.0";
            }
            if (StringUtils.isEmpty(currency)) {
                currency = GemsEconomy.inst().getCurrencyManager().getDefaultCurrency().getPlural();
            }
            return new ChequeStorage(issuer, currency, Double.parseDouble(value));
        } catch (Throwable throwable) {
            return null;
        }
    }
}

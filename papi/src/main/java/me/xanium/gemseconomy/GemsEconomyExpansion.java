package me.xanium.gemseconomy;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.currency.Currency;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class GemsEconomyExpansion extends PlaceholderExpansion {

    private GemsEconomy economy = null;

    @Override
    public boolean register() {
        if (!canRegister()) {
            return false;
        }

        economy = (GemsEconomy) Bukkit.getPluginManager().getPlugin(this.getRequiredPlugin());

        if (economy == null) {
            return false;
        }

        return super.register();
    }

    @Override
    public boolean canRegister() {
        return Bukkit.getPluginManager().getPlugin(this.getRequiredPlugin()) != null;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "gemseconomy";
    }

    @Override
    public @NonNull String getAuthor() {
        return "Xanium";
    }

    @Override
    public @NonNull String getVersion() {
        return "1.6";
    }

    @Override
    public @Nullable String getRequiredPlugin() {
        return "GemsEconomy";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NonNull String s) {
        if (player == null) {
            return "";
        }

        Account account = this.economy.getAccountManager().fetchAccount(player.getUniqueId());
        Currency defCurrency = this.economy.getCurrencyManager().getDefaultCurrency();
        s = s.toLowerCase();

        if (s.equalsIgnoreCase("balance_default")) {
            String amount = "";
            return amount + Math.round(account.getBalance(defCurrency));
        } else if (s.equalsIgnoreCase("balance_default_formatted")) {
            return defCurrency.format(account.getBalance(defCurrency));
        } else if (s.startsWith("balance_") || !s.startsWith("balance_default")) {
            String[] currencyArray = s.split("_");
            Currency currency = this.economy.getCurrencyManager().getCurrency(currencyArray[1]);
            if (s.equalsIgnoreCase("balance_" + currencyArray[1] + "_formatted")) {
                return currency.format(account.getBalance(currency));
            } else {
                String amount = "";
                return amount + Math.round(account.getBalance(currency));
            }
        }

        return null;
    }

}
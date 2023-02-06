package me.xanium.gemseconomy.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.xanium.gemseconomy.GemsEconomy;
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

        economy = (GemsEconomy) Bukkit.getPluginManager().getPlugin(getRequiredPlugin());

        if (economy == null) {
            return false;
        }

        return super.register();
    }

    @Override
    public boolean canRegister() {
        return Bukkit.getPluginManager().getPlugin(getRequiredPlugin()) != null;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "gemseconomy";
    }

    @Override
    public @NonNull String getAuthor() {
        return "Nailm";
    }

    @Override
    public @NonNull String getVersion() {
        return "1.0";
    }

    @Override
    public @NonNull String getRequiredPlugin() {
        return "GemsEconomy";
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer player, @NonNull String params) {
        if (player == null) {
            return "";
        }

        params = params.toLowerCase();
        Currency defaultCurrency = this.economy.getCurrencyManager().getDefaultCurrency();
        Account account = this.economy.getAccountManager().fetchAccount(player.getUniqueId());

        if (account == null)
            return "";

        if (params.equals("balance_default")) {
            return String.valueOf(Math.round(account.getBalance(defaultCurrency)));
        } else if (params.equals("balance_default_formatted")) {
            return defaultCurrency.format(account.getBalance(defaultCurrency));
        } else if (params.startsWith("balance_") || !params.startsWith("balance_default")) {
            String[] currencyArray = params.split("_");
            String currencyId = currencyArray[1];
            Currency currency = this.economy.getCurrencyManager().getCurrency(currencyId);
            if (currency == null)
                return "";
            if (params.equals("balance_" + currencyId + "_formatted")) {
                return currency.format(account.getBalance(currency));
            } else {
                return String.valueOf(Math.round(account.getBalance(currency)));
            }
        } else if (params.startsWith("balance_acc_")) {
            String[] currencyArray = params.split("_");
            String currencyId = currencyArray[2];
            return String.valueOf(account.getAccBalance(currencyId));
        }

        return null;
    }

}
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

    private GemsEconomy economy;

    @Override
    public boolean register() {
        if (!canRegister()) {
            return false;
        }

        this.economy = (GemsEconomy) Bukkit.getPluginManager().getPlugin(getRequiredPlugin());

        if (this.economy == null) {
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
        return "1.1";
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

        if (params.startsWith("cum.balance.formatted.fancy")) { // length of "cum.balance.formatted.fancy": 27
            return parseFancyFormattedCum(defaultCurrency, account, params.substring(27));
        } else if (params.startsWith("cum.balance.formatted")) { // length of "cum.balance.formatted": 21
            return parseSimpleFormattedCum(defaultCurrency, account, params.substring(21));
        } else if (params.startsWith("cum.balance")) { // length of "cum.balance": 11
            return parsePlainCum(defaultCurrency, account, params.substring(11));
        } else if (params.startsWith("balance.formatted.fancy")) { // length of "balance.formatted.fancy": 23
            return parseFancyFormatted(defaultCurrency, account, params.substring(23));
        } else if (params.startsWith("balance.formatted")) { // length of "balance.formatted": 17
            return parseSimpleFormatted(defaultCurrency, account, params.substring(17));
        } else if (params.startsWith("balance")) { // length of "balance": 7
            return parsePlain(defaultCurrency, account, params.substring(7));
        }

        return null;
    }

    private String parsePlain(Currency def, Account acc, String input) {
        // $gemseconomy_balance$
        // $gemseconomy_balance:<currency>$

        if (input.startsWith(":")) {
            String currencyName = input.substring(1);
            Currency currency = this.economy.getCurrencyManager().getCurrency(currencyName);
            return currency != null ? String.valueOf(acc.getBalance(currency)) : "";
        } else {
            return String.valueOf(acc.getBalance(def));
        }
    }

    private String parseSimpleFormatted(Currency def, Account acc, String input) {
        // $gemseconomy_balance.formatted$
        // $gemseconomy_balance.formatted:<currency>$

        if (input.startsWith(":")) {
            String currencyName = input.substring(1);
            Currency currency = this.economy.getCurrencyManager().getCurrency(currencyName);
            return currency != null ? currency.simpleFormat(acc.getBalance(currency)) : "";
        } else {
            return def.simpleFormat(acc.getBalance(def));
        }
    }

    private String parseFancyFormatted(Currency def, Account acc, String input) {
        // $gemseconomy_balance.formatted.fancy$
        // $gemseconomy_balance.formatted.fancy:<currency>$

        if (input.startsWith(":")) {
            String currencyName = input.substring(1);
            Currency currency = this.economy.getCurrencyManager().getCurrency(currencyName);
            return currency != null ? currency.fancyFormat(acc.getBalance(currency)) : "";
        } else {
            return def.fancyFormat(acc.getBalance(def));
        }
    }

    private String parsePlainCum(Currency def, Account acc, String input) {
        // $gemseconomy_cum.balance$
        // $gemseconomy_cum.balance:<currency>$

        if (input.startsWith(":")) {
            String currencyName = input.substring(1);
            Currency currency = this.economy.getCurrencyManager().getCurrency(currencyName);
            return currency != null ? String.valueOf(acc.getCumulativeBalance(currency)) : "";
        } else {
            return String.valueOf(acc.getCumulativeBalance(def));
        }
    }

    private String parseSimpleFormattedCum(Currency def, Account acc, String input) {
        // $gemseconomy_cum.balance.formatted$
        // $gemseconomy_cum.balance.formatted:<currency>$

        if (input.startsWith(":")) {
            String currencyName = input.substring(1);
            Currency currency = this.economy.getCurrencyManager().getCurrency(currencyName);
            return currency != null ? currency.simpleFormat(acc.getCumulativeBalance(currency)) : "";
        } else {
            return def.simpleFormat(acc.getCumulativeBalance(def));
        }
    }

    private String parseFancyFormattedCum(Currency def, Account acc, String input) {
        // $gemseconomy_cum.balance.formatted.fancy$
        // $gemseconomy_cum.balance.formatted.fancy:<currency>$

        if (input.startsWith(":")) {
            String currencyName = input.substring(1);
            Currency currency = this.economy.getCurrencyManager().getCurrency(currencyName);
            return currency != null ? currency.fancyFormat(acc.getCumulativeBalance(currency)) : "";
        } else {
            return def.fancyFormat(acc.getCumulativeBalance(def));
        }
    }

}
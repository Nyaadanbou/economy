package me.xanium.gemseconomy.currency;

import com.google.common.collect.Lists;
import me.xanium.gemseconomy.GemsEconomy;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class CurrencyManager {

    private final GemsEconomy plugin;
    private final List<Currency> currencies = Lists.newArrayList();

    public CurrencyManager(GemsEconomy plugin) {
        this.plugin = plugin;
    }

    public boolean currencyExist(String name) {
        for (Currency currency : currencies) {
            if (currency.getSingular().equalsIgnoreCase(name) || currency.getPlural().equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    public @Nullable Currency getCurrency(String name) {
        for (Currency currency : currencies) {
            if (currency.getSingular().equalsIgnoreCase(name) || currency.getPlural().equalsIgnoreCase(name)) {
                return currency;
            }
        }
        return null;
    }

    public @Nullable Currency getCurrency(UUID uuid) {
        for (Currency currency : currencies) {
            if (!currency.getUuid().equals(uuid)) continue;
            return currency;
        }
        return null;
    }

    public @Nullable Currency getDefaultCurrency() {
        for (Currency currency : currencies) {
            if (!currency.isDefaultCurrency()) continue;
            return currency;
        }
        return null;
    }

    public @Nullable Currency createNewCurrency(String singular, String plural) {
        if (currencyExist(singular) || currencyExist(plural)) {
            return null;
        }

        Currency currency = new Currency(UUID.randomUUID(), singular, plural);
        currency.setExchangeRate(1.0);
        if (currencies.size() == 0) {
            currency.setDefaultCurrency(true);
        }

        add(currency);

        plugin.getDataStore().saveCurrency(currency);

        return currency;
    }

    /**
     * <p>Remove specified currency.
     *
     * <p><b>This will also remove the currency from all accounts!!!</b>
     *
     * @param currency the currency to remove
     */
    public void remove(Currency currency) {
        // Remove this currency from all accounts
        GemsEconomy.inst()
                .getAccountManager()
                .getAllAccounts()
                .stream()
                .filter(account -> account.getBalances().containsKey(currency))
                .forEach(account -> account.getBalances().remove(currency));

        // Remove this currency from this manager
        currencies.remove(currency);

        // Remove this currency from data storage
        plugin.getDataStore().deleteCurrency(currency);
    }

    public void add(Currency currency) {
        if (currencies.contains(currency)) return;
        currencies.add(currency);
    }

    public @NotNull List<Currency> getCurrencies() {
        return currencies;
    }

}

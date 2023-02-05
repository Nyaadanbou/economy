package me.xanium.gemseconomy.currency;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.bungee.UpdateType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@DefaultQualifier(NonNull.class)
public class CurrencyManager {

    private final GemsEconomy plugin;
    private final List<Currency> currencies = new ArrayList<>();

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
            if (!currency.getUuid().equals(uuid))
                continue;
            return currency;
        }
        return null;
    }

    public @NonNull Currency getDefaultCurrency() {
        for (Currency currency : currencies) {
            if (currency.isDefaultCurrency())
                return currency;
        }
        throw new IllegalStateException("No default currency is provided");
    }

    /**
     * Creates a new currency and saves it to database.
     *
     * @param singular the singular form of the new currency
     * @param plural   the plural form of the new currency
     *
     * @return the new currency, or <code>null</code> if already existed
     */
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

        plugin.getDataStore().saveCurrency(currency); // TODO sync creation between servers
        plugin.getUpdateForwarder().sendUpdateMessage(UpdateType.CURRENCY, currency.getUuid().toString());

        return currency;
    }

    public void add(Currency currency) {
        if (!currencies.contains(currency)) {
            currencies.add(currency);
        }
    }

    public void save(Currency currency) {
        plugin.getDataStore().saveCurrency(currency); // TODO sync between servers
        plugin.getUpdateForwarder().sendUpdateMessage(UpdateType.CURRENCY, currency.getUuid().toString());
    }

    /**
     * Clears the balance of specific currency for all Accounts, i.e. set balance to 0.
     *
     * @param currency the currency to clear balance
     */
    public void clear(Currency currency) {
        plugin.getAccountManager().getOfflineAccounts().forEach(account -> {
            account.getBalances().put(currency, 0D);
            plugin.getDataStore().saveAccount(account);
            plugin.getUpdateForwarder().sendUpdateMessage(UpdateType.ACCOUNT, account.getUuid().toString());
        });
    }

    /**
     * Removes specified currency.
     * <p>
     * <b>This will also remove the currency from all accounts!!!</b>
     *
     * @param currency the currency to remove
     */
    public void remove(Currency currency) {
        // Remove this currency from all accounts
        GemsEconomy.getInstance()
            .getAccountManager()
            .getOfflineAccounts()
            .forEach(account -> {
                account.getBalances().remove(currency);
                plugin.getDataStore().saveAccount(account);
                plugin.getUpdateForwarder().sendUpdateMessage(UpdateType.ACCOUNT, account.getUuid().toString()); // TODO sync deletion between servers
            });

        // Remove this currency from this manager
        currencies.remove(currency);

        // Remove this currency from data storage
        plugin.getDataStore().deleteCurrency(currency);
        plugin.getUpdateForwarder().sendUpdateMessage(UpdateType.CURRENCY, currency.getUuid().toString());
    }

    public List<Currency> getCurrencies() {
        return currencies;
    }

}

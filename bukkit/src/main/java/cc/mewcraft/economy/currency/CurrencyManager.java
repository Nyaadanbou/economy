package cc.mewcraft.economy.currency;

import cc.mewcraft.economy.EconomyPlugin;
import cc.mewcraft.economy.api.Currency;
import cc.mewcraft.economy.message.Action;
import cc.mewcraft.economy.message.Messenger;

import com.google.common.collect.ImmutableList;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class CurrencyManager {

    private final EconomyPlugin plugin;
    private final Map<UUID, Currency> currencies;

    public CurrencyManager(EconomyPlugin plugin) {
        this.plugin = plugin;
        currencies = new ConcurrentHashMap<>();
    }

    /* ---------------- Getters ---------------- */


    public boolean hasCurrency(String name) {
        return getCurrency(name) != null;
    }

    public @Nullable Currency getCurrency(UUID uuid) {
        return currencies.get(uuid);
    }

    public @Nullable Currency getCurrency(String name) {
        for (Currency currency : currencies.values()) {
            if (currency.getName().equalsIgnoreCase(name)) {
                return currency;
            }
        }
        return null;
    }

    public Currency getDefaultCurrency() {
        for (Currency currency : currencies.values()) {
            if (currency.isDefaultCurrency())
                return currency;
        }
        throw new IllegalStateException("No default currency is provided");
    }

    public List<Currency> getLoadedCurrencies() {
        return ImmutableList.copyOf(currencies.values());
    }

    /* ---------------- Setters ---------------- */

    /**
     * Creates a new currency and saves it to database.
     *
     * @param name the name of the new currency
     * @return the new currency, or <code>null</code> if already existed
     */
    public @Nullable Currency createCurrency(String name) {
        if (hasCurrency(name)) {
            return null;
        }

        Currency currency = new ServerCurrency(UUID.randomUUID(), name);
        currency.setExchangeRate(1D);

        if (currencies.isEmpty()) {
            currency.setDefaultCurrency(true);
        }

        addCurrency(currency);

        plugin.getDataStore().saveCurrency(currency);
        plugin.getMessenger().sendMessage(Action.CREATE_CURRENCY, currency.getUuid());

        return currency;
    }

    /**
     * Adds given currency object to this manager.
     * <p>
     * This method will do nothing if this manager already contains specific currency.
     *
     * @param currency a currency object
     */
    public void addCurrency(Currency currency) {
        currencies.putIfAbsent(currency.getUuid(), currency);
    }

    /**
     * Saves specific currency to database.
     *
     * @param currency a currency
     */
    public void saveCurrency(Currency currency) {
        plugin.getDataStore().saveCurrency(currency);
        plugin.getMessenger().sendMessage(Action.UPDATE_CURRENCY, currency.getUuid());
    }

    /**
     * Updates specific currency in this manager so that it syncs with database.
     * <p>
     * This method is specifically used by {@link Messenger}.
     *
     * @param uuid   the uuid of specific currency
     * @param create if true, it will create specific currency if not existing in this manager; otherwise false
     */
    public void updateCurrency(UUID uuid, boolean create) {
        @Nullable Currency newCurrency = plugin.getDataStore().loadCurrency(uuid);
        @Nullable Currency oldCurrency = getCurrency(uuid);
        if (newCurrency != null) { // Only update it if the new currency is actually loaded
            if (oldCurrency != null) { // This manager has specific currency, but not synced with database
                oldCurrency.update(newCurrency);
            } else if (create) { // This manager doesn't have specific currency - just create it
                addCurrency(newCurrency);
            }
        }
    }

    /**
     * Removes specified currency from this manager, all accounts, and database.
     *
     * @param currency the currency to remove
     */
    public void removeCurrency(Currency currency) {
        // Remove this currency from all accounts
        EconomyPlugin.getInstance()
                .getAccountManager()
                .getOfflineAccounts()
                .forEach(account -> {
                    account.getBalances().remove(currency);
                    plugin.getDataStore().saveAccount(account);
                    plugin.getMessenger().sendMessage(Action.UPDATE_ACCOUNT, account.getUuid());
                });

        // Remove this currency from this manager
        currencies.remove(currency.getUuid());

        // Remove this currency from data storage
        plugin.getDataStore().deleteCurrency(currency);
        plugin.getMessenger().sendMessage(Action.DELETE_CURRENCY, currency.getUuid());

        // Flush accounts in cache
        plugin.getAccountManager().flushAccounts();
    }

    /**
     * The same as {@link #removeCurrency(Currency)} but it accepts a UUID.
     * <p>
     * If the UUID does not map to a currency in this manager, this method will do nothing.
     */
    public void removeCurrency(UUID uuid) {
        Currency currency = currencies.get(uuid);
        if (currency != null)
            removeCurrency(currency);
    }

    /**
     * Sets the balances of specific currency to default value for <b>ALL</b> accounts.
     *
     * @param currency the currency to clear balance
     */
    public void clearBalance(Currency currency) {
        plugin.getAccountManager().getOfflineAccounts().forEach(account -> {
            account.getBalances().compute(currency, (c, d) -> c.getDefaultBalance());
            plugin.getDataStore().saveAccount(account);
            plugin.getMessenger().sendMessage(Action.UPDATE_ACCOUNT, account.getUuid());
        });

        // Flush accounts in cache
        plugin.getAccountManager().flushAccounts();
    }

}

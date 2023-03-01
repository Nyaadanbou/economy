package me.xanium.gemseconomy.currency;

import com.google.common.collect.ImmutableList;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.message.Action;
import me.xanium.gemseconomy.message.Messenger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@DefaultQualifier(NonNull.class)
public class CurrencyManager {

    private final GemsEconomy plugin;
    private final Map<UUID, Currency> currencies;

    public CurrencyManager(GemsEconomy plugin) {
        this.plugin = plugin;
        this.currencies = new ConcurrentHashMap<>();
    }

    /* ---------------- Getters ---------------- */

    @Deprecated
    public boolean currencyExist(String name) {
        return getCurrency(name) != null;
    }

    public boolean hasCurrency(String name) {
        return getCurrency(name) != null;
    }

    public @Nullable Currency getCurrency(UUID uuid) {
        return this.currencies.get(uuid);
    }

    public @Nullable Currency getCurrency(String name) {
        for (Currency currency : this.currencies.values()) {
            if (currency.getName().equalsIgnoreCase(name)) {
                return currency;
            }
        }
        return null;
    }

    public Currency getDefaultCurrency() {
        for (Currency currency : this.currencies.values()) {
            if (currency.isDefaultCurrency())
                return currency;
        }
        throw new IllegalStateException("No default currency is provided");
    }

    public List<Currency> getCurrencies() {
        return ImmutableList.copyOf(this.currencies.values());
    }

    /* ---------------- Setters ---------------- */

    /**
     * Creates a new Currency and saves it to database.
     *
     * @param name the name of the new Currency
     *
     * @return the new Currency, or <code>null</code> if already existed
     */
    public @Nullable Currency createCurrency(String name) {
        if (hasCurrency(name)) {
            return null;
        }

        Currency currency = new Currency(UUID.randomUUID(), name);
        currency.setExchangeRate(1.0);

        if (this.currencies.size() == 0) {
            currency.setDefaultCurrency(true);
        }

        addCurrency(currency);

        this.plugin.getDataStore().saveCurrency(currency);
        this.plugin.getMessenger().sendMessage(Action.CREATE_CURRENCY, currency.getUuid());

        return currency;
    }

    /**
     * Adds given Currency object to this manager.
     * <p>
     * If this manager already contains specific Currency, this method will do nothing.
     *
     * @param currency a Currency object
     */
    public void addCurrency(Currency currency) {
        this.currencies.putIfAbsent(currency.getUuid(), currency);
    }

    /**
     * Saves specific currency to database.
     *
     * @param currency a Currency
     */
    public void saveCurrency(Currency currency) {
        this.plugin.getDataStore().saveCurrency(currency);
        this.plugin.getMessenger().sendMessage(Action.UPDATE_CURRENCY, currency.getUuid());
    }

    /**
     * Updates specific Currency in this manager so that it syncs with database.
     * <p>
     * This method is specifically used by {@link Messenger}.
     *
     * @param uuid   the uuid of specific Currency
     * @param create if true, it will create specific currency if not existing in this manager; otherwise false
     */
    public void updateCurrency(UUID uuid, boolean create) {
        @Nullable Currency newCurrency = this.plugin.getDataStore().loadCurrency(uuid);
        @Nullable Currency oldCurrency = getCurrency(uuid);
        if (newCurrency != null) // only do something if the new currency is actually loaded
            if (oldCurrency != null) {
                oldCurrency.update(newCurrency); // This manager has specific Currency, but not synced with database
            } else if (create) {
                this.currencies.put(uuid, newCurrency); // This manager doesn't have specific Currency - just create it
            }
    }

    /**
     * Removes specified Currency from this manager, all Accounts, and database.
     *
     * @param currency the Currency to remove
     */
    public void removeCurrency(Currency currency) {
        // Remove this currency from all accounts
        GemsEconomy.getInstance()
            .getAccountManager()
            .getOfflineAccounts()
            .forEach(account -> {
                account.getBalances().remove(currency);
                this.plugin.getDataStore().saveAccount(account);
                this.plugin.getMessenger().sendMessage(Action.UPDATE_ACCOUNT, account.getUuid());
                this.plugin.getAccountManager().flushAccount(account.getUuid());
            });

        // Remove this currency from this manager
        this.currencies.remove(currency.getUuid());

        // Remove this currency from data storage
        this.plugin.getDataStore().deleteCurrency(currency);
        this.plugin.getMessenger().sendMessage(Action.DELETE_CURRENCY, currency.getUuid());
    }

    /**
     * The same as {@link #removeCurrency(Currency)} but it accepts a UUID.
     * <p>
     * If the UUID does not map to a Currency in this manager, this method will do nothing.
     */
    public void removeCurrency(UUID uuid) {
        Currency currency = this.currencies.get(uuid);
        if (currency != null)
            removeCurrency(currency);
    }

    /**
     * Sets the balances of specific Currency to default value for <b>ALL</b> Accounts.
     *
     * @param currency the Currency to clear balance
     */
    public void clearBalance(Currency currency) {
        this.plugin.getAccountManager().getOfflineAccounts().forEach(account -> {
            account.getBalances().compute(currency, (c, d) -> c.getDefaultBalance());
            this.plugin.getDataStore().saveAccount(account);
            this.plugin.getMessenger().sendMessage(Action.UPDATE_ACCOUNT, account.getUuid());
            this.plugin.getAccountManager().flushAccount(account.getUuid());
        });
    }

}

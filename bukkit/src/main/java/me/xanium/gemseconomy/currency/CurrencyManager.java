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

    public boolean currencyExist(String name) {
        return getCurrency(name) != null;
    }

    public @Nullable Currency getCurrency(String name) {
        for (Currency currency : this.currencies.values()) {
            if (currency.getSingular().equalsIgnoreCase(name)) {
                return currency;
            }
        }
        return null;
    }

    public @Nullable Currency getCurrency(UUID uuid) {
        return this.currencies.get(uuid);
    }

    public @NonNull Currency getDefaultCurrency() {
        for (Currency currency : this.currencies.values()) {
            if (currency.isDefaultCurrency())
                return currency;
        }
        throw new IllegalStateException("No default currency is provided");
    }

    /**
     * Creates a new Currency and saves it to database.
     *
     * @param singular the singular form of the new Currency
     *
     * @return the new Currency, or <code>null</code> if already existed
     */
    public @Nullable Currency createCurrency(String singular) {
        if (currencyExist(singular)) {
            return null;
        }

        Currency currency = new Currency(UUID.randomUUID(), singular);
        currency.setExchangeRate(1.0);

        if (this.currencies.size() == 0) {
            currency.setDefaultCurrency(true);
        }

        addCurrencyIfAbsent(currency);

        this.plugin.getDataStore().saveCurrency(currency);
        this.plugin.getMessenger().sendMessage(Action.CREATE_CURRENCY, currency.getUuid());

        return currency;
    }

    /**
     * Adds given Currency object to memory.
     * <p>
     * If this manager already contains specific Currency, this method will do nothing.
     *
     * @param currency a Currency
     */
    public void addCurrencyIfAbsent(Currency currency) {
        this.currencies.putIfAbsent(currency.getUuid(), currency);
    }

    /**
     * Loads specific Currency from database, overriding any in memory.
     *
     * @param uuid the uuid of specific Currency
     */
    public void loadCurrencyOverride(UUID uuid) {
        @Nullable Currency updated = this.plugin.getDataStore().loadCurrency(uuid);
        if (updated != null) {
            this.currencies.put(updated.getUuid(), updated);
        }
    }

    /**
     * Updates specific Currency in the memory so that it syncs with database.
     * <p>
     * This method is specifically used by {@link Messenger}.
     *
     * @param uuid the uuid of specific Currency
     */
    public void updateCurrency(UUID uuid) {
        @Nullable Currency updated = this.plugin.getDataStore().loadCurrency(uuid);
        @Nullable Currency loaded = this.currencies.get(uuid);
        if (updated != null && loaded != null) {
            loaded.update(updated); // This manager has specific Currency, but not synced with database
        }
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
     * Clears the balance of specific Currency for <b>ALL</b> Accounts, i.e. set balance to 0.
     *
     * @param currency the Currency to clear balance
     */
    public void clearBalance(Currency currency) {
        this.plugin.getAccountManager().getOfflineAccounts().forEach(account -> {
            account.getBalances().put(currency, 0D);
            this.plugin.getDataStore().saveAccount(account);
            this.plugin.getMessenger().sendMessage(Action.UPDATE_ACCOUNT, account.getUuid());
        });
    }

    /**
     * Removes specified Currency.
     * <p>
     * This will also remove the Currency from <b>ALL</b> Accounts!
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
            });

        // Remove this currency from this manager
        this.currencies.remove(currency.getUuid());

        // Remove this currency from data storage
        this.plugin.getDataStore().deleteCurrency(currency);
        this.plugin.getMessenger().sendMessage(Action.DELETE_CURRENCY, currency.getUuid());
    }

    public void removeCurrency(UUID uuid) {
        Currency currency = this.currencies.get(uuid);
        if (currency != null)
            removeCurrency(currency);
    }

    public List<Currency> getCurrencies() {
        return ImmutableList.copyOf(this.currencies.values());
    }

}

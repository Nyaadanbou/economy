package me.xanium.gemseconomy.currency;

import com.google.common.collect.ImmutableList;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.message.Action;
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
        for (Currency currency : currencies.values()) {
            if (currency.getSingular().equalsIgnoreCase(name) || currency.getPlural().equalsIgnoreCase(name)) {
                return currency;
            }
        }
        return null;
    }

    public @Nullable Currency getCurrency(UUID uuid) {
        return currencies.get(uuid);
    }

    public @NonNull Currency getDefaultCurrency() {
        for (Currency currency : currencies.values()) {
            if (currency.isDefaultCurrency())
                return currency;
        }
        throw new IllegalStateException("No default currency is provided");
    }

    /**
     * Creates a new Currency and saves it to database.
     *
     * @param singular the singular form of the new Currency
     * @param plural   the plural form of the new Currency
     *
     * @return the new Currency, or <code>null</code> if already existed
     */
    public @Nullable Currency createCurrency(String singular, String plural) {
        if (currencyExist(singular) || currencyExist(plural)) {
            return null;
        }

        Currency currency = new Currency(UUID.randomUUID(), singular, plural);
        currency.setExchangeRate(1.0);

        if (currencies.size() == 0) {
            currency.setDefaultCurrency(true);
        }

        addCurrencyIfAbsent(currency);

        plugin.getDataStore().saveCurrency(currency);
        plugin.getUpdateForwarder().sendMessage(Action.CREATE_CURRENCY, currency.getUuid());

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
        currencies.putIfAbsent(currency.getUuid(), currency);
    }

    /**
     * Loads specific Currency from database, overriding any in memory.
     *
     * @param uuid the uuid of specific Currency
     */
    public void loadCurrencyOverride(UUID uuid) {
        @Nullable Currency updated = plugin.getDataStore().loadCurrency(uuid);
        if (updated != null) {
            currencies.put(updated.getUuid(), updated);
        }
    }

    /**
     * Updates specific Currency in the memory so that it syncs with database.
     * <p>
     * This method is specifically used by {@link me.xanium.gemseconomy.message.MessageForwarder}.
     *
     * @param uuid the uuid of specific Currency
     */
    public void updateCurrency(UUID uuid) {
        @Nullable Currency updated = plugin.getDataStore().loadCurrency(uuid);
        @Nullable Currency loaded = currencies.get(uuid);
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
        plugin.getDataStore().saveCurrency(currency);
        plugin.getUpdateForwarder().sendMessage(Action.UPDATE_CURRENCY, currency.getUuid());
    }

    /**
     * Clears the balance of specific Currency for <b>ALL</b> Accounts, i.e. set balance to 0.
     *
     * @param currency the Currency to clear balance
     */
    public void clearBalance(Currency currency) {
        plugin.getAccountManager().getOfflineAccounts().forEach(account -> {
            account.getBalances().put(currency, 0D);
            plugin.getDataStore().saveAccount(account);
            plugin.getUpdateForwarder().sendMessage(Action.UPDATE_ACCOUNT, account.getUuid());
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
                plugin.getDataStore().saveAccount(account);
                plugin.getUpdateForwarder().sendMessage(Action.UPDATE_ACCOUNT, account.getUuid());
            });

        // Remove this currency from this manager
        currencies.remove(currency.getUuid());

        // Remove this currency from data storage
        plugin.getDataStore().deleteCurrency(currency);
        plugin.getUpdateForwarder().sendMessage(Action.DELETE_CURRENCY, currency.getUuid());
    }

    public void removeCurrency(UUID uuid) {
        Currency currency = currencies.get(uuid);
        if (currency != null)
            removeCurrency(currency);
    }

    public List<Currency> getCurrencies() {
        return ImmutableList.copyOf(currencies.values());
    }

}

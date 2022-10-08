/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */
package me.xanium.gemseconomy.data;

import com.google.common.base.Preconditions;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.currency.CachedTopListEntry;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.utils.OfflineModeProfiles;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import me.xanium.gemseconomy.utils.UtilServer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class YamlStorage extends DataStorage {

    private YamlConfiguration configuration;
    private final File file;

    public YamlStorage(File file) {
        super(StorageType.YAML, false);
        this.file = file;
    }

    @Override
    public void initialize() {
        if (!getFile().exists()) {
            try {
                if (getFile().createNewFile()) {
                    UtilServer.consoleLog("Data file created.");
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        configuration = new YamlConfiguration();
        try {
            configuration.load(getFile());
        } catch (IOException | InvalidConfigurationException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void close() {

    }

    @Override
    public void loadCurrencies() {
        ConfigurationSection section = getConfig().getConfigurationSection("currencies");
        if (section != null) {
            Set<String> currencies = section.getKeys(false);
            for (String uuid : currencies) {
                String path = "currencies." + uuid;
                String single = Objects.requireNonNull(getConfig().getString(path + ".singular"), path + ".singular");
                String plural = Objects.requireNonNull(getConfig().getString(path + ".plural"), path + ".plural");

                String rawColor = Objects.requireNonNull(getConfig().getString(path + ".color"), path + ".color");
                TextColor color = Objects.requireNonNullElse(TextColor.fromHexString(rawColor), NamedTextColor.WHITE);
                boolean decimalSupported = getConfig().getBoolean(path + ".decimalsupported");
                double defaultBalance = getConfig().getDouble(path + ".defaultbalance");
                double maxBalance = getConfig().getDouble(path + ".maxbalance");
                boolean defaultCurrency = getConfig().getBoolean(path + ".defaultcurrency");
                boolean payable = getConfig().getBoolean(path + ".payable");
                String symbol = getConfig().getString(path + ".symbol");
                double exchangeRate = getConfig().getDouble(path + ".exchange_rate");

                Currency currency = new Currency(UUID.fromString(uuid), single, plural);
                currency.setColor(color);
                currency.setDecimalSupported(decimalSupported);
                currency.setDefaultBalance(defaultBalance);
                currency.setMaxBalance(maxBalance);
                currency.setDefaultCurrency(defaultCurrency);
                currency.setPayable(payable);
                currency.setSymbol(symbol);
                currency.setExchangeRate(exchangeRate);
                plugin.getCurrencyManager().add(currency);

                UtilServer.consoleLog("Loaded currency: %s (default_balance: %s, max_balance: %s, default_currency: %s, payable: %s, color: %s)"
                        .formatted(currency.getSingular(), currency.getDefaultBalance(), currency.getMaxBalance(),
                                currency.isDefaultCurrency(), currency.isPayable(), currency.getColor()));
            }
        }
    }

    @Override
    public void saveCurrency(Currency currency) {
        String path = "currencies." + currency.getUuid();
        getConfig().set(path + ".singular", currency.getSingular());
        getConfig().set(path + ".plural", currency.getPlural());
        getConfig().set(path + ".defaultbalance", currency.getDefaultBalance());
        getConfig().set(path + ".maxbalance", currency.getMaxBalance());
        getConfig().set(path + ".symbol", currency.getSymbol());
        getConfig().set(path + ".decimalsupported", currency.isDecimalSupported());
        getConfig().set(path + ".defaultcurrency", currency.isDefaultCurrency());
        getConfig().set(path + ".payable", currency.isPayable());
        getConfig().set(path + ".color", currency.getColor().asHexString());
        getConfig().set(path + ".exchange_rate", currency.getExchangeRate());
        try {
            getConfig().save(getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCurrency(Currency currency) {
        String path = "currencies." + currency.getUuid();
        getConfig().set(path, null);
        try {
            getConfig().save(getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void getTopList(Currency currency, int offset, int amount, Callback<LinkedList<CachedTopListEntry>> callback) {
        throw new UnsupportedOperationException("YAML does not support Top Lists!");
    }

    @Override
    public ArrayList<Account> getOfflineAccounts() {
        ConfigurationSection section = getConfig().getConfigurationSection("accounts");
        Preconditions.checkNotNull(section, "section");
        ArrayList<Account> accounts = new ArrayList<>();
        for (String uuid : section.getKeys(false)) {
            Account acc = loadAccount(UUID.fromString(uuid));
            accounts.add(acc);
        }
        return accounts;
    }

    @Override
    public Account createAccount(Account account) {
        return saveAccount(account);
    }

    @Override
    public Account loadAccount(String name) {
        ConfigurationSection section = getConfig().getConfigurationSection("accounts");
        if (section != null) {
            Set<String> uuidSet = section.getKeys(false);
            if (!uuidSet.isEmpty()) {
                for (String uuid : uuidSet) {
                    String path = "accounts." + uuid;
                    String nick = getConfig().getString(path + ".nickname");
                    if (nick != null && nick.equalsIgnoreCase(name)) {
                        Account account = new Account(UUID.fromString(uuid), nick);
                        account.setCanReceiveCurrency(getConfig().getBoolean(path + ".payable"));
                        loadBalances(account);
                        return account;
                    }
                }
            }
        }
        UUID uuid = OfflineModeProfiles.getUniqueId(name);
        Account account = new Account(uuid, name);
        return createAccount(account);
    }

    @Override
    public Account loadAccount(UUID uuid) {
        String path = "accounts." + uuid.toString();
        String nick = getConfig().getString(path + ".nickname");
        if (nick != null) {
            Account account = new Account(uuid, nick);
            account.setCanReceiveCurrency(getConfig().getBoolean(path + ".payable"));
            loadBalances(account);
            return account;
        }
        Account account = new Account(uuid, Bukkit.getOfflinePlayer(uuid).getName());
        return createAccount(account);
    }

    @Override
    public void loadAccount(UUID uuid, Callback<Account> callback) {
        Account account = this.loadAccount(uuid);
        SchedulerUtils.run(() -> callback.call(account));
    }

    @Override
    public void loadAccount(String name, Callback<Account> callback) {
        Account account = this.loadAccount(name);
        SchedulerUtils.run(() -> callback.call(account));
    }

    @Override
    public Account saveAccount(Account account) {
        String path = "accounts." + account.getUuid().toString();
        getConfig().set(path + ".nickname", account.getNickname());
        getConfig().set(path + ".uuid", account.getUuid().toString());
        for (Currency currency : account.getBalances().keySet()) {
            double balance = account.getBalance(currency);
            getConfig().set(path + ".balances." + currency.getUuid(), balance);
        }
        getConfig().set(path + ".payable", account.canReceiveCurrency());
        try {
            getConfig().save(getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return account;
    }

    @Override
    public void deleteAccount(Account account) {
        String path = "accounts." + account.getUuid().toString();
        getConfig().set(path, null);
        try {
            getConfig().save(getFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateCurrencyLocally(Currency currency) {
        throw new UnsupportedOperationException("YAML does not support updates. Only READ/WRITE");
    }

    private void loadBalances(Account account) {
        String path1 = "accounts." + account.getUuid().toString();
        ConfigurationSection section = getConfig().getConfigurationSection(path1 + ".balances");
        if (section != null) {
            Set<String> balances = section.getKeys(false);
            if (!balances.isEmpty()) {
                for (String currency1 : balances) {
                    String path2 = path1 + ".balances." + currency1;
                    double balance = getConfig().getDouble(path2);

                    Currency currency2 = plugin.getCurrencyManager().getCurrency(UUID.fromString(currency1));
                    if (currency2 != null) {
                        // cap the amount
                        balance = Math.min(balance, currency2.getMaxBalance());
                        account.modifyBalance(currency2, balance, false);
                    }
                }
            }
        }
    }

    public YamlConfiguration getConfig() {
        return configuration;
    }

    public File getFile() {
        return file;
    }
}


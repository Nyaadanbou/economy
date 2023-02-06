/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy;

import me.lucko.helper.plugin.ExtendedJavaPlugin;
import me.xanium.gemseconomy.account.AccountManager;
import me.xanium.gemseconomy.api.GemsEconomyAPI;
import me.xanium.gemseconomy.bungee.UpdateForwarder;
import me.xanium.gemseconomy.cheque.ChequeManager;
import me.xanium.gemseconomy.command.CommandManager;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.currency.CurrencyManager;
import me.xanium.gemseconomy.data.DataStorage;
import me.xanium.gemseconomy.data.MySQLStorage;
import me.xanium.gemseconomy.data.StorageType;
import me.xanium.gemseconomy.listeners.EconomyListener;
import me.xanium.gemseconomy.logging.EconomyLogger;
import me.xanium.gemseconomy.utils.UtilServer;
import me.xanium.gemseconomy.vault.VaultHandler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.logging.Level;

import static java.util.Objects.requireNonNull;

public class GemsEconomy extends ExtendedJavaPlugin {

    private static GemsEconomy INSTANCE;

    private GemsMessages messages;
    private BukkitAudiences audiences;
    private GemsEconomyAPI api;
    private DataStorage dataStorage = null;
    private AccountManager accountManager;
    private ChequeManager chequeManager;
    private CurrencyManager currencyManager;
    private VaultHandler vaultHandler;
    private EconomyLogger economyLogger;
    private UpdateForwarder updateForwarder;

    private boolean debug = false;
    private boolean vault = true;
    private boolean logging = false;
    private boolean cheques = false;
    private boolean disabling = false;

    public static GemsEconomy getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unused")
    public static GemsEconomyAPI getAPI() {
        if (INSTANCE.api == null) {
            INSTANCE.api = new GemsEconomyAPI();
        }
        return INSTANCE.api;
    }

    public static GemsMessages lang() {
        return INSTANCE.messages;
    }

    @Override
    public void load() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();

        debug = getConfig().getBoolean("debug");
        vault = getConfig().getBoolean("vault");
        logging = getConfig().getBoolean("transaction_log");
        cheques = getConfig().getBoolean("cheque.enabled");
    }

    @Override
    public void enable() {
        INSTANCE = this;

        audiences = bind(BukkitAudiences.create(this));

        messages = new GemsMessages(this);
        accountManager = new AccountManager(this);
        currencyManager = new CurrencyManager(this);
        economyLogger = new EconomyLogger(this);
        updateForwarder = new UpdateForwarder(this);

        initializeDataStore(StorageType.valueOf(requireNonNull(getConfig().getString("storage")).toUpperCase()));

        if (currencyManager.getCurrencies().stream().noneMatch(Currency::isDefaultCurrency)) {
            getServer().getPluginManager().disablePlugin(this);
            throw new IllegalStateException("No default currency is provided");
        }

        bind(registerListener(new EconomyListener()));

        if (isVault()) {
            vaultHandler = new VaultHandler(this);
            vaultHandler.hook();
        } else {
            UtilServer.consoleLog("Vault link is disabled.");
        }

        if (isLogging()) {
            getEconomyLogger().save();
        }

        if (isChequesEnabled()) {
            chequeManager = new ChequeManager(this);
        }

        try {
            new CommandManager(this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize commands", e);
            setEnabled(false);
        }
    }

    @Override
    public void disable() {
        disabling = true;

        if (isVault())
            getVaultHandler().unhook();

        if (getDataStore() != null)
            getDataStore().close();
    }

    public void reloadLanguages() {
        messages = new GemsMessages(this);
    }

    public void reloadConfiguration() {
        // TODO support reloading config.yml
    }

    public void reloadDataStorage() {
        // TODO support reloading data storage
    }

    public BukkitAudiences getAudiences() {
        return audiences;
    }

    public DataStorage getDataStore() {
        return dataStorage;
    }

    public CurrencyManager getCurrencyManager() {
        return currencyManager;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public VaultHandler getVaultHandler() {
        return vaultHandler;
    }

    public EconomyLogger getEconomyLogger() {
        return economyLogger;
    }

    public ChequeManager getChequeManager() {
        return chequeManager;
    }

    public UpdateForwarder getUpdateForwarder() {
        return updateForwarder;
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isVault() {
        return vault;
    }

    public boolean isLogging() {
        return logging;
    }

    public boolean isDisabling() {
        return disabling;
    }

    public boolean isChequesEnabled() {
        return cheques;
    }

    private void initializeDataStore(@Nullable StorageType strategy) {
        DataStorage.getMethods().add(new MySQLStorage(
            requireNonNull(getConfig().getString("mysql.host")),
            getConfig().getInt("mysql.port", 3306),
            requireNonNull(getConfig().getString("mysql.database")),
            requireNonNull(getConfig().getString("mysql.username")),
            requireNonNull(getConfig().getString("mysql.password"))
        ));

        if (strategy != null) {
            dataStorage = DataStorage.getMethod(strategy);
        } else {
            UtilServer.consoleLog("§cNo valid storage method provided.");
            UtilServer.consoleLog("§cCheck your files, then try again.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            UtilServer.consoleLog("Initializing data store \"" + getDataStore().getStorageType() + "\"...");
            getDataStore().initialize();

            UtilServer.consoleLog("Loading currencies...");
            getDataStore().loadCurrencies();
            UtilServer.consoleLog("Loaded " + getCurrencyManager().getCurrencies().size() + " currencies!");
        } catch (Throwable e) {
            UtilServer.consoleLog("§cCannot load initial data from data storage.");
            UtilServer.consoleLog("§cCheck your files, then try again.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

}

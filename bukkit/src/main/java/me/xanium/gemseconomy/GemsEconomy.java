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
import me.xanium.gemseconomy.command.CommandManager;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.currency.CurrencyManager;
import me.xanium.gemseconomy.data.DataStorage;
import me.xanium.gemseconomy.data.MySQLStorage;
import me.xanium.gemseconomy.data.StorageType;
import me.xanium.gemseconomy.listener.EconomyListener;
import me.xanium.gemseconomy.logging.EconomyLogger;
import me.xanium.gemseconomy.message.MessageForwarder;
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
    private CurrencyManager currencyManager;
    private VaultHandler vaultHandler;
    private EconomyLogger economyLogger;
    private MessageForwarder updateForwarder;

    private boolean debug = false;
    private boolean vault = true;
    private boolean logging = false;
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
        INSTANCE = this;

        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();

        this.debug = getConfig().getBoolean("debug");
        this.vault = getConfig().getBoolean("vault");
        this.logging = getConfig().getBoolean("transaction_log");

        this.messages = new GemsMessages(this);
        this.accountManager = new AccountManager(this);
        this.currencyManager = new CurrencyManager(this);
        this.economyLogger = new EconomyLogger(this);
        this.updateForwarder = MessageForwarder.get();

        initializeDataStore(StorageType.valueOf(requireNonNull(getConfig().getString("storage")).toUpperCase()));

        if (this.currencyManager.getCurrencies().stream().noneMatch(Currency::isDefaultCurrency)) {
            this.logger.severe("No default currency is provided");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (isVault()) {
            this.vaultHandler = new VaultHandler(this);
            this.vaultHandler.hook();
        } else {
            UtilServer.consoleLog("Vault link is disabled.");
        }
    }

    @Override
    public void enable() {
        this.audiences = bind(BukkitAudiences.create(this));

        registerListener(new EconomyListener()).bindWith(this);

        if (isLogging())
            getEconomyLogger().save();

        try {
            new CommandManager(this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize commands", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void disable() {
        this.disabling = true;

        if (isVault())
            getVaultHandler().unhook();

        if (getDataStore() != null)
            getDataStore().close();
    }

    public void reloadLanguages() {
        this.messages = new GemsMessages(this);
    }

    public void reloadConfiguration() {
        // TODO support reloading config.yml
    }

    public void reloadDataStorage() {
        // TODO support reloading data storage
    }

    public BukkitAudiences getAudiences() {
        return this.audiences;
    }

    public DataStorage getDataStore() {
        return this.dataStorage;
    }

    public CurrencyManager getCurrencyManager() {
        return this.currencyManager;
    }

    public AccountManager getAccountManager() {
        return this.accountManager;
    }

    public VaultHandler getVaultHandler() {
        return this.vaultHandler;
    }

    public EconomyLogger getEconomyLogger() {
        return this.economyLogger;
    }

    public MessageForwarder getUpdateForwarder() {
        return this.updateForwarder;
    }

    public boolean isDebug() {
        return this.debug;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isVault() {
        return this.vault;
    }

    public boolean isLogging() {
        return this.logging;
    }

    public boolean isDisabling() {
        return this.disabling;
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
            this.dataStorage = DataStorage.getMethod(strategy);
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

/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy;

import me.xanium.gemseconomy.account.AccountManager;
import me.xanium.gemseconomy.api.GemsEconomyAPI;
import me.xanium.gemseconomy.bungee.UpdateForwarder;
import me.xanium.gemseconomy.cheque.ChequeManager;
import me.xanium.gemseconomy.command.GemsCommands;
import me.xanium.gemseconomy.currency.CurrencyManager;
import me.xanium.gemseconomy.data.DataStorage;
import me.xanium.gemseconomy.data.MySQLStorage;
import me.xanium.gemseconomy.data.StorageType;
import me.xanium.gemseconomy.listeners.EconomyListener;
import me.xanium.gemseconomy.logging.EconomyLogger;
import me.xanium.gemseconomy.utils.UtilServer;
import me.xanium.gemseconomy.vault.VaultHandler;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

import static java.util.Objects.requireNonNull;

public class GemsEconomy extends JavaPlugin {

    private static GemsEconomy instance;

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
        return instance;
    }

    public static GemsMessages lang() {
        return instance.messages;
    }

    @SuppressWarnings("unused")
    public static GemsEconomyAPI getAPI() {
        if (instance.api == null) {
            instance.api = new GemsEconomyAPI();
        }
        return instance.api;
    }

    @Override
    public void onLoad() {
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        reloadConfig();

        debug = getConfig().getBoolean("debug");
        vault = getConfig().getBoolean("vault");
        logging = getConfig().getBoolean("transaction_log");
        cheques = getConfig().getBoolean("cheque.enabled");
    }

    @Override
    public void onEnable() {
        instance = this;

        audiences = BukkitAudiences.create(this);
        messages = new GemsMessages(this);
        accountManager = new AccountManager(this);
        currencyManager = new CurrencyManager(this);
        economyLogger = new EconomyLogger(this);
        updateForwarder = new UpdateForwarder(this);

        initializeDataStore(StorageType.valueOf(requireNonNull(getConfig().getString("storage")).trim().toUpperCase()), true);

        getServer().getPluginManager().registerEvents(new EconomyListener(), this);

        if (isVault()) {
            vaultHandler = new VaultHandler(this);
            vaultHandler.hook();
        } else {
            UtilServer.consoleLog("Vault link is disabled.");
        }

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", updateForwarder);

        if (isLogging()) {
            getEconomyLogger().save();
        }

        if (isChequesEnabled()) {
            chequeManager = new ChequeManager(this);
        }

        try {
            new GemsCommands(this);
        } catch (Exception e) {
            getLogger().log(Level.SEVERE, "Failed to initialize commands", e);
            this.setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        disabling = true;

        if (audiences != null) {
            audiences.close();
            audiences = null;
        }

        if (isVault()) getVaultHandler().unhook();

        if (getDataStore() != null) {
            getDataStore().close();
        }
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

    private void initializeDataStore(StorageType strategy, boolean load) {
        DataStorage.getMethods().add(new MySQLStorage(
            getConfig().getString("mysql.host"),
            getConfig().getInt("mysql.port"),
            getConfig().getString("mysql.database"),
            getConfig().getString("mysql.username"),
            getConfig().getString("mysql.password")
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

            if (load) {
                UtilServer.consoleLog("Loading currencies...");
                getDataStore().loadCurrencies();
                UtilServer.consoleLog("Loaded " + getCurrencyManager().getCurrencies().size() + " currencies!");
            }
        } catch (Throwable e) {
            UtilServer.consoleLog("§cCannot load initial data from data storage.");
            UtilServer.consoleLog("§cCheck your files, then try again.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

}

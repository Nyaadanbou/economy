/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy;

import me.xanium.gemseconomy.bungee.UpdateForwarder;
import me.xanium.gemseconomy.commands.*;
import me.xanium.gemseconomy.data.DataStore;
import me.xanium.gemseconomy.data.MySQLStorage;
import me.xanium.gemseconomy.data.SQLiteDataStore;
import me.xanium.gemseconomy.data.YamlStorage;
import me.xanium.gemseconomy.economy.AccountManager;
import me.xanium.gemseconomy.economy.ChequeManager;
import me.xanium.gemseconomy.file.Configuration;
import me.xanium.gemseconomy.listeners.EconomyListener;
import me.xanium.gemseconomy.logging.EcoLogger;
import me.xanium.gemseconomy.logging.EconomyLogger;
import me.xanium.gemseconomy.logging.ILogger;
import me.xanium.gemseconomy.nbt.NMSVersion;
import me.xanium.gemseconomy.utils.Metrics;
import me.xanium.gemseconomy.utils.Updater;
import me.xanium.gemseconomy.utils.UtilServer;
import me.xanium.gemseconomy.vault.VaultHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class GemsEconomy extends JavaPlugin {

    private static DataStore dataStore = null;
    private static GemsEconomy instance;
    private ChequeManager chequeManager;
    private VaultHandler vaultHandler;
    private NMSVersion nmsVersion;
    private Metrics metrics;
    private ILogger economyLogger;
    private UpdateForwarder updateForwarder;

    private boolean debug = false;
    private boolean vault = false;
    private boolean logging = false;

    private boolean disabling = false;

    /**
     * Changes:
     * Fixed the Cache, I made a cache for accounts but didn't actually use it.
     * So this would bring a performance boost regarding account lookups. (Data reading)
     * Fixed a money dupe bug when a player joins first time, receives money and can pay infinite until they rejoin.
     *
     *
     * Added support for 1.14!
     *
     * Please let me know if you find bugs!
     * PM me or contact me on discord!
     *
     */


    /**
     * Todo List:
     *
     *
     */

    @Override
    public void onLoad(){
        Configuration configuration = new Configuration(this);
        configuration.loadDefaultConfig();

        setDebug(getConfig().getBoolean("debug"));
        setVault(getConfig().getBoolean("vault"));
        setLogging(getConfig().getBoolean("transaction_log"));
    }

    @Override
    public void onEnable(){
        instance = this;

        nmsVersion = new NMSVersion();
        chequeManager = new ChequeManager(this);
        economyLogger = new EcoLogger(this);
        metrics = new Metrics(this);
        updateForwarder = new UpdateForwarder();

        initializeDataStore(getConfig().getString("storage"), true);

        getServer().getPluginManager().registerEvents(new EconomyListener(), this);
        getCommand("balance").setExecutor(new BalanceCommand());
        getCommand("baltop").setExecutor(new BalanceTopCommand());
        getCommand("economy").setExecutor(new EconomyCommand());
        getCommand("pay").setExecutor(new PayCommand());
        getCommand("currency").setExecutor(new CurrencyCommand());
        getCommand("cheque").setExecutor(new ChequeCommand());
        getCommand("exchange").setExecutor(new ExchangeCommand());

        if(isVault()){
            vaultHandler = new VaultHandler(this);
            vaultHandler.hook();
            UtilServer.consoleLog("Vault compatibility enabled.");
        }else{
            UtilServer.consoleLog("Vault compatibility is disabled.");
        }

        this.getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "BungeeCord", updateForwarder);

        if(isLogging()) {
            getEconomyLogger().save();
        }

        doAsync(() -> checkForUpdate());
    }

    @Override
    public void onDisable() {
        disabling = true;

        if(isVault()) getVaultHandler().unhook();
        if(isLogging()) EconomyLogger.closeLog();

        if (GemsEconomy.getDataStore() != null) {
            GemsEconomy.getDataStore().close();
        }
    }

    public void initializeDataStore(String strategy, boolean load) {

        DataStore.getMethods().add(new YamlStorage(new File(getDataFolder(), "data.yml")));
        DataStore.getMethods().add(new MySQLStorage(getConfig().getString("mysql.host"), getConfig().getInt("mysql.port"), getConfig().getString("mysql.database"), getConfig().getString("mysql.username"), getConfig().getString("mysql.password")));
        DataStore.getMethods().add(new SQLiteDataStore(new File(getDataFolder(), getConfig().getString("sqlite.file"))));

        if(strategy != null){
            dataStore = DataStore.getMethod(strategy);
        }else{
            UtilServer.consoleLog("§cNo valid storage method provided.");
            UtilServer.consoleLog("§cCheck your files, then try again.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        try {
            UtilServer.consoleLog("Initializing data store \"" + getDataStore().getName() + "\"...");
            getDataStore().initialize();

            if(load) {
                UtilServer.consoleLog("Loading currencies...");
                getDataStore().loadCurrencies();
                UtilServer.consoleLog("Loaded " + AccountManager.getCurrencies().size() + " currencies!");
            }
        } catch (Throwable e) {
            UtilServer.consoleLog("§cCannot load initial data from DataStore.");
            UtilServer.consoleLog("§cCheck your files, then try again.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    private void checkForUpdate() {
        Updater updater = new Updater(this);
        try {
            if (updater.checkForUpdates()) {
                UtilServer.consoleLog("-------------------------------------------");
                UtilServer.consoleLog("New Version: " + updater.getNewVersion());
                UtilServer.consoleLog("Current Version: " + updater.getCurrentVersion());
                UtilServer.consoleLog("Download link: " + updater.getResourceURL());
                UtilServer.consoleLog("--------------------------------------------");
            }
        } catch (IOException e) {
            UtilServer.consoleLog("Could not check for updates! Error log will follow if debug is enabled.");
            if(isDebug()) {
                UtilServer.consoleLog(e.getCause());
            }
        }
    }

    public static void doAsync(Runnable runnable){
        getInstance().getServer().getScheduler().runTaskAsynchronously(getInstance(), runnable);
    }

    public static void doSync(Runnable runnable){
        getInstance().getServer().getScheduler().runTask(getInstance(), runnable);
    }

    public static DataStore getDataStore() {
        return dataStore;
    }

    public static GemsEconomy getInstance() {
        return instance;
    }

    public boolean isDebug() {
        return debug;
    }

    private void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isVault() {
        return vault;
    }

    private void setVault(boolean vault) {
        this.vault = vault;
    }

    public boolean isLogging() {
        return logging;
    }

    public void setLogging(boolean logging) {
        this.logging = logging;
    }

    public VaultHandler getVaultHandler() {
        return vaultHandler;
    }

    public ILogger getEconomyLogger() {
        return economyLogger;
    }

    public NMSVersion getNmsVersion() {
        return nmsVersion;
    }

    public Metrics getMetrics() {
        return metrics;
    }

    public ChequeManager getChequeManager() {
        return chequeManager;
    }

    public boolean isDisabling() {
        return disabling;
    }

    public UpdateForwarder getUpdateForwarder() {
        return updateForwarder;
    }
}

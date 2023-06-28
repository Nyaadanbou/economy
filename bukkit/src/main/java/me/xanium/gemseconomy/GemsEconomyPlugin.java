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
import me.xanium.gemseconomy.api.Currency;
import me.xanium.gemseconomy.api.GemsEconomy;
import me.xanium.gemseconomy.api.GemsEconomyImpl;
import me.xanium.gemseconomy.api.GemsEconomyProvider;
import me.xanium.gemseconomy.command.CommandManager;
import me.xanium.gemseconomy.currency.BalanceTopRepository;
import me.xanium.gemseconomy.currency.CurrencyManager;
import me.xanium.gemseconomy.data.DataStorage;
import me.xanium.gemseconomy.data.MySQLStorage;
import me.xanium.gemseconomy.data.StorageType;
import me.xanium.gemseconomy.listener.EconomyListener;
import me.xanium.gemseconomy.logging.EconomyLogger;
import me.xanium.gemseconomy.message.Messenger;
import me.xanium.gemseconomy.utils.UtilServer;
import me.xanium.gemseconomy.vault.VaultHandler;
import org.bukkit.plugin.ServicePriority;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.logging.Level;

import static java.util.Objects.requireNonNull;

public class GemsEconomyPlugin extends ExtendedJavaPlugin {

    private static GemsEconomyPlugin INSTANCE;

    private GemsMessages messages;
    private DataStorage dataStorage;
    private AccountManager accountManager;
    private CurrencyManager currencyManager;
    private BalanceTopRepository balanceTopRepository;
    private VaultHandler vaultHandler;
    private EconomyLogger economyLogger;
    private Messenger messenger;

    private boolean debug = false;
    private boolean vault = true;
    private boolean logging = false;
    private boolean disabling = false;

    public static GemsEconomyPlugin getInstance() {
        return INSTANCE;
    }

    public static GemsMessages lang() {
        return INSTANCE.messages;
    }

    @Override
    public void load() {
        INSTANCE = this;

        saveDefaultConfig();
        reloadConfig();

        debug = getConfig().getBoolean("debug");
        vault = getConfig().getBoolean("vault");
        logging = getConfig().getBoolean("transaction_log");

        messages = new GemsMessages(this);
        accountManager = new AccountManager(this);
        currencyManager = new CurrencyManager(this);
        economyLogger = new EconomyLogger(this);
        balanceTopRepository = new BalanceTopRepository(this);

        initializeDataStore(StorageType.valueOf(requireNonNull(getConfig().getString("storage")).toUpperCase()));

        if (currencyManager.getCurrencies().stream().noneMatch(Currency::isDefaultCurrency)) {
            getLogger().severe("No default currency is provided");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Provides API instances with the providers
        GemsEconomyImpl instance = new GemsEconomyImpl(this);
        getServer().getServicesManager().register(GemsEconomy.class, instance, this, ServicePriority.Normal);
        GemsEconomyProvider.register(instance);

        if (isVault()) {
            vaultHandler = new VaultHandler(this);
            vaultHandler.hook();
        } else {
            UtilServer.consoleLog("Vault link is disabled.");
        }
    }

    @Override
    public void enable() {
        this.messenger = bind(Messenger.get());

        registerListener(new EconomyListener()).bindWith(this);

        if (isLogging()) getEconomyLogger().save();

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

        GemsEconomyProvider.unregister();
        getServer().getServicesManager().unregisterAll(this);

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

    public DataStorage getDataStore() {
        return this.dataStorage;
    }

    public BalanceTopRepository getBalanceTopRepository() {
        return this.balanceTopRepository;
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

    public Messenger getMessenger() {
        return this.messenger;
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

            UtilServer.consoleLog("Loading currencies from database...");
            getDataStore().loadCurrencies().forEach(currency -> getCurrencyManager().addCurrency(currency));
            UtilServer.consoleLog("Loaded " + getCurrencyManager().getCurrencies().size() + " currencies!");
        } catch (Throwable e) {
            UtilServer.consoleLog("§cCannot load initial data from data storage.");
            UtilServer.consoleLog("§cCheck your files, then try again.");
            e.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
        }
    }

}

/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package cc.mewcraft.economy;

import cc.mewcraft.economy.api.EconomyImpl;
import me.lucko.helper.plugin.ExtendedJavaPlugin;
import cc.mewcraft.economy.account.AccountManager;
import cc.mewcraft.economy.api.Currency;
import cc.mewcraft.economy.api.Economy;
import cc.mewcraft.economy.api.EconomyProvider;
import cc.mewcraft.economy.command.CommandManager;
import cc.mewcraft.economy.currency.BalanceTopRepository;
import cc.mewcraft.economy.currency.CurrencyManager;
import cc.mewcraft.economy.data.DataStorage;
import cc.mewcraft.economy.data.MySQLStorage;
import cc.mewcraft.economy.data.StorageType;
import cc.mewcraft.economy.hook.MiniPlaceholderExpansion;
import cc.mewcraft.economy.hook.PAPIPlaceholderExpansion;
import cc.mewcraft.economy.listener.EconomyListener;
import cc.mewcraft.economy.logging.EconomyLogger;
import cc.mewcraft.economy.message.Messenger;
import cc.mewcraft.economy.utils.UtilServer;
import cc.mewcraft.economy.vault.VaultHandler;
import org.bukkit.plugin.ServicePriority;

import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;

public class EconomyPlugin extends ExtendedJavaPlugin {

    private static EconomyPlugin INSTANCE;

    private EconomyMessages messages;
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

    public static EconomyPlugin getInstance() {
        return INSTANCE;
    }

    public static EconomyMessages lang() {
        return INSTANCE.messages;
    }

    @Override
    public void load() {
        INSTANCE = this;

        // Save default config (if there is none) and load it
        saveDefaultConfig();
        reloadConfig();

        debug = getConfig().getBoolean("debug");
        vault = getConfig().getBoolean("vault");
        logging = getConfig().getBoolean("transaction_log");

        // Initialize managers
        messages = new EconomyMessages(this);
        accountManager = new AccountManager(this);
        currencyManager = new CurrencyManager(this);
        balanceTopRepository = new BalanceTopRepository(this);
        economyLogger = new EconomyLogger(this);

        // Initialize data source
        initializeDataStore(StorageType.valueOf(requireNonNull(getConfig().getString("storage")).toUpperCase()));

        // Check if default currency is set
        if (currencyManager.getLoadedCurrencies().stream().noneMatch(Currency::isDefaultCurrency)) {
            getLogger().severe("No default currency is provided");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize API instances
        EconomyImpl instance = new EconomyImpl(this);
        getServer().getServicesManager().register(Economy.class, instance, this, ServicePriority.Normal);
        EconomyProvider.register(instance);

        // Register Vault hook
        if (isVault()) {
            vaultHandler = new VaultHandler(this);
            vaultHandler.hook();
        } else {
            UtilServer.consoleLog("Vault link is disabled.");
        }
    }

    @Override
    public void enable() {
        // Initialize messenger
        messenger = bind(Messenger.get());

        // Register listeners
        registerTerminableListener(new EconomyListener()).bindWith(this);

        // Register placeholder expansions
        if (isPluginPresent("PlaceholderAPI"))
            bind(new PAPIPlaceholderExpansion()).register();
        if (isPluginPresent("MiniPlaceholders"))
            bind(new MiniPlaceholderExpansion()).register();

        // Initialize logger
        if (isLogging())
            getEconomyLogger().save();

        // Initialize commands
        try {
            new CommandManager(this);
        } catch (Exception e) {
            getSLF4JLogger().error("Failed to initialize commands", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void disable() {
        disabling = true;

        // Unregister API instances
        EconomyProvider.unregister();
        getServer().getServicesManager().unregisterAll(this);

        // Unregister Vault hook
        if (isVault())
            getVaultHandler().unhook();

        // Close data source
        if (getDataStore() != null)
            getDataStore().close();
    }

    public void reloadLanguages() {
        messages = new EconomyMessages(this);
    }

    public void reloadConfiguration() {
        // TODO support reloading config.yml
    }

    public void reloadDataStorage() {
        // TODO support reloading data storage
    }

    public DataStorage getDataStore() {
        return dataStorage;
    }

    public BalanceTopRepository getBalanceTopRepository() {
        return balanceTopRepository;
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

    public Messenger getMessenger() {
        return messenger;
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

            UtilServer.consoleLog("Loading currencies from database...");
            getDataStore().loadCurrencies().forEach(currency -> getCurrencyManager().addCurrency(currency));
            UtilServer.consoleLog("Loaded " + getCurrencyManager().getLoadedCurrencies().size() + " currencies!");
        } catch (Throwable e) {
            e.printStackTrace();
            UtilServer.consoleLog("§cCannot load initial data from data storage.");
            UtilServer.consoleLog("§cCheck your files, then try again.");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

}

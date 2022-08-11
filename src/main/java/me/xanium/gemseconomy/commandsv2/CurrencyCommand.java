package me.xanium.gemseconomy.commandsv2;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.StringTooltip;
import dev.jorel.commandapi.arguments.*;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.commandsv2.argument.BaseArguments;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.data.DataStorage;
import me.xanium.gemseconomy.data.StorageType;
import me.xanium.gemseconomy.file.F;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import me.xanium.gemseconomy.utils.UtilServer;
import me.xanium.gemseconomy.utils.UtilString;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;

public class CurrencyCommand {

    private static final String NAME = "currency";
    private static final String PERM_CURRENCY = "gemseconomy.command.currency";

    public CurrencyCommand() {
        // Arguments
        final Argument<String> singleArgument = new StringArgument("单数符号").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(
                StringTooltip.of("R", "货币的单数符号")));
        final Argument<String> pluralArgument = new StringArgument("复数符号").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(
                StringTooltip.of("R", "货币的复数符号")));
        final Argument<String> startBalArgument = new StringArgument("初始余额").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(
                StringTooltip.of("250", "新账户的初始余额")));
        final Argument<String> maxBalArgument = new StringArgument("最大余额").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(
                StringTooltip.of("10000", "账户的最大余额")));
        final Argument<String> symbolArgument = new StringArgument("符号").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(
                StringTooltip.of("R", "货币的符号")));
        final Argument<Double> rateArgument = new DoubleArgument("汇率").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(
                StringTooltip.of("1.0", "货币的汇率")));
        final Argument<String> methodArgument = new StringArgument("存储").replaceSuggestions(ArgumentSuggestions.stringsWithTooltips(
                StringTooltip.of("MySQL", "使用 MySQL 作为数据储存方式"),
                StringTooltip.of("YAML", "使用 YAML 作为数据储存方式")
        ));

        // Commands
        new CommandAPICommand(NAME)
                .withPermission(PERM_CURRENCY)
                .withSubcommand(new CommandAPICommand("create")
                        .withArguments(singleArgument, pluralArgument)
                        .executes((sender, args) -> {
                            SchedulerUtils.runAsync(() -> {
                                String single = (String) args[0];
                                String plural = (String) args[1];
                                if (GemsEconomy.inst().getCurrencyManager().currencyExist(single) || GemsEconomy.inst().getCurrencyManager().currencyExist(plural)) {
                                    sender.sendMessage(F.prefix() + "§cCurrency already exists.");
                                    return;
                                }
                                GemsEconomy.inst().getCurrencyManager().createNewCurrency(single, plural);
                                sender.sendMessage(F.prefix() + "§7Created currency: §a" + single);
                            });
                        }))
                .withSubcommand(new CommandAPICommand("list")
                        .executes((sender, args) -> {
                            sender.sendMessage(F.prefix() + "§7There are §f" + GemsEconomy.inst().getCurrencyManager().getCurrencies().size() + "§7 currencies.");
                            for (Currency currency : GemsEconomy.inst().getCurrencyManager().getCurrencies()) {
                                sender.sendMessage("§a§l>> §e" + currency.getSingular());
                            }
                        }))
                .withSubcommand(new CommandAPICommand("view")
                        .withArguments(BaseArguments.CURRENCY)
                        .executes((sender, args) -> {
                            Currency currency = GemsEconomy.inst().getCurrencyManager().getCurrency(args[0].toString());
                            if (currency != null) {
                                sender.sendMessage(F.prefix() + "§7ID: §c" + currency.getUuid().toString());
                                sender.sendMessage(F.prefix() + "§7Singular: §a" + currency.getSingular() + "§7, Plural: §a" + currency.getPlural());
                                sender.sendMessage(F.prefix() + "§7Start Balance: " + currency.getColor() + currency.format(currency.getDefaultBalance()) + "§7.");
                                sender.sendMessage(F.prefix() + "§7Max Balance: " + currency.getColor() + currency.format(currency.getMaxBalance()) + "§7.");
                                sender.sendMessage(F.prefix() + "§7Decimals: " + (currency.isDecimalSupported() ? "§aYes" : "§cNo"));
                                sender.sendMessage(F.prefix() + "§7Default: " + (currency.isDefaultCurrency() ? "§aYes" : "§cNo"));
                                sender.sendMessage(F.prefix() + "§7Payable: " + (currency.isPayable() ? "§aYes" : "§cNo"));
                                sender.sendMessage(F.prefix() + "§7Color: " + currency.getColor() + currency.getColor().name());
                                sender.sendMessage(F.prefix() + "§7Rate: " + currency.getColor() + currency.getExchangeRate());
                            } else {
                                sender.sendMessage(F.unknownCurrency());
                            }
                        }))
                .withSubcommand(new CommandAPICommand("startbal")
                        .withArguments(BaseArguments.CURRENCY)
                        .withArguments(startBalArgument)
                        .executes((sender, args) -> {
                            SchedulerUtils.runAsync(() -> {
                                Currency currency = GemsEconomy.inst().getCurrencyManager().getCurrency(args[0].toString());
                                if (currency != null) {
                                    double amount;
                                    block76:
                                    {
                                        if (currency.isDecimalSupported()) {
                                            try {
                                                amount = Double.parseDouble(args[1].toString());
                                                if (amount <= 0.0) {
                                                    throw new NumberFormatException();
                                                }
                                                break block76;
                                            } catch (NumberFormatException ex) {
                                                sender.sendMessage(F.invalidAmount());
                                                return;
                                            }
                                        }
                                        try {
                                            amount = Integer.parseInt(args[1].toString());
                                            if (amount <= 0.0) {
                                                throw new NumberFormatException();
                                            }
                                        } catch (NumberFormatException ex) {
                                            sender.sendMessage(F.invalidAmount());
                                            return;
                                        }
                                    }
                                    currency.setDefaultBalance(amount);
                                    sender.sendMessage(F.prefix() + "§7Starting balance for §f" + currency.getPlural() + " §7set: §a" + UtilString.format(currency.getDefaultBalance()));
                                    GemsEconomy.inst().getDataStore().saveCurrency(currency);
                                } else {
                                    sender.sendMessage(F.unknownCurrency());
                                }
                            });
                        }))
                .withSubcommand(new CommandAPICommand("maxbal")
                        .withArguments(BaseArguments.CURRENCY)
                        .withArguments(maxBalArgument)
                        .executes((sender, args) -> {
                            SchedulerUtils.runAsync(() -> {
                                Currency currency = GemsEconomy.inst().getCurrencyManager().getCurrency(args[0].toString());
                                if (currency != null) {
                                    double amount;
                                    block77:
                                    {
                                        if (currency.isDecimalSupported()) {
                                            try {
                                                amount = Double.parseDouble(args[1].toString());
                                                if (amount < 0.0) {
                                                    throw new NumberFormatException();
                                                }
                                                break block77;
                                            } catch (NumberFormatException ex) {
                                                sender.sendMessage(F.invalidAmount());
                                                return;
                                            }
                                        }
                                        try {
                                            amount = Integer.parseInt(args[1].toString());
                                            if (amount < 0.0) {
                                                throw new NumberFormatException();
                                            }
                                        } catch (NumberFormatException ex) {
                                            sender.sendMessage(F.invalidAmount());
                                            return;
                                        }
                                    }
                                    currency.setMaxBalance(amount);
                                    sender.sendMessage(F.prefix() + "§7Maximum balance for §f" + currency.getPlural() + " §7set: §a" + UtilString.format(currency.getMaxBalance()));
                                    GemsEconomy.inst().getDataStore().saveCurrency(currency);
                                } else {
                                    sender.sendMessage(F.unknownCurrency());
                                }
                            });
                        }))
                .withSubcommand(new CommandAPICommand("color")
                        .withArguments(BaseArguments.CURRENCY)
                        .withArguments(new ChatColorArgument("color"))
                        .executes((sender, args) -> {
                            SchedulerUtils.runAsync(() -> {
                                Currency currency = GemsEconomy.inst().getCurrencyManager().getCurrency(args[0].toString());
                                if (currency != null) {
                                    try {
                                        ChatColor color = (ChatColor) args[1];
                                        if (color.isFormat()) {
                                            throw new UnsupportedOperationException();
                                        }
                                        currency.setColor(color);
                                        sender.sendMessage(F.prefix() + "§7Color for §f" + currency.getPlural() + " §7updated: " + color + color.name());
                                        GemsEconomy.inst().getDataStore().saveCurrency(currency);
                                    } catch (UnsupportedOperationException ex) {
                                        sender.sendMessage(F.prefix() + "§cInvalid chat color.");
                                    }
                                } else {
                                    sender.sendMessage(F.unknownCurrency());
                                }
                            });
                        }))
                .withSubcommand(new CommandAPICommand("colorlist")
                        .executes((sender, args) -> {
                            sender.sendMessage("§0§lBLACK §7= black");
                            sender.sendMessage("§1§lDARK BLUE §7= dark_blue");
                            sender.sendMessage("§2§lDARK GREEN §7= dark_green");
                            sender.sendMessage("§3§lDARK AQUA §7= dark_aqua");
                            sender.sendMessage("§4§lDARK RED §7= dark_red");
                            sender.sendMessage("§5§lDARK PURPLE §7= dark_purple");
                            sender.sendMessage("§6§lGOLD §7= gold");
                            sender.sendMessage("§7§lGRAY §7= gray");
                            sender.sendMessage("§8§lDARK GRAY §7= dark_gray");
                            sender.sendMessage("§9§lBLUE §7= blue");
                            sender.sendMessage("§a§lGREEN §7= green");
                            sender.sendMessage("§b§lAQUA §7= aqua");
                            sender.sendMessage("§c§lRED §7= red");
                            sender.sendMessage("§d§lLIGHT PURPLE §7= light_purple");
                            sender.sendMessage("§e§lYELLOW §7= yellow");
                            sender.sendMessage("§f§lWHITE §7= white|reset");
                        }))
                .withSubcommand(new CommandAPICommand("symbol")
                        .withArguments(BaseArguments.CURRENCY)
                        .withArguments(symbolArgument)
                        .executes((sender, args) -> {
                            SchedulerUtils.runAsync(() -> {
                                Currency currency = GemsEconomy.inst().getCurrencyManager().getCurrency(args[0].toString());
                                if (currency != null) {
                                    String symbol = args[1].toString();
                                    if (symbol.equalsIgnoreCase("remove")) {
                                        currency.setSymbol(null);
                                        sender.sendMessage(F.prefix() + "§7Currency symbol removed for §f" + currency.getPlural());
                                        GemsEconomy.inst().getDataStore().saveCurrency(currency);
                                    } else if (symbol.length() == 1) {
                                        currency.setSymbol(symbol);
                                        sender.sendMessage(F.prefix() + "§7Currency symbol for §f" + currency.getPlural() + " §7updated: §a" + symbol);
                                        GemsEconomy.inst().getDataStore().saveCurrency(currency);
                                    } else {
                                        sender.sendMessage(F.prefix() + "§7Symbol must be 1 character, or remove it with \"remove\".");
                                    }
                                } else {
                                    sender.sendMessage(F.unknownCurrency());
                                }
                            });
                        }))
                .withSubcommand(new CommandAPICommand("default")
                        .withArguments(BaseArguments.CURRENCY)
                        .executes((sender, args) -> {
                            SchedulerUtils.runAsync(() -> {
                                Currency currency = GemsEconomy.inst().getCurrencyManager().getCurrency(args[0].toString());
                                if (currency != null) {
                                    Currency c = GemsEconomy.inst().getCurrencyManager().getDefaultCurrency();
                                    if (c != null) {
                                        c.setDefaultCurrency(false);
                                        GemsEconomy.inst().getDataStore().saveCurrency(c);
                                    }
                                    currency.setDefaultCurrency(true);
                                    sender.sendMessage(F.prefix() + "§7Set default currency to §f" + currency.getPlural());
                                    GemsEconomy.inst().getDataStore().saveCurrency(currency);
                                } else {
                                    sender.sendMessage(F.unknownCurrency());
                                }
                            });
                        }))
                .withSubcommand(new CommandAPICommand("payable")
                        .withArguments(BaseArguments.CURRENCY)
                        .executes((sender, args) -> {
                            SchedulerUtils.runAsync(() -> {
                                Currency currency = GemsEconomy.inst().getCurrencyManager().getCurrency(args[0].toString());
                                if (currency != null) {
                                    currency.setPayable(!currency.isPayable());
                                    sender.sendMessage(F.prefix() + "§7Toggled payability for §f" + currency.getPlural() + "§7: " + (currency.isPayable() ? "§aYes" : "§cNo"));
                                    GemsEconomy.inst().getDataStore().saveCurrency(currency);
                                } else {
                                    sender.sendMessage(F.unknownCurrency());
                                }
                            });
                        }))
                .withSubcommand(new CommandAPICommand("decimals")
                        .withArguments(BaseArguments.CURRENCY)
                        .executes((sender, args) -> {
                            SchedulerUtils.runAsync(() -> {
                                Currency currency = GemsEconomy.inst().getCurrencyManager().getCurrency(args[0].toString());
                                if (currency != null) {
                                    currency.setDecimalSupported(!currency.isDecimalSupported());
                                    sender.sendMessage(F.prefix() + "§7Toggled Decimal Support for §f" + currency.getPlural() + "§7: " + (currency.isDecimalSupported() ? "§aYes" : "§cNo"));
                                    GemsEconomy.inst().getDataStore().saveCurrency(currency);
                                } else {
                                    sender.sendMessage(F.unknownCurrency());
                                }
                            });
                        }))
                .withSubcommand(new CommandAPICommand("delete")
                        .withArguments(BaseArguments.CURRENCY)
                        .executes((sender, args) -> {
                            SchedulerUtils.runAsync(() -> {
                                Currency currency = GemsEconomy.inst().getCurrencyManager().getCurrency(args[0].toString());
                                if (currency != null) {
                                    GemsEconomy.inst().getAccountManager().getAccounts().stream().filter(account -> account.getBalances().containsKey(currency)).forEach(account -> account.getBalances().remove(currency));
                                    GemsEconomy.inst().getDataStore().deleteCurrency(currency);
                                    GemsEconomy.inst().getCurrencyManager().getCurrencies().remove(currency);
                                    sender.sendMessage(F.prefix() + "§7Deleted currency: §a" + currency.getPlural());
                                } else {
                                    sender.sendMessage(F.unknownCurrency());
                                }
                            });
                        }))
                .withSubcommand(new CommandAPICommand("setrate")
                        .withArguments(BaseArguments.CURRENCY)
                        .withArguments(rateArgument)
                        .executes((sender, args) -> {
                            SchedulerUtils.runAsync(() -> {
                                Currency currency = GemsEconomy.inst().getCurrencyManager().getCurrency(args[0].toString());
                                if (currency != null) {
                                    double amount;

                                    try {
                                        amount = Double.parseDouble((String) args[1]);
                                        if (amount <= 0.0) {
                                            throw new NumberFormatException();
                                        }
                                    } catch (NumberFormatException ex) {
                                        sender.sendMessage(F.invalidAmount());
                                        return;
                                    }
                                    currency.setExchangeRate(amount);
                                    GemsEconomy.inst().getDataStore().saveCurrency(currency);
                                    sender.sendMessage(F.exchangeRateSet().replace("{currencycolor}", "" + currency.getColor()).replace("{currency}", currency.getPlural()).replace("{amount}", String.valueOf(amount)));
                                } else {
                                    sender.sendMessage(F.unknownCurrency());
                                }
                            });
                        }))
                .withSubcommand(new CommandAPICommand("convert")
                        .withArguments(methodArgument)
                        .executes((sender, args) -> {
                            SchedulerUtils.runAsync(() -> {
                                String methodArg = args[0].toString();
                                StorageType method = StorageType.valueOf(methodArg.trim().toUpperCase());
                                DataStorage current = GemsEconomy.inst().getDataStore();
                                DataStorage given = DataStorage.getMethod(method);

                                if (current == null) {
                                    sender.sendMessage(F.prefix() + "§7Current Data Store is null. Did something go wrong on startup?");
                                    return;
                                }

                                if (given != null) {
                                    if (current.getStorageType() == given.getStorageType()) {
                                        sender.sendMessage(F.prefix() + "§7You can't convert to the same datastore.");
                                        return;
                                    }

                                    GemsEconomy.inst().getConfig().set("storage", given.getStorageType());
                                    GemsEconomy.inst().saveConfig();

                                    sender.sendMessage(F.prefix() + "§aLoading data..");
                                    GemsEconomy.inst().getAccountManager().getAccounts().clear();

                                    sender.sendMessage(F.prefix() + "§aStored accounts.");
                                    ArrayList<Account> offline = new ArrayList<>(GemsEconomy.inst().getDataStore().getOfflineAccounts());
                                    UtilServer.consoleLog("Stored Accounts: " + offline.size());
                                    if (GemsEconomy.inst().isDebug()) {
                                        for (Account account : offline) {
                                            UtilServer.consoleLog("Account: " + account.getNickname() + " (" + account.getUuid().toString() + ")");
                                            for (Currency currency : account.getBalances().keySet()) {
                                                UtilServer.consoleLog("Balance: " + currency.format(account.getBalance(currency)));
                                            }
                                        }
                                    }

                                    ArrayList<Currency> currencies = new ArrayList<>(GemsEconomy.inst().getCurrencyManager().getCurrencies());
                                    sender.sendMessage(F.prefix() + "§aStored currencies.");
                                    GemsEconomy.inst().getCurrencyManager().getCurrencies().clear();

                                    if (GemsEconomy.inst().isDebug()) {
                                        for (Currency c : currencies) {
                                            UtilServer.consoleLog("Currency: " + c.getSingular() + " (" + c.getPlural() + "): " + c.format(1000000));
                                        }
                                    }

                                    sender.sendMessage(F.prefix() + "§aSwitching from §f" + current.getStorageType() + " §ato §f" + given.getStorageType() + "§a.");

                                    if (given.getStorageType() == StorageType.YAML) {
                                        SchedulerUtils.run(() -> {
                                            File data = new File(GemsEconomy.inst().getDataFolder() + File.separator + "data.yml");
                                            if (data.exists()) {
                                                data.delete();
                                            }
                                        });
                                    }

                                    if (GemsEconomy.inst().getDataStore() != null) {
                                        GemsEconomy.inst().getDataStore().close();

                                        sender.sendMessage(F.prefix() + "§aDataStore is closed. Plugin is essentially dead now.");
                                    }

                                    GemsEconomy.inst().initializeDataStore(given.getStorageType(), false);
                                    try {
                                        Thread.sleep(2000);
                                    } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                    }

                                    sender.sendMessage(F.prefix() + "§aInitialized " + given.getStorageType() + " Data Store. Check console for wrong username/password if using mysql.");
                                    sender.sendMessage(F.prefix() + "§aIf there are sql login errors, you can just retry after you have fixed the credentials, changed the datastore back to what you were using and restarted the server!");

                                    if (GemsEconomy.inst().getDataStore().getStorageType() != null) {
                                        for (Currency c : currencies) {
                                            Currency newCurrency = new Currency(c.getUuid(), c.getSingular(), c.getPlural());
                                            newCurrency.setExchangeRate(c.getExchangeRate());
                                            newCurrency.setDefaultCurrency(c.isDefaultCurrency());
                                            newCurrency.setSymbol(c.getSymbol());
                                            newCurrency.setColor(c.getColor());
                                            newCurrency.setDecimalSupported(c.isDecimalSupported());
                                            newCurrency.setPayable(c.isPayable());
                                            newCurrency.setDefaultBalance(c.getDefaultBalance());
                                            GemsEconomy.inst().getDataStore().saveCurrency(newCurrency);
                                        }
                                        sender.sendMessage(F.prefix() + "§aSaved currencies to storage.");
                                        GemsEconomy.inst().getDataStore().loadCurrencies();
                                        sender.sendMessage(F.prefix() + "§aLoaded all currencies as usual.");

                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException ex) {
                                            ex.printStackTrace();
                                        }

                                        for (Account a : offline) {
                                            GemsEconomy.inst().getDataStore().saveAccount(a);
                                        }
                                        sender.sendMessage(F.prefix() + "§aAll accounts saved to storage.");

                                        try {
                                            Thread.sleep(2000);
                                        } catch (InterruptedException ex) {
                                            ex.printStackTrace();
                                        }

                                        for (Player players : Bukkit.getOnlinePlayers()) {
                                            GemsEconomy.inst().getDataStore().loadAccount(players.getUniqueId(), account -> GemsEconomy.inst().getAccountManager().addAccount(account));
                                        }
                                        sender.sendMessage(F.prefix() + "§aLoaded all accounts for online players.");
                                        sender.sendMessage(F.prefix() + "§aData storage conversion is done.");
                                    }
                                } else {
                                    sender.sendMessage(F.prefix() + "§cData Storing method not found.");
                                }
                            });
                        }))
                .withSubcommand(new CommandAPICommand("backend")
                        .withArguments(methodArgument)
                        .executes((sender, args) -> {
                            SchedulerUtils.runAsync(() -> {
                                String methodArg = args[0].toString();
                                StorageType method = StorageType.valueOf(methodArg.trim().toUpperCase());
                                DataStorage current = GemsEconomy.inst().getDataStore();
                                DataStorage given = DataStorage.getMethod(method);

                                if (current == null) {
                                    sender.sendMessage(F.prefix() + "§7Current Data Store is null. Did something go wrong on startup?");
                                    return;
                                }

                                if (given != null) {
                                    if (current.getStorageType() == given.getStorageType()) {
                                        sender.sendMessage(F.prefix() + "§7You can't convert to the same datastore.");
                                        return;
                                    }

                                    GemsEconomy.inst().getConfig().set("storage", given.getStorageType());
                                    GemsEconomy.inst().saveConfig();

                                    sender.sendMessage(F.prefix() + "§aSaving data and closing up...");

                                    if (GemsEconomy.inst().getDataStore() != null) {
                                        GemsEconomy.inst().getDataStore().close();

                                        GemsEconomy.inst().getAccountManager().getAccounts().clear();
                                        GemsEconomy.inst().getCurrencyManager().getCurrencies().clear();

                                        sender.sendMessage(F.prefix() + "§aSuccessfully shutdown. Booting..");
                                    }

                                    sender.sendMessage(F.prefix() + "§aSwitching from §f" + current.getStorageType() + " §ato §f" + given.getStorageType() + "§a.");

                                    GemsEconomy.inst().initializeDataStore(given.getStorageType(), true);
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ex) {
                                        ex.printStackTrace();
                                    }

                                    for (Player players : Bukkit.getOnlinePlayers()) {
                                        GemsEconomy.inst().getDataStore().loadAccount(players.getUniqueId(), account -> GemsEconomy.inst().getAccountManager().addAccount(account));
                                    }
                                    sender.sendMessage(F.prefix() + "§aLoaded all accounts for online players.");
                                }
                            });
                        }))
                .register();
    }
}

package me.xanium.gemseconomy.command.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.arguments.standard.EnumArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.GemsMessages;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.command.GemsCommand;
import me.xanium.gemseconomy.command.GemsCommands;
import me.xanium.gemseconomy.command.argument.AmountArgument;
import me.xanium.gemseconomy.command.argument.CurrencyArgument;
import me.xanium.gemseconomy.command.argument.TextColorArgument;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.data.DataStorage;
import me.xanium.gemseconomy.data.StorageType;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import me.xanium.gemseconomy.utils.UtilServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CurrencyCommand extends GemsCommand {

    public CurrencyCommand(GemsEconomy plugin, GemsCommands manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command.Builder<CommandSender> builder = manager
                .commandBuilder("currency")
                .permission("gemseconomy.command.currency");

        Command<CommandSender> createCurrency = builder
                .literal("create")
                .argument(StringArgument.of("singular"))
                .argument(StringArgument.of("plural"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    String singular = context.get("singular");
                    String plural = context.get("plural");

                    Currency currency = GemsEconomy.getInstance().getCurrencyManager().createNewCurrency(singular, plural);
                    if (currency != null) {
                        GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                                .component(sender, "msg_created_currency")
                                .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                        );
                    } else {
                        GemsEconomy.lang().sendComponent(sender, "err_currency_exists");
                    }
                })
                .build();

        Command<CommandSender> listCurrency = builder
                .literal("list")
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    GemsEconomy.lang().sendComponent(sender, "msg_currency_list_header", "size", Integer.toString(GemsEconomy.getInstance().getCurrencyManager().getCurrencies().size()));
                    for (Currency currency : GemsEconomy.getInstance().getCurrencyManager().getCurrencies()) {
                        GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                                .component(sender, "msg_currency_list_entry")
                                .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                        );
                    }
                }).build();

        Command<CommandSender> viewCurrency = builder
                .literal("view")
                .argument(CurrencyArgument.of("currency"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Currency currency = context.get("currency");
                    GemsEconomy.lang().sendComponent(sender, "msg_currency_uuid", "uuid", currency.getUuid().toString());
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_currency_display_name")
                            .replaceText(config -> {
                                config.matchLiteral("{singular}");
                                config.replacement(Component.text(currency.getSingular(), currency.getColor()));
                            })
                            .replaceText(config -> {
                                config.matchLiteral("{plural}");
                                config.replacement(Component.text(currency.getPlural(), currency.getColor()));
                            })
                    );
                    GemsEconomy.lang().sendComponent(sender, "msg_currency_default_balance", "default_balance", Double.toString(currency.getDefaultBalance()));
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_currency_decimal_support")
                            .replaceText(GemsMessages.STATUS_REPLACEMENT.apply(currency.isDecimalSupported()))
                    );
                    GemsEconomy.lang().sendComponent(sender, "msg_currency_maximum_balance", "maximum_balance", Double.toString(currency.getMaxBalance()));
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_currency_default")
                            .replaceText(GemsMessages.STATUS_REPLACEMENT.apply(currency.isDefaultCurrency()))
                    );
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_currency_payable")
                            .replaceText(GemsMessages.STATUS_REPLACEMENT.apply(currency.isPayable()))
                    );
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_currency_color")
                            .replaceText(config -> {
                                config.matchLiteral("{color}");
                                config.replacement(Component.text(currency.getColor().asHexString(), currency.getColor()));
                            })
                    );
                    GemsEconomy.lang().sendComponent(sender, "msg_currency_rate", "rate", Double.toString(currency.getExchangeRate()));
                })
                .build();

        Command<CommandSender> setCurrencyDefaultBalance = builder
                .literal("startbal")
                .argument(CurrencyArgument.of("currency"))
                .argument(AmountArgument.of("amount"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Currency currency = context.get("currency");
                    double amount = context.get("amount");
                    currency.setDefaultBalance(amount);
                    GemsEconomy.getInstance().getDataStore().saveCurrency(currency);
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_set_currency_default_balance")
                            .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                            .replaceText(config -> {
                                config.matchLiteral("{default_balance}");
                                config.replacement(Component.text(currency.getDefaultBalance()));
                            })
                    );
                })
                .build();

        Command<CommandSender> setCurrencyMaximumBalance = builder
                .literal("maxbal")
                .argument(CurrencyArgument.of("currency"))
                .argument(AmountArgument.of("amount"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Currency currency = context.get("currency");
                    double amount = context.get("amount");
                    currency.setMaxBalance(amount);
                    GemsEconomy.getInstance().getDataStore().saveCurrency(currency);
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_set_currency_maximum_balance")
                            .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                            .replaceText(config -> {
                                config.matchLiteral("{maximum_balance}");
                                config.replacement(Component.text(currency.getMaxBalance()));
                            })
                    );
                })
                .build();

        Command<CommandSender> setCurrencyColor = builder
                .literal("color")
                .argument(CurrencyArgument.of("currency"))
                .argument(TextColorArgument.of("color"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Currency currency = context.get("currency");
                    TextColor chatColor = context.get("color");
                    currency.setColor(chatColor);
                    GemsEconomy.getInstance().getDataStore().saveCurrency(currency);
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_set_currency_color")
                            .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                            .replaceText(config -> {
                                config.matchLiteral("{color}");
                                config.replacement(Component.text(currency.getColor().asHexString(), currency.getColor()));
                            })
                    );
                })
                .build();

        Command<CommandSender> setCurrencySymbol = builder.literal("symbol")
                .argument(CurrencyArgument.of("currency"))
                .argument(StringArgument.of("symbol"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Currency currency = context.get("currency");
                    String symbol = context.get("symbol");
                    if (symbol.equalsIgnoreCase("remove")) {
                        currency.setSymbol(null);
                        GemsEconomy.getInstance().getDataStore().saveCurrency(currency);
                        GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                                .component(sender, "msg_removed_currency_symbol")
                                .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                        );
                    } else if (symbol.length() == 1) {
                        currency.setSymbol(symbol);
                        GemsEconomy.getInstance().getDataStore().saveCurrency(currency);
                        GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                                .component(sender, "msg_set_currency_symbol")
                                .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                                .replaceText(config -> {
                                    config.matchLiteral("{symbol}");
                                    config.replacement(symbol);
                                })
                        );
                    } else {
                        GemsEconomy.lang().sendComponent(sender, "err_currency_symbol_can_only_have_one_char");
                    }
                })
                .build();

        Command<CommandSender> setDefaultCurrency = builder.literal("default")
                .argument(CurrencyArgument.of("currency"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Currency currency = context.get("currency");
                    Currency defaultCurrency = GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency();
                    if (defaultCurrency != null) {
                        defaultCurrency.setDefaultCurrency(false);
                        GemsEconomy.getInstance().getDataStore().saveCurrency(defaultCurrency);
                    }
                    currency.setDefaultCurrency(true);
                    GemsEconomy.getInstance().getDataStore().saveCurrency(currency);
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_set_default_currency")
                            .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                    );
                })
                .build();

        Command<CommandSender> toggleCurrencyPayable = builder.literal("payable")
                .argument(CurrencyArgument.of("currency"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Currency currency = context.get("currency");
                    currency.setPayable(!currency.isPayable());
                    GemsEconomy.getInstance().getDataStore().saveCurrency(currency);
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_toggled_currency_payable")
                            .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                            .replaceText(GemsMessages.STATUS_REPLACEMENT.apply(currency.isPayable()))
                    );
                })
                .build();

        Command<CommandSender> toggleCurrencyDecimals = builder.literal("decimals")
                .argument(CurrencyArgument.of("currency"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Currency currency = context.get("currency");
                    currency.setDecimalSupported(!currency.isDecimalSupported());
                    GemsEconomy.getInstance().getDataStore().saveCurrency(currency);
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_toggled_currency_decimal_support")
                            .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                            .replaceText(GemsMessages.STATUS_REPLACEMENT.apply(currency.isDecimalSupported()))
                    );
                })
                .build();

        Command<CommandSender> deleteCurrency = builder.literal("delete")
                .argument(CurrencyArgument.of("currency"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Currency currency = context.get("currency");
                    GemsEconomy.getInstance().getCurrencyManager().remove(currency);
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_deleted_currency")
                            .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                    );
                })
                .build();

        Command<CommandSender> clearBalance = builder.literal("clear")
                .argument(CurrencyArgument.of("currency"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Currency currency = context.get("currency");
                    GemsEconomy.getInstance()
                            .getAccountManager()
                            .getAllAccounts()
                            .stream()
                            .filter(account -> account.getBalances().containsKey(currency))
                            .forEach(account -> {
                                account.getBalances().put(currency, 0D);
                                GemsEconomy.getInstance().getDataStore().saveAccount(account);
                            });
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_cleared_balance")
                            .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                    );
                })
                .build();

        Command<CommandSender> setCurrencyRate = builder.literal("setrate")
                .argument(CurrencyArgument.of("currency"))
                .argument(DoubleArgument
                        .<CommandSender>newBuilder("rate")
                        .withMin(0)
                        .build())
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Currency currency = context.get("currency");
                    double rate = context.get("rate");
                    currency.setExchangeRate(rate);
                    GemsEconomy.getInstance().getDataStore().saveCurrency(currency);
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_set_exchange_rate")
                            .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                            .replaceText(config -> {
                                config.matchLiteral("{rate}");
                                config.replacement(Double.toString(rate));
                            })
                    );
                })
                .build();

        Command<CommandSender> convertStorageMethod = builder
                .literal("convert")
                .argument(EnumArgument.of(StorageType.class, "method"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    StorageType method = context.get("method");
                    DataStorage current = GemsEconomy.getInstance().getDataStore();
                    DataStorage given = DataStorage.getMethod(method);

                    if (current == null) {
                        GemsEconomy.lang().sendComponent(sender, "err_data_storage_is_null");
                        return;
                    }

                    if (given != null) {
                        if (current.getStorageType() == given.getStorageType()) {
                            GemsEconomy.lang().sendComponent(sender, "err_convert_to_same_storage");
                            return;
                        }

                        GemsEconomy.getInstance().getConfig().set("storage", given.getStorageType());
                        GemsEconomy.getInstance().saveConfig();

                        GemsEconomy.lang().sendComponent(sender, "msg_loading_data");
                        GemsEconomy.getInstance().getAccountManager().getAccounts().clear();

                        ArrayList<Account> offline = new ArrayList<>(GemsEconomy.getInstance().getDataStore().getOfflineAccounts());
                        GemsEconomy.lang().sendComponent(sender, "msg_stored_account", "size", Integer.toString(offline.size()));
                        UtilServer.consoleLog("Stored Accounts: " + offline.size());
                        if (GemsEconomy.getInstance().isDebug()) {
                            for (Account account : offline) {
                                UtilServer.consoleLog("Account: " + account.getNickname() + " (" + account.getUuid().toString() + ")");
                                for (Currency currency : account.getBalances().keySet()) {
                                    UtilServer.consoleLog("Balance: " + currency.format(account.getBalance(currency)));
                                }
                            }
                        }

                        ArrayList<Currency> currencies = new ArrayList<>(GemsEconomy.getInstance().getCurrencyManager().getCurrencies());
                        GemsEconomy.getInstance().getCurrencyManager().getCurrencies().clear();
                        GemsEconomy.lang().sendComponent(sender, "msg_stored_currency");

                        if (GemsEconomy.getInstance().isDebug()) {
                            for (Currency c : currencies) {
                                UtilServer.consoleLog("Currency: " + c.getSingular() + " (" + c.getPlural() + "): " + c.format(1000000));
                            }
                        }

                        GemsEconomy.lang().sendComponent(sender,
                                "msg_switched_storage",
                                "from", current.getStorageType().toString(),
                                "to", given.getStorageType().toString()
                        );

                        if (given.getStorageType() == StorageType.YAML) {
                            SchedulerUtils.run(() -> {
                                File data = new File(GemsEconomy.getInstance().getDataFolder() + File.separator + "data.yml");
                                if (data.exists()) {
                                    data.delete();
                                }
                            });
                        }

                        if (GemsEconomy.getInstance().getDataStore() != null) {
                            GemsEconomy.getInstance().getDataStore().close();
                            GemsEconomy.lang().sendComponent(sender, "msg_storage_is_closed");
                        }

                        GemsEconomy.getInstance().initializeDataStore(given.getStorageType(), false);
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }

                        GemsEconomy.lang().sendComponent(sender, "msg_storage_is_initialised", "storage", given.getStorageType().toString());

                        if (GemsEconomy.getInstance().getDataStore().getStorageType() != null) {
                            for (Currency c : currencies) {
                                Currency newCurrency = new Currency(c.getUuid(), c.getSingular(), c.getPlural());
                                newCurrency.setExchangeRate(c.getExchangeRate());
                                newCurrency.setDefaultCurrency(c.isDefaultCurrency());
                                newCurrency.setSymbol(c.getSymbol());
                                newCurrency.setColor(c.getColor());
                                newCurrency.setDecimalSupported(c.isDecimalSupported());
                                newCurrency.setPayable(c.isPayable());
                                newCurrency.setDefaultBalance(c.getDefaultBalance());
                                GemsEconomy.getInstance().getDataStore().saveCurrency(newCurrency);
                            }
                            GemsEconomy.lang().sendComponent(sender, "msg_saved_currencies_to_storage");
                            GemsEconomy.getInstance().getDataStore().loadCurrencies();
                            GemsEconomy.lang().sendComponent(sender, "msg_loaded_all_currencies");

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }

                            for (Account a : offline) {
                                GemsEconomy.getInstance().getDataStore().saveAccount(a);
                            }
                            GemsEconomy.lang().sendComponent(sender, "msg_all_accounts_saved_to_storage");

                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }

                            for (Player players : Bukkit.getOnlinePlayers()) {
                                GemsEconomy.getInstance().getDataStore().loadAccount(players.getUniqueId(), account -> GemsEconomy.getInstance().getAccountManager().addAccount(account));
                            }
                            GemsEconomy.lang().sendComponent(sender, "msg_loaded_all_accounts_for_online");
                            GemsEconomy.lang().sendComponent(sender, "msg_data_storage_conversion_is_done");
                        }
                    } else {
                        GemsEconomy.lang().sendComponent(sender, "err_data_storage_method_not_found");
                    }

                })
                .build();

        Command<CommandSender> switchBackendStorage = builder
                .literal("backend")
                .argument(EnumArgument.of(StorageType.class, "method"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    StorageType method = context.get("method");
                    DataStorage current = GemsEconomy.getInstance().getDataStore();
                    DataStorage given = DataStorage.getMethod(method);

                    if (current == null) {
                        GemsEconomy.lang().sendComponent(sender, "err_data_storage_is_null");
                        return;
                    }

                    if (given != null) {
                        if (current.getStorageType() == given.getStorageType()) {
                            GemsEconomy.lang().sendComponent(sender, "err_convert_to_same_storage");
                            return;
                        }

                        GemsEconomy.getInstance().getConfig().set("storage", given.getStorageType());
                        GemsEconomy.getInstance().saveConfig();

                        GemsEconomy.lang().sendComponent(sender, "msg_saving_data_and_closing");

                        if (GemsEconomy.getInstance().getDataStore() != null) {
                            GemsEconomy.getInstance().getDataStore().close();

                            GemsEconomy.getInstance().getAccountManager().getAccounts().clear();
                            GemsEconomy.getInstance().getCurrencyManager().getCurrencies().clear();

                            GemsEconomy.lang().sendComponent(sender, "msg_shutdown_and_booting");
                        }

                        GemsEconomy.lang().sendComponent(sender,
                                "msg_switched_storage",
                                "from", current.getStorageType().toString(),
                                "to", given.getStorageType().toString()
                        );


                        GemsEconomy.getInstance().initializeDataStore(given.getStorageType(), true);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException ex) {
                            ex.printStackTrace();
                        }

                        for (Player players : Bukkit.getOnlinePlayers()) {
                            GemsEconomy.getInstance().getDataStore().loadAccount(players.getUniqueId(), account -> GemsEconomy.getInstance().getAccountManager().addAccount(account));
                        }
                        GemsEconomy.lang().sendComponent(sender, "msg_loaded_all_accounts_for_online");
                    }
                })
                .build();

        manager.register(List.of(
                createCurrency,
                listCurrency,
                viewCurrency,
                setCurrencyDefaultBalance,
                setCurrencyMaximumBalance,
                setCurrencyColor,
                setCurrencySymbol,
                setDefaultCurrency,
                toggleCurrencyPayable,
                toggleCurrencyDecimals,
                deleteCurrency,
                clearBalance,
                setCurrencyRate,
                convertStorageMethod,
                switchBackendStorage
        ));
    }

}

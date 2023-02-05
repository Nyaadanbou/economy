package me.xanium.gemseconomy.command.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.GemsMessages;
import me.xanium.gemseconomy.command.GemsCommand;
import me.xanium.gemseconomy.command.GemsCommands;
import me.xanium.gemseconomy.command.argument.AmountArgument;
import me.xanium.gemseconomy.command.argument.CurrencyArgument;
import me.xanium.gemseconomy.command.argument.TextColorArgument;
import me.xanium.gemseconomy.currency.Currency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;

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
                    Currency newDefault = context.get("currency");

                    Currency oldDefault = GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency();
                    oldDefault.setDefaultCurrency(false);
                    GemsEconomy.getInstance().getDataStore().saveCurrency(oldDefault);

                    newDefault.setDefaultCurrency(true);
                    GemsEconomy.getInstance().getDataStore().saveCurrency(newDefault);
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_set_default_currency")
                            .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(newDefault.getDisplayName()))
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
                            .getOfflineAccounts()
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
                .argument(DoubleArgument.<CommandSender>builder("rate").withMin(0).build())
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
                setCurrencyRate
        ));
    }

}

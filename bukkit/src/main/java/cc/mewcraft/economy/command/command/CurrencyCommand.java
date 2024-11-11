package cc.mewcraft.economy.command.command;

import cc.mewcraft.economy.EconomyPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import cc.mewcraft.economy.api.Currency;
import cc.mewcraft.economy.command.AbstractCommand;
import cc.mewcraft.economy.command.CommandManager;
import cc.mewcraft.economy.command.argument.AmountParser;
import cc.mewcraft.economy.command.argument.CurrencyParser;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.minecraft.extras.parser.TextColorParser;
import org.incendo.cloud.parser.standard.DoubleParser;
import org.incendo.cloud.parser.standard.StringParser;

import java.util.List;

import static cc.mewcraft.economy.EconomyMessages.CURRENCY_REPLACEMENT;
import static cc.mewcraft.economy.EconomyMessages.STATUS_REPLACEMENT;

@SuppressWarnings("UnstableApiUsage")
public class CurrencyCommand extends AbstractCommand {

    public CurrencyCommand(EconomyPlugin plugin, CommandManager manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command.Builder<CommandSourceStack> builder = this.manager.getCommandManager()
                .commandBuilder("currency")
                .permission("economy.command.currency");

        Command<CommandSourceStack> createCurrency = builder
                .literal("create")
                .required("name", StringParser.quotedStringParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    String name = context.get("name");

                    Currency currency = EconomyPlugin.getInstance().getCurrencyManager().createCurrency(name);
                    if (currency != null) {
                        EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                                .component(sender, "msg_created_currency")
                                .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                        );
                    } else {
                        EconomyPlugin.lang().sendComponent(sender, "err_currency_exists");
                    }
                })
                .build();

        Command<CommandSourceStack> listCurrency = builder
                .literal("list")
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    EconomyPlugin.lang().sendComponent(sender, "msg_currency_list_header", "size", Integer.toString(EconomyPlugin.getInstance().getCurrencyManager().getLoadedCurrencies().size()));
                    for (Currency currency : EconomyPlugin.getInstance().getCurrencyManager().getLoadedCurrencies()) {
                        EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                                .component(sender, "msg_currency_list_entry")
                                .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                        );
                    }
                }).build();

        Command<CommandSourceStack> viewCurrency = builder
                .literal("view")
                .required("currency", CurrencyParser.currencyParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Currency currency = context.get("currency");
                    EconomyPlugin.lang().sendComponent(sender, "msg_currency_uuid", "uuid", currency.getUuid().toString());
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_currency_name")
                            .replaceText(config -> {
                                config.matchLiteral("{name}");
                                config.replacement(Component.text(currency.getName(), currency.getColor()));
                            })
                    );
                    EconomyPlugin.lang().sendComponent(sender, "msg_currency_default_balance", "default_balance", Double.toString(currency.getDefaultBalance()));
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_currency_decimal_support")
                            .replaceText(STATUS_REPLACEMENT.apply(currency.isDecimalSupported()))
                    );
                    EconomyPlugin.lang().sendComponent(sender, "msg_currency_maximum_balance", "maximum_balance", Double.toString(currency.getMaximumBalance()));
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_currency_default")
                            .replaceText(STATUS_REPLACEMENT.apply(currency.isDefaultCurrency()))
                    );
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_currency_payable")
                            .replaceText(STATUS_REPLACEMENT.apply(currency.isPayable()))
                    );
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_currency_color")
                            .replaceText(config -> {
                                config.matchLiteral("{color}");
                                config.replacement(Component.text(currency.getColor().asHexString(), currency.getColor()));
                            })
                    );
                    EconomyPlugin.lang().sendComponent(sender, "msg_currency_rate", "rate", Double.toString(currency.getExchangeRate()));
                })
                .build();

        Command<CommandSourceStack> setCurrencyDefaultBalance = builder
                .literal("startbal")
                .required("currency", CurrencyParser.currencyParser())
                .required("amount", AmountParser.amountParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Currency currency = context.get("currency");
                    double amount = context.get("amount");
                    currency.setDefaultBalance(amount);
                    this.plugin.getCurrencyManager().saveCurrency(currency);
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_set_currency_default_balance")
                            .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                            .replaceText(config -> {
                                config.matchLiteral("{default_balance}");
                                config.replacement(Component.text(currency.getDefaultBalance()));
                            })
                    );
                })
                .build();

        Command<CommandSourceStack> setCurrencyMaximumBalance = builder
                .literal("maxbal")
                .required("currency", CurrencyParser.currencyParser())
                .required("amount", AmountParser.amountParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Currency currency = context.get("currency");
                    double amount = context.get("amount");
                    currency.setMaximumBalance(amount);
                    this.plugin.getCurrencyManager().saveCurrency(currency);
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_set_currency_maximum_balance")
                            .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                            .replaceText(config -> {
                                config.matchLiteral("{maximum_balance}");
                                config.replacement(Component.text(currency.getMaximumBalance()));
                            })
                    );
                })
                .build();

        Command<CommandSourceStack> setCurrencyColor = builder
                .literal("color")
                .required("currency", CurrencyParser.currencyParser())
                .required("color", TextColorParser.textColorParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Currency currency = context.get("currency");
                    TextColor chatColor = context.get("color");
                    currency.setColor(chatColor);
                    this.plugin.getCurrencyManager().saveCurrency(currency);
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_set_currency_color")
                            .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                            .replaceText(config -> {
                                config.matchLiteral("{color}");
                                config.replacement(Component.text(currency.getColor().asHexString(), currency.getColor()));
                            })
                    );
                })
                .build();

        Command<CommandSourceStack> setCurrencySymbol = builder.literal("symbol")
                .required("currency", CurrencyParser.currencyParser())
                .required("symbol", StringParser.quotedStringParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Currency currency = context.get("currency");
                    String symbol = context.get("symbol");
                    if (symbol.equalsIgnoreCase("remove")) {
                        currency.setSymbol(null);
                        this.plugin.getCurrencyManager().saveCurrency(currency);
                        EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                                .component(sender, "msg_removed_currency_symbol")
                                .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                        );
                    } else {
                        currency.setSymbol(symbol);
                        this.plugin.getCurrencyManager().saveCurrency(currency);
                        EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                                .component(sender, "msg_set_currency_symbol")
                                .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                                .replaceText(config -> {
                                    config.matchLiteral("{symbol}");
                                    config.replacement(symbol);
                                })
                        );
                    }
                })
                .build();

        Command<CommandSourceStack> setDefaultCurrency = builder.literal("default")
                .required("currency", CurrencyParser.currencyParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Currency newDefault = context.get("currency");

                    Currency oldDefault = EconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency();
                    oldDefault.setDefaultCurrency(false);
                    this.plugin.getCurrencyManager().saveCurrency(oldDefault);

                    newDefault.setDefaultCurrency(true);
                    this.plugin.getCurrencyManager().saveCurrency(newDefault);
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_set_default_currency")
                            .replaceText(CURRENCY_REPLACEMENT.apply(newDefault))
                    );
                })
                .build();

        Command<CommandSourceStack> toggleCurrencyPayable = builder.literal("payable")
                .required("currency", CurrencyParser.currencyParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Currency currency = context.get("currency");
                    currency.setPayable(!currency.isPayable());
                    this.plugin.getCurrencyManager().saveCurrency(currency);
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_toggled_currency_payable")
                            .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                            .replaceText(STATUS_REPLACEMENT.apply(currency.isPayable()))
                    );
                })
                .build();

        Command<CommandSourceStack> toggleCurrencyDecimals = builder.literal("decimals")
                .required("currency", CurrencyParser.currencyParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Currency currency = context.get("currency");
                    currency.setDecimalSupported(!currency.isDecimalSupported());
                    this.plugin.getCurrencyManager().saveCurrency(currency);
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_toggled_currency_decimal_support")
                            .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                            .replaceText(STATUS_REPLACEMENT.apply(currency.isDecimalSupported()))
                    );
                })
                .build();

        Command<CommandSourceStack> deleteCurrency = builder.literal("delete")
                .required("currency", CurrencyParser.currencyParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Currency currency = context.get("currency");
                    EconomyPlugin.getInstance().getCurrencyManager().removeCurrency(currency);
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_deleted_currency")
                            .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                    );
                })
                .build();

        Command<CommandSourceStack> clearBalance = builder.literal("clear")
                .required("currency", CurrencyParser.currencyParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Currency currency = context.get("currency");
                    EconomyPlugin.getInstance().getCurrencyManager().clearBalance(currency);
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_cleared_balance")
                            .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                    );
                })
                .build();

        Command<CommandSourceStack> setCurrencyRate = builder.literal("setrate")
                .required("currency", CurrencyParser.currencyParser())
                .required("rate", DoubleParser.doubleParser(.0))
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Currency currency = context.get("currency");
                    double rate = context.get("rate");
                    currency.setExchangeRate(rate);
                    this.plugin.getCurrencyManager().saveCurrency(currency);
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_set_exchange_rate")
                            .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                            .replaceText(config -> {
                                config.matchLiteral("{rate}");
                                config.replacement(Double.toString(rate));
                            })
                    );
                })
                .build();

        this.manager.register(List.of(
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

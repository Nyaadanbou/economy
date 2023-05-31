package me.xanium.gemseconomy.command.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.DoubleArgument;
import cloud.commandframework.arguments.standard.StringArgument;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.command.AbstractCommand;
import me.xanium.gemseconomy.command.CommandManager;
import me.xanium.gemseconomy.command.argument.AmountArgument;
import me.xanium.gemseconomy.command.argument.CurrencyArgument;
import me.xanium.gemseconomy.command.argument.TextColorArgument;
import me.xanium.gemseconomy.currency.Currency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;

import java.util.List;

import static me.xanium.gemseconomy.GemsMessages.CURRENCY_REPLACEMENT;
import static me.xanium.gemseconomy.GemsMessages.STATUS_REPLACEMENT;

public class CurrencyCommand extends AbstractCommand {

    public CurrencyCommand(GemsEconomy plugin, CommandManager manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command.Builder<CommandSender> builder = this.manager
            .commandBuilder("currency")
            .permission("gemseconomy.command.currency");

        Command<CommandSender> createCurrency = builder
            .literal("create")
            .argument(StringArgument.of("name"))
            .handler(context -> {
                CommandSender sender = context.getSender();
                String name = context.get("name");

                Currency currency = GemsEconomy.getInstance().getCurrencyManager().createCurrency(name);
                if (currency != null) {
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                        .component(sender, "msg_created_currency")
                        .replaceText(CURRENCY_REPLACEMENT.apply(currency))
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
                        .replaceText(CURRENCY_REPLACEMENT.apply(currency))
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
                    .component(sender, "msg_currency_name")
                    .replaceText(config -> {
                        config.matchLiteral("{name}");
                        config.replacement(currency.getDisplayName());
                    })
                );
                GemsEconomy.lang().sendComponent(sender, "msg_currency_default_balance", "default_balance", Double.toString(currency.getDefaultBalance()));
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_currency_decimal_support")
                    .replaceText(STATUS_REPLACEMENT.apply(currency.isDecimalSupported()))
                );
                GemsEconomy.lang().sendComponent(sender, "msg_currency_maximum_balance", "maximum_balance", Double.toString(currency.getMaxBalance()));
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_currency_default")
                    .replaceText(STATUS_REPLACEMENT.apply(currency.isDefaultCurrency()))
                );
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_currency_payable")
                    .replaceText(STATUS_REPLACEMENT.apply(currency.isPayable()))
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
                this.plugin.getCurrencyManager().saveCurrency(currency);
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_set_currency_default_balance")
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency))
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
                currency.setMaximumBalance(amount);
                this.plugin.getCurrencyManager().saveCurrency(currency);
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_set_currency_maximum_balance")
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency))
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
                this.plugin.getCurrencyManager().saveCurrency(currency);
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_set_currency_color")
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency))
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
                    this.plugin.getCurrencyManager().saveCurrency(currency);
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                        .component(sender, "msg_removed_currency_symbol")
                        .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                    );
                } else {
                    currency.setSymbol(symbol);
                    this.plugin.getCurrencyManager().saveCurrency(currency);
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
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

        Command<CommandSender> setDefaultCurrency = builder.literal("default")
            .argument(CurrencyArgument.of("currency"))
            .handler(context -> {
                CommandSender sender = context.getSender();
                Currency newDefault = context.get("currency");

                Currency oldDefault = GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency();
                oldDefault.setDefaultCurrency(false);
                this.plugin.getCurrencyManager().saveCurrency(oldDefault);

                newDefault.setDefaultCurrency(true);
                this.plugin.getCurrencyManager().saveCurrency(newDefault);
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_set_default_currency")
                    .replaceText(CURRENCY_REPLACEMENT.apply(newDefault))
                );
            })
            .build();

        Command<CommandSender> toggleCurrencyPayable = builder.literal("payable")
            .argument(CurrencyArgument.of("currency"))
            .handler(context -> {
                CommandSender sender = context.getSender();
                Currency currency = context.get("currency");
                currency.setPayable(!currency.isPayable());
                this.plugin.getCurrencyManager().saveCurrency(currency);
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_toggled_currency_payable")
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                    .replaceText(STATUS_REPLACEMENT.apply(currency.isPayable()))
                );
            })
            .build();

        Command<CommandSender> toggleCurrencyDecimals = builder.literal("decimals")
            .argument(CurrencyArgument.of("currency"))
            .handler(context -> {
                CommandSender sender = context.getSender();
                Currency currency = context.get("currency");
                currency.setDecimalSupported(!currency.isDecimalSupported());
                this.plugin.getCurrencyManager().saveCurrency(currency);
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_toggled_currency_decimal_support")
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                    .replaceText(STATUS_REPLACEMENT.apply(currency.isDecimalSupported()))
                );
            })
            .build();

        Command<CommandSender> deleteCurrency = builder.literal("delete")
            .argument(CurrencyArgument.of("currency"))
            .handler(context -> {
                CommandSender sender = context.getSender();
                Currency currency = context.get("currency");
                GemsEconomy.getInstance().getCurrencyManager().removeCurrency(currency);
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_deleted_currency")
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                );
            })
            .build();

        Command<CommandSender> clearBalance = builder.literal("clear")
            .argument(CurrencyArgument.of("currency"))
            .handler(context -> {
                CommandSender sender = context.getSender();
                Currency currency = context.get("currency");
                GemsEconomy.getInstance().getCurrencyManager().clearBalance(currency);
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_cleared_balance")
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency))
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
                this.plugin.getCurrencyManager().saveCurrency(currency);
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
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

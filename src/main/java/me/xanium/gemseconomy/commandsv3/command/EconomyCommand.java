package me.xanium.gemseconomy.commandsv3.command;

import cloud.commandframework.Command;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.commandsv3.GemsCommand;
import me.xanium.gemseconomy.commandsv3.GemsCommands;
import me.xanium.gemseconomy.commandsv3.argument.AccountArgument;
import me.xanium.gemseconomy.commandsv3.argument.AmountArgument;
import me.xanium.gemseconomy.commandsv3.argument.CurrencyArgument;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.utils.UtilServer;
import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class EconomyCommand extends GemsCommand {

    private final Function<Currency, Consumer<TextReplacementConfig.Builder>> currencyReplacement = currency -> config -> {
        config.matchLiteral("{currency}");
        config.replacement(currency.getDisplayName());
    };
    private final Function<Account, Consumer<TextReplacementConfig.Builder>> accountReplacement = account -> config -> {
        config.matchLiteral("{currency}");
        config.replacement(account.getDisplayName());
    };
    private final BiFunction<Currency, Double, Consumer<TextReplacementConfig.Builder>> amountReplacement = (currency, amount) -> config -> {
        config.matchLiteral("{amount}");
        config.replacement(currency.getDisplayName());
    };

    public EconomyCommand(GemsEconomy plugin, GemsCommands manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command.Builder<CommandSender> builder = manager
                .commandBuilder("economy", "eco")
                .permission("gemseconomy.command.economy");

        Command<CommandSender> give = builder
                .literal("give")
                .argument(AccountArgument.of("account"))
                .argument(AmountArgument.of("amount"))
                .argument(CurrencyArgument.of("currency"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Account account = context.get("account");
                    double amount = context.get("amount");
                    Currency currency = context.get("currency");

                    changeBalance(sender, account, amount, currency, false);
                })
                .build();

        Command<CommandSender> take = builder
                .literal("take")
                .argument(AccountArgument.of("account"))
                .argument(AmountArgument.of("amount"))
                .argument(CurrencyArgument.of("currency"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Account account = context.get("account");
                    double amount = context.get("amount");
                    Currency currency = context.get("currency");

                    changeBalance(sender, account, amount, currency, true);
                })
                .build();

        Command<CommandSender> set = builder
                .literal("set")
                .argument(AccountArgument.of("account"))
                .argument(AmountArgument.of("amount"))
                .argument(CurrencyArgument.of("currency"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Account account = context.get("account");
                    double amount = context.get("amount");
                    Currency currency = context.get("currency");

                    setBalance(sender, account, amount, currency);
                })
                .build();

        Command<CommandSender> cache = builder
                .literal("cache")
                .handler(context -> {
                    for (Account a : GemsEconomy.inst().getAccountManager().getAccounts()) {
                        UtilServer.consoleLog("Account: " + a.getNickname() + " cached");
                    }
                })
                .build();

        Command<CommandSender> debug = builder
                .literal("debug")
                .permission("gemseconomy.command.debug")
                .senderType(ConsoleCommandSender.class)
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    GemsEconomy.inst().setDebug(!GemsEconomy.inst().isDebug());
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                            .component(sender, "msg_debug_status")
                            .replaceText(config -> {
                                config.matchLiteral("{status}");
                                config.replacement(GemsEconomy.inst().isDebug()
                                        ? GemsEconomy.lang().component(sender, "msg_enabled")
                                        : GemsEconomy.lang().component(sender, "msg_disabled"));
                            })
                    );
                })
                .build();

        manager.register(List.of(
                give,
                take,
                set,
                cache,
                debug
        ));
    }

    @NonnullByDefault
    private void changeBalance(CommandSender sender, Account account, double amount, Currency currency, boolean withdraw) {
        if (withdraw) {
            if (account.withdraw(currency, amount)) {
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                        .component(sender, "msg_eco_taken")
                        .replaceText(amountReplacement.apply(currency, amount))
                        .replaceText(accountReplacement.apply(account))
                );
            } else {
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                        .component(sender, "err_player_insufficient_funds")
                        .replaceText(currencyReplacement.apply(currency))
                        .replaceText(accountReplacement.apply(account))
                );
            }
        } else {
            if (account.deposit(currency, amount)) {
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                        .component(sender, "msg_eco_added")
                        .replaceText(amountReplacement.apply(currency, amount))
                        .replaceText(accountReplacement.apply(account))
                );
                Player target = Bukkit.getPlayer(account.getUuid());
                if (target != null) { // Send message if target player is online
                    GemsEconomy.lang().sendComponent(target, GemsEconomy.lang()
                            .component(target, "msg_received_currency")
                            .replaceText(amountReplacement.apply(currency, amount))
                            .replaceText(config -> {
                                config.matchLiteral("{player}");
                                config.replacement(GemsEconomy.lang().component(target, "msg_console_name"));
                            })
                    );
                }
            }
        }
    }

    @NonnullByDefault
    private void setBalance(CommandSender sender, Account account, double amount, Currency currency) {
        account.setBalance(currency, amount);
        GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                .component(sender, "msg_eco_set")
                .replaceText(amountReplacement.apply(currency, amount))
                .replaceText(config -> {
                    config.matchLiteral("{player}");
                    config.replacement(account.getNickname());
                })
        );
    }

}

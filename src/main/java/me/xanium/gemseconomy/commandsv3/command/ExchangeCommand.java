package me.xanium.gemseconomy.commandsv3.command;

import cloud.commandframework.Command;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.GemsMessages;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.commandsv3.GemsCommand;
import me.xanium.gemseconomy.commandsv3.GemsCommands;
import me.xanium.gemseconomy.commandsv3.argument.AccountArgument;
import me.xanium.gemseconomy.commandsv3.argument.AmountArgument;
import me.xanium.gemseconomy.commandsv3.argument.CurrencyArgument;
import me.xanium.gemseconomy.commandsv3.argument.PreprocessorUtil;
import me.xanium.gemseconomy.currency.Currency;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class ExchangeCommand extends GemsCommand {

    public ExchangeCommand(GemsEconomy plugin, GemsCommands manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command.Builder<CommandSender> builder = manager
                .commandBuilder("exchange")
                .permission("gemseconomy.command.exchange");

        Command<CommandSender> custom = builder.permission("gemseconomy.command.exchange.custom")
                .argument(CurrencyArgument.of("currency1"))
                .argument(AmountArgument.of("amount1").addPreprocessor(PreprocessorUtil.currencyReferrerOf("currency1")))
                .argument(CurrencyArgument.of("currency2"))
                .argument(AmountArgument.of("amount2").addPreprocessor(PreprocessorUtil.currencyReferrerOf("currency2")))
                .senderType(Player.class)
                .handler(context -> {
                    Player sender = (Player) context.getSender();
                    Currency toExchange = context.get("currency1");
                    double toExchangeAmount = context.get("amount1");
                    Currency toReceive = context.get("currency2");
                    double toReceiveAmount = context.get("amount2");

                    Account account = GemsEconomy.inst().getAccountManager().getAccount(sender.getName());
                    if (account != null) {
                        if (account.convert(toExchange, toExchangeAmount, toReceive, toReceiveAmount)) {
                            GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                                    .component(sender, "msg_exchanged_currency")
                                    .replaceText(config -> {
                                        config.matchLiteral("{exchanged_curr}");
                                        config.replacement(toExchange.format(toExchangeAmount));
                                    })
                                    .replaceText(config -> {
                                        config.matchLiteral("{received_curr}");
                                        config.replacement(toReceive.format(toReceiveAmount));
                                    })
                            );
                        }
                    } else {
                        GemsEconomy.lang().sendComponent(sender, "err_account_missing");
                    }
                })
                .build();

        Command<CommandSender> customOther = builder.permission("gemseconomy.command.exchange.custom.other")
                .argument(CurrencyArgument.of("currency1"))
                .argument(AmountArgument.of("amount1").addPreprocessor(PreprocessorUtil.currencyReferrerOf("currency1")))
                .argument(CurrencyArgument.of("currency2"))
                .argument(AmountArgument.of("amount2").addPreprocessor(PreprocessorUtil.currencyReferrerOf("currency2")))
                .argument(AccountArgument.of("account"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Currency toExchange = context.get("currency1");
                    double toExchangeAmount = context.get("amount1");
                    Currency toReceive = context.get("currency2");
                    double toReceiveAmount = context.get("amount2");
                    Account account = context.get("account");

                    if (account.convert(toExchange, toExchangeAmount, toReceive, toReceiveAmount)) {
                        GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                                .component(sender, "msg_exchanged_currency_for_other_player")
                                .replaceText(GemsMessages.ACCOUNT_REPLACEMENT.apply(account.getNickname()))
                                .replaceText(config -> {
                                    config.matchLiteral("{exchanged_curr}");
                                    config.replacement(toExchange.format(toExchangeAmount));
                                })
                                .replaceText(config -> {
                                    config.matchLiteral("{received_curr}");
                                    config.replacement(toReceive.format(toReceiveAmount));
                                })
                        );
                    }
                })
                .build();

        manager.register(List.of(
                custom, customOther
        ));
    }

}

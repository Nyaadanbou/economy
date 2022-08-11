package me.xanium.gemseconomy.commandsv2;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.StringArgument;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.commandsv2.argument.BaseArguments;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.file.F;

public class ExchangeCommand {

    private static final String NAME = "exchange";
    private static final String PERM_EXCHANGE = "gemseconomy.command.exchange";
    private static final String PERM_EXCHANGE_PRESET = "gemseconomy.command.exchange.preset";
    private static final String PERM_EXCHANGE_CUSTOM = "gemseconomy.command.exchange.custom";
    private static final String PERM_EXCHANGE_CUSTOM_OTHER = "gemseconomy.command.exchange.custom.other";

    public ExchangeCommand() {
        final Argument<String> toReceiveArgument = new StringArgument("要兑换消耗的货币数额").replaceSuggestions(ArgumentSuggestions.strings("1"));
        final Argument<String> toExchangeArgument = new StringArgument("要兑换获得的货币数额").replaceSuggestions(ArgumentSuggestions.strings("1"));

        new CommandAPICommand(NAME)
                .withPermission(PERM_EXCHANGE)
                .withPermission(PERM_EXCHANGE_PRESET)
                .withArguments(BaseArguments.CURRENCY)
                .withArguments(toReceiveArgument)
                .withArguments(BaseArguments.CURRENCY)
                .executesPlayer((sender, args) -> {
                    Currency toExchange = GemsEconomy.inst().getCurrencyManager().getCurrency(args[0].toString());
                    Currency toReceive = GemsEconomy.inst().getCurrencyManager().getCurrency(args[2].toString());
                    double amount;

                    if (toExchange != null && toReceive != null) {
                        if (toReceive.isDecimalSupported()) {
                            try {
                                amount = Double.parseDouble(args[1].toString());
                                if (amount <= 0D) {
                                    throw new NumberFormatException();
                                }
                            } catch (NumberFormatException ex) {
                                sender.sendMessage(F.invalidAmount());
                                return;
                            }
                        } else {
                            try {
                                amount = Integer.parseInt(args[1].toString());
                                if (amount <= 0D) {
                                    throw new NumberFormatException();
                                }
                            } catch (NumberFormatException ex) {
                                sender.sendMessage(F.invalidAmount());
                                return;
                            }
                        }
                        Account account = GemsEconomy.inst().getAccountManager().getAccount(sender.getName());
                        if (account != null) {
                            if (account.convert(toExchange, amount, toReceive, -1)) {
                                sender.sendMessage(F.exchangeSuccess()
                                        .replace("{currencycolor}", "" + toExchange.getColor())
                                        .replace("{ex_curr}", toExchange.format(amount))
                                        .replace("{currencycolor2}", "" + toReceive.getColor())
                                        .replace("{re_curr}", toReceive.getPlural()));
                            }
                        }
                    } else {
                        sender.sendMessage(F.unknownCurrency());
                    }
                })
                .register();
        new CommandAPICommand(NAME)
                .withPermission(PERM_EXCHANGE)
                .withPermission(PERM_EXCHANGE_CUSTOM)
                .withArguments(BaseArguments.CURRENCY)
                .withArguments(toExchangeArgument)
                .withArguments(BaseArguments.CURRENCY)
                .withArguments(toReceiveArgument)
                .executesPlayer((sender, args) -> {
                    Currency toExchange = GemsEconomy.inst().getCurrencyManager().getCurrency(args[0].toString());
                    Currency toReceive = GemsEconomy.inst().getCurrencyManager().getCurrency(args[2].toString());
                    double toExchangeAmount = 0D;
                    double toReceiveAmount = 0D;

                    if (toExchange != null && toReceive != null) {
                        if (toExchange.isDecimalSupported() || toReceive.isDecimalSupported()) {
                            try {
                                toExchangeAmount = Double.parseDouble(args[1].toString());
                                toReceiveAmount = Double.parseDouble(args[3].toString());
                                if (toExchangeAmount <= 0D || toReceiveAmount <= 0D) {
                                    throw new NumberFormatException();
                                }
                            } catch (NumberFormatException ex) {
                                sender.sendMessage(F.invalidAmount());
                            }
                        } else {
                            try {
                                toExchangeAmount = Integer.parseInt(args[1].toString());
                                toReceiveAmount = Integer.parseInt(args[3].toString());
                                if (toExchangeAmount <= 0D || toReceiveAmount <= 0D) {
                                    throw new NumberFormatException();
                                }
                            } catch (NumberFormatException ex) {
                                sender.sendMessage(F.invalidAmount());
                            }
                        }
                        Account account = GemsEconomy.inst().getAccountManager().getAccount(sender.getName());
                        if (account != null) {
                            if (account.convert(toExchange, toExchangeAmount, toReceive, toReceiveAmount)) {
                                sender.sendMessage(F.exchangeSuccessCustom()
                                        .replace("{currencycolor}", "" + toExchange.getColor())
                                        .replace("{currEx}", toExchange.format(toExchangeAmount))
                                        .replace("{currencycolor2}", "" + toReceive.getColor())
                                        .replace("{receivedCurr}", toReceive.format(toReceiveAmount)));
                            }
                        }
                    } else {
                        sender.sendMessage(F.unknownCurrency());
                    }
                })
                .register();
        new CommandAPICommand(NAME)
                .withPermission(PERM_EXCHANGE)
                .withPermission(PERM_EXCHANGE_CUSTOM_OTHER)
                .withArguments(BaseArguments.ACCOUNT)
                .withArguments(BaseArguments.CURRENCY)
                .withArguments(toExchangeArgument)
                .withArguments(BaseArguments.CURRENCY)
                .withArguments(toReceiveArgument)
                .executesPlayer((sender, args) -> {
                    Account account = GemsEconomy.inst().getAccountManager().getAccount(args[0].toString());
                    if (account == null) {
                        sender.sendMessage(F.playerDoesNotExist());
                        return;
                    }
                    Currency toExchange = GemsEconomy.inst().getCurrencyManager().getCurrency(args[1].toString());
                    Currency toReceive = GemsEconomy.inst().getCurrencyManager().getCurrency(args[3].toString());
                    double toExchangeAmount = 0D;
                    double toReceiveAmount = 0D;

                    if (toExchange != null && toReceive != null) {
                        if (toExchange.isDecimalSupported() || toReceive.isDecimalSupported()) {
                            try {
                                toExchangeAmount = Double.parseDouble(args[2].toString());
                                toReceiveAmount = Double.parseDouble(args[4].toString());
                                if (toExchangeAmount <= 0D || toReceiveAmount <= 0D) {
                                    throw new NumberFormatException();
                                }
                            } catch (NumberFormatException ex) {
                                sender.sendMessage(F.invalidAmount());
                            }
                        } else {
                            try {
                                toExchangeAmount = Integer.parseInt(args[2].toString());
                                toReceiveAmount = Integer.parseInt(args[4].toString());
                                if (toExchangeAmount <= 0D || toReceiveAmount <= 0D) {
                                    throw new NumberFormatException();
                                }
                            } catch (NumberFormatException ex) {
                                sender.sendMessage(F.invalidAmount());
                            }
                        }

                        if (account.convert(toExchange, toExchangeAmount, toReceive, toReceiveAmount)) {
                            sender.sendMessage(F.exchangeSuccessCustomOther()
                                    .replace("{player}", account.getDisplayName())
                                    .replace("{currencycolor}", "" + toExchange.getColor())
                                    .replace("{currEx}", toExchange.format(toExchangeAmount))
                                    .replace("{currencycolor2}", "" + toReceive.getColor())
                                    .replace("{receivedCurr}", toReceive.format(toReceiveAmount)));
                        }
                    } else {
                        sender.sendMessage(F.unknownCurrency());
                    }
                })
                .register();
    }

}

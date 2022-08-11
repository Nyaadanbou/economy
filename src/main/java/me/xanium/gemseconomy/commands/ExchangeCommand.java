package me.xanium.gemseconomy.commands;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.file.F;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ExchangeCommand implements CommandExecutor {

    private final GemsEconomy plugin = GemsEconomy.inst();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String v21315, String[] args) {
        SchedulerUtils.runAsync(() -> {
            if (!sender.hasPermission("gemseconomy.command.exchange")) {
                sender.sendMessage(F.noPerms());
                return;
            }

            if (args.length == 0) {
                F.exchangeHelp(sender);
            } else if (args.length == 3) {

                if (!sender.hasPermission("gemseconomy.command.exchange.preset")) {
                    sender.sendMessage(F.exchangeNoPermPreset());
                    return;
                }
                Currency toExchange = plugin.getCurrencyManager().getCurrency(args[0]);
                Currency toReceive = plugin.getCurrencyManager().getCurrency(args[2]);
                double amount;

                if (toExchange != null && toReceive != null) {
                    if (toReceive.isDecimalSupported()) {
                        try {
                            amount = Double.parseDouble(args[1]);
                            if (amount <= 0.0) {
                                throw new NumberFormatException();
                            }
                        } catch (NumberFormatException ex) {
                            sender.sendMessage(F.invalidAmount());
                            return;
                        }
                    } else {
                        try {
                            amount = Integer.parseInt(args[1]);
                            if (amount <= 0.0) {
                                throw new NumberFormatException();
                            }
                        } catch (NumberFormatException ex) {
                            sender.sendMessage(F.invalidAmount());
                            return;
                        }
                    }
                    Account account = plugin.getAccountManager().getAccount(sender.getName());
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

            } else if (args.length == 4) {
                if (!sender.hasPermission("gemseconomy.command.exchange.custom")) {
                    sender.sendMessage(F.exchangeNoPermCustom());
                    return;
                }
                Currency toExchange = plugin.getCurrencyManager().getCurrency(args[0]);
                Currency toReceive = plugin.getCurrencyManager().getCurrency(args[2]);
                double toExchangeAmount = 0.0;
                double toReceiveAmount = 0.0;

                if (toExchange != null && toReceive != null) {
                    if (toExchange.isDecimalSupported() || toReceive.isDecimalSupported()) {
                        try {
                            toExchangeAmount = Double.parseDouble(args[1]);
                            toReceiveAmount = Double.parseDouble(args[3]);
                            if (toExchangeAmount <= 0.0 || toReceiveAmount <= 0) {
                                throw new NumberFormatException();
                            }
                        } catch (NumberFormatException ex) {
                            sender.sendMessage(F.invalidAmount());
                        }
                    } else {
                        try {
                            toExchangeAmount = Integer.parseInt(args[1]);
                            toReceiveAmount = Integer.parseInt(args[3]);
                            if (toExchangeAmount <= 0.0 || toReceiveAmount <= 0) {
                                throw new NumberFormatException();
                            }
                        } catch (NumberFormatException ex) {
                            sender.sendMessage(F.invalidAmount());
                        }
                    }
                    Account account = plugin.getAccountManager().getAccount(sender.getName());
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
            } else if (args.length == 5) {
                if (!sender.hasPermission("gemseconomy.command.exchange.custom.other")) {
                    sender.sendMessage(F.exchangeNoPermCustom());
                    return;
                }
                Account account = plugin.getAccountManager().getAccount(args[0]);
                if (account == null) {
                    sender.sendMessage(F.playerDoesNotExist());
                    return;
                }
                Currency toExchange = plugin.getCurrencyManager().getCurrency(args[1]);
                Currency toReceive = plugin.getCurrencyManager().getCurrency(args[3]);
                double toExchangeAmount = 0.0;
                double toReceiveAmount = 0.0;

                if (toExchange != null && toReceive != null) {
                    if (toExchange.isDecimalSupported() || toReceive.isDecimalSupported()) {
                        try {
                            toExchangeAmount = Double.parseDouble(args[2]);
                            toReceiveAmount = Double.parseDouble(args[4]);
                            if (toExchangeAmount <= 0.0 || toReceiveAmount <= 0) {
                                throw new NumberFormatException();
                            }
                        } catch (NumberFormatException ex) {
                            sender.sendMessage(F.invalidAmount());
                        }
                    } else {
                        try {
                            toExchangeAmount = Integer.parseInt(args[2]);
                            toReceiveAmount = Integer.parseInt(args[4]);
                            if (toExchangeAmount <= 0.0 || toReceiveAmount <= 0) {
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
            } else {
                sender.sendMessage(F.unknownSubCommand());
            }
        });
        return true;
    }
}

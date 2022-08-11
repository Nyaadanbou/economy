/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.commands;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.event.GemsPayEvent;
import me.xanium.gemseconomy.file.F;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PayCommand implements CommandExecutor {

    private final GemsEconomy plugin = GemsEconomy.inst();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s13542415, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(F.noConsole());
            return true;
        }
        SchedulerUtils.runAsync(() -> {
            if (!sender.hasPermission("gemseconomy.command.pay")) {
                sender.sendMessage(F.noPerms());
                return;
            }
            if (args.length < 2) {
                sender.sendMessage(F.payUsage());
                return;
            }
            if (plugin.getCurrencyManager().getDefaultCurrency() == null) {
                sender.sendMessage(F.noDefaultCurrency());
                return;
            }

            Currency currency = plugin.getCurrencyManager().getDefaultCurrency();
            if (args.length == 3) {
                currency = plugin.getCurrencyManager().getCurrency(args[2]);
            }
            if (currency != null) {
                double amount;

                if (!currency.isPayable()) {
                    sender.sendMessage(F.currencyNotPayable().replace("{currencycolor}", "" + currency.getColor()).replace("{currency}", currency.getPlural()));
                    return;
                }
                if (!sender.hasPermission("gemseconomy.command.pay." + currency.getPlural().toLowerCase()) && !sender.hasPermission("gemseconomy.command.pay." + currency.getSingular().toLowerCase())) {
                    sender.sendMessage(F.payNoPerms().replace("{currencycolor}", "" + currency.getColor()).replace("{currency}", currency.getPlural()));
                    return;
                }
                if (currency.isDecimalSupported()) {
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
                        if (amount <= 0) {
                            throw new NumberFormatException();
                        }
                    } catch (NumberFormatException ex) {
                        sender.sendMessage(F.invalidAmount());
                        return;
                    }
                }
                Account account = plugin.getAccountManager().getAccount((Player) sender);
                if (account != null) {
                    Account target = plugin.getAccountManager().getAccount(args[0]);
                    if (target != null) {
                        if (!target.getUuid().equals(account.getUuid())) {
                            if (target.canReceiveCurrency()) {
                                if (account.hasEnough(currency, amount)) {
                                    GemsPayEvent event = new GemsPayEvent(currency, account, target, amount);
                                    SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(event));
                                    if (event.isCancelled()) return;

                                    double accBal = account.getBalance(currency) - amount;
                                    double tarBal = target.getBalance(currency) + amount;

                                    // cap the amount
                                    double cappedAccBal = Math.min(accBal, currency.getMaxBalance());
                                    double cappedTarBal = Math.min(tarBal, currency.getMaxBalance());

                                    account.modifyBalance(currency, cappedAccBal, true);
                                    target.modifyBalance(currency, cappedTarBal, true);
                                    GemsEconomy.inst().getEconomyLogger().log("[PAYMENT] " + account.getDisplayName() + " (New bal: " + currency.format(cappedAccBal) + ") -> paid " + target.getDisplayName() + " (New bal: " + currency.format(cappedTarBal) + ") - An amount of " + currency.format(amount));

                                    if (Bukkit.getPlayer(target.getUuid()) != null) {
                                        Bukkit.getPlayer(target.getUuid()).sendMessage(F.paidMessage().replace("{currencycolor}", currency.getColor() + "").replace("{amount}", currency.format(amount)).replace("{player}", sender.getName()));
                                    }
                                    sender.sendMessage(F.payerMessage().replace("{currencycolor}", currency.getColor() + "").replace("{amount}", currency.format(amount)).replace("{player}", target.getDisplayName()));
                                } else {
                                    sender.sendMessage(F.insufficientFunds().replace("{currencycolor}", "" + currency.getColor()).replace("{currency}", currency.getPlural()));
                                }
                            } else {
                                sender.sendMessage(F.cannotReceive().replace("{player}", target.getDisplayName()));
                            }
                        } else {
                            sender.sendMessage(F.payYourself());
                        }
                    } else {
                        sender.sendMessage(F.playerDoesNotExist());
                    }
                } else {
                    sender.sendMessage(F.accountMissing());
                }
            } else {
                sender.sendMessage(F.unknownCurrency());
            }
        });
        return true;
    }

}


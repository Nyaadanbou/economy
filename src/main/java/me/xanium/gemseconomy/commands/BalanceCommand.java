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
import me.xanium.gemseconomy.file.F;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BalanceCommand implements CommandExecutor {

    private final GemsEconomy plugin = GemsEconomy.inst();

    @Override
    public boolean onCommand(final @NotNull CommandSender sender, @NotNull Command command, @NotNull String s, final String[] args) {
        SchedulerUtils.runAsync(() -> {
            if (!sender.hasPermission("gemseconomy.command.balance")) {
                sender.sendMessage(F.noPerms());
                return;
            }
            Account account;
            if (args.length == 0 && sender instanceof Player) {
                account = plugin.getAccountManager().getAccount((Player) sender);
            } else if (sender.hasPermission("gemseconomy.command.balance.other") && args.length == 1) {
                account = plugin.getAccountManager().getAccount(args[0]);
            } else {
                sender.sendMessage(F.noPerms());
                return;
            }
            if (account != null) {
                int currencies = plugin.getCurrencyManager().getCurrencies().size();

                if (currencies == 0) {
                    sender.sendMessage(F.noDefaultCurrency());
                } else if (currencies == 1) {
                    Currency currency = plugin.getCurrencyManager().getDefaultCurrency();
                    if (currency == null) {
                        sender.sendMessage(F.balanceNone().replace("{player}", account.getNickname()));
                        return;
                    }
                    double balance = account.getBalance(currency);
                    sender.sendMessage(F.balance().replace("{player}", account.getDisplayName()).replace("{currencycolor}", "" + currency.getColor()).replace("{balance}", currency.format(balance)));
                } else {
                    sender.sendMessage(F.balanceMultiple().replace("{player}", account.getDisplayName()));
                    for (Currency currency : plugin.getCurrencyManager().getCurrencies()) {
                        double balance = account.getBalance(currency);
                        sender.sendMessage(F.balanceList().replace("{currencycolor}", currency.getColor() + "").replace("{format}", currency.format(balance)));
                    }
                }
            } else {
                sender.sendMessage(F.playerDoesNotExist());
            }
        });
        return true;
    }

}





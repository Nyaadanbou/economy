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
import me.xanium.gemseconomy.utils.UtilString;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ChequeCommand implements CommandExecutor {

    private final GemsEconomy plugin = GemsEconomy.inst();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if(!plugin.isChequesEnabled()){
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(F.noConsole());
            return true;
        }
        if (!player.hasPermission("gemseconomy.command.cheque")) {
            player.sendMessage(F.noPerms());
            return true;
        }
        if (args.length == 0) {
            F.chequeHelp(player);
            return true;
        }
        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("redeem")) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType().equals(Material.valueOf(plugin.getConfig().getString("cheque.material")))) {
                    if (plugin.getChequeManager().isValid(item)) {
                        double value = plugin.getChequeManager().getValue(item);
                        if (item.getAmount() > 1) {
                            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        } else {
                            player.getInventory().remove(item);
                        }
                        Account user = plugin.getAccountManager().getAccount(player);
                        Currency currency = plugin.getChequeManager().getCurrency(item);
                        user.deposit(currency, value);
                        player.sendMessage(F.chequeRedeemed());
                        return true;
                    } else {
                        player.sendMessage(F.chequeInvalid());
                    }
                } else {
                    player.sendMessage(F.chequeInvalid());
                }
            } else {
                player.sendMessage(F.unknownSubCommand());
            }
        }

        if (args.length >= 2) {
            if (args[0].equalsIgnoreCase("write")) {

                if (UtilString.validateInput(sender, args[1])) {

                    double amount = Double.parseDouble(args[1]);
                    if (amount != 0) {

                        if (args.length == 3) {

                            Currency currency = plugin.getCurrencyManager().getCurrency(args[2]);
                            Account user = plugin.getAccountManager().getAccount(player);
                            if (currency != null) {
                                if(user.hasEnough(currency, amount)) {
                                    user.withdraw(currency, amount);
                                    player.getInventory().addItem(plugin.getChequeManager().write(player.getName(), currency, amount));
                                    player.sendMessage(F.chequeSuccess());
                                    return true;
                                }else{
                                    player.sendMessage(F.insufficientFunds().replace("{currencycolor}", currency.getColor() + "").replace("{currency}", currency.getSingular()));
                                }
                            } else {
                                player.sendMessage(F.unknownCurrency());
                            }
                        }

                        Currency defaultCurrency = plugin.getCurrencyManager().getDefaultCurrency();
                        Account user = plugin.getAccountManager().getAccount(player);
                        if(user.hasEnough(amount)) {
                            user.withdraw(plugin.getCurrencyManager().getDefaultCurrency(), amount);
                            player.getInventory().addItem(plugin.getChequeManager().write(player.getName(), plugin.getCurrencyManager().getDefaultCurrency(), amount));
                            player.sendMessage(F.chequeSuccess());
                            return true;
                        }else{
                            player.sendMessage(F.insufficientFunds().replace("{currencycolor}", defaultCurrency.getColor() + "").replace("{currency}", defaultCurrency.getSingular()));
                        }
                    } else {
                        player.sendMessage(F.invalidAmount());
                    }
                } else {
                    player.sendMessage(F.invalidAmount());
                }
            } else {
                player.sendMessage(F.unknownSubCommand());
            }
        }
        return true;
    }
}

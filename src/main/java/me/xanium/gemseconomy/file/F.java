/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.file;

import me.xanium.gemseconomy.GemsEconomy;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;

public class F {

    private static final GemsEconomy plugin = GemsEconomy.getInstance();
    private static final FileConfiguration cfg = plugin.getConfig();

    private static String get(String path) {
        return colorize(cfg.getString(path));
    }

    private static List<String> getList(String path) {
        List<String> str = new ArrayList<>();
        for (String s : cfg.getStringList(path)) {
            str.add(colorize(s));
        }
        return str;
    }

    private static String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String getPrefix() {
        return colorize(cfg.getString("messages.prefix"));
    }

    public static String getNoPerms() {
        return getPrefix() + colorize(cfg.getString("messages.nopermission"));
    }

    public static String getNoConsole() {
        return getPrefix() + colorize(cfg.getString("messages.noconsole"));
    }

    public static String getInsufficientFunds() {
        return getPrefix() + colorize(cfg.getString("messages.insufficientFunds"));
    }

    public static String getTargetInsufficientFunds() {
        return getPrefix() + colorize(cfg.getString("messages.targetInsufficientFunds"));
    }

    public static String getPayerMessage() {
        return getPrefix() + colorize(cfg.getString("messages.payer"));
    }

    public static String getPaidMessage() {
        return getPrefix() + colorize(cfg.getString("messages.paid"));
    }

    public static String getPayUsage() {
        return colorize(cfg.getString("messages.usage.pay_command"));
    }

    public static String getAddMessage() {
        return getPrefix() + colorize(cfg.getString("messages.add"));
    }

    public static String getTakeMessage() {
        return getPrefix() + colorize(cfg.getString("messages.take"));
    }

    public static String getSetMessage() {
        return getPrefix() + colorize(cfg.getString("messages.set"));
    }

    public static String getPlayerDoesNotExist() {
        return getPrefix() + colorize(cfg.getString("messages.player_is_null"));
    }

    public static String getPayYourself() {
        return getPrefix() + colorize(cfg.getString("messages.pay_yourself"));
    }

    public static String getUnknownCurrency() {
        return getPrefix() + colorize(cfg.getString("messages.unknownCurrency"));
    }

    public static String getUnknownSubCommand() {
        return getPrefix() + colorize(cfg.getString("messages.unknownCommand"));
    }

    public static void getManageHelp(CommandSender sender) {
        for (String s : cfg.getStringList("messages.help.eco_command")) {
            sender.sendMessage(colorize(s.replace("{prefix}", getPrefix())));
        }
    }

    public static void getExchangeHelp(CommandSender sender) {
        for (String s : cfg.getStringList("messages.help.exchange_command")) {
            sender.sendMessage(colorize(s.replace("{prefix}", getPrefix())));
        }
    }

    public static String getBalance() {
        return getPrefix() + colorize(cfg.getString("messages.balance.current"));
    }

    public static String getBalanceMultiple() {
        return getPrefix() + colorize(cfg.getString("messages.balance.multiple"));
    }

    public static String getBalanceList() {
        return colorize(cfg.getString("messages.balance.list"));
    }

    public static String getUnvalidAmount() {
        return getPrefix() + colorize(cfg.getString("messages.invalidamount"));
    }

    public static String getUnvalidPage() {
        return getPrefix() + colorize(cfg.getString("messages.invalidpage"));
    }

    public static void getChequeHelp(CommandSender sender) {
        for (String s : cfg.getStringList("messages.help.cheque_command")) {
            sender.sendMessage(colorize(s.replace("{prefix}", getPrefix())));
        }
    }

    public static String getChequeSucess() {
        return getPrefix() + colorize(cfg.getString("messages.cheque.success"));
    }

    public static String getChequeRedeemed() {
        return getPrefix() + colorize(cfg.getString("messages.cheque.redeemed"));
    }

    public static String getChequeInvalid() {
        return getPrefix() + colorize(cfg.getString("messages.cheque.invalid"));
    }

    public static String getGiveUsage() {
        return colorize(cfg.getString("messages.usage.give_command"));
    }

    public static String getTakeUsage() {
        return colorize(cfg.getString("messages.usage.take_command"));
    }

    public static String getSetUsage() {
        return colorize(cfg.getString("messages.usage.set_command"));
    }

    public static String getBalanceTopHeader() {
        return colorize(cfg.getString("messages.balance_top.header"));
    }

    public static String getBalanceTopEmpty() {
        return colorize(cfg.getString("messages.balance_top.empty"));
    }

    public static String getBalanceTopNext() {
        return colorize(cfg.getString("messages.balance_top.next"));
    }

    public static String getBalanceTop() {
        return colorize(cfg.getString("messages.balance_top.balance"));
    }

    public static String getNoDefaultCurrency() {
        return getPrefix() + colorize(cfg.getString("messages.noDefaultCurrency"));
    }

    public static String getBalanceNone() {
        return getPrefix() + colorize(cfg.getString("messages.balance.none"));
    }

    public static String getBalanceTopNoSupport() {
        return getPrefix() + colorize(cfg.getString("messages.balance_top.nosupport"));
    }

    public static String getPayNoPerms() {
        return getPrefix() + colorize(cfg.getString("messages.payNoPermission"));
    }

    public static String getCurrencyNotPayable() {
        return getPrefix() + colorize(cfg.getString("messages.currencyNotPayable"));
    }

    public static String getAccountMissing() {
        return getPrefix() + colorize(cfg.getString("messages.accountMissing"));
    }

    public static String getCannotReceive() {
        return getPrefix() + colorize(cfg.getString("messages.cannotReceiveMoney"));
    }


    public static String getCurrencyUsage_Create() {
        return get("messages.usage.currency_create");
    }

    public static String getCurrencyUsage_Delete() {
        return get("messages.usage.currency_delete");
    }

    public static String getCurrencyUsage_View() {
        return get("messages.usage.currency_view");
    }

    public static String getCurrencyUsage_Default() {
        return get("messages.usage.currency_default");
    }

    public static String getCurrencyUsage_List() {
        return get("messages.usage.currency_list");
    }

    public static String getCurrencyUsage_Color() {
        return get("messages.usage.currency_color");
    }

    public static String getCurrencyUsage_Colorlist() {
        return get("messages.usage.currency_colorlist");
    }

    public static String getCurrencyUsage_Payable() {
        return get("messages.usage.currency_payable");
    }

    public static String getCurrencyUsage_Startbal() {
        return get("messages.usage.currency_startbal");
    }

    public static String getCurrencyUsage_Maxbal() {
        return get("messages.usage.currency_maxbal");
    }

    public static String getCurrencyUsage_Decimals() {
        return get("messages.usage.currency_decimals");
    }

    public static String getCurrencyUsage_Symbol() {
        return get("messages.usage.currency_symbol");
    }

    public static String getCurrencyUsage_Rate() {
        return get("messages.usage.currency_setrate");
    }

    public static String getCurrencyUsage_Backend() {
        return get("messages.usage.currency_backend");
    }

    public static String getCurrencyUsage_Convert() {
        return get("messages.usage.currency_convert");
    }

    public static void sendCurrencyUsage(CommandSender sender) {
        for (String s : getList("messages.help.currency_command")) {
            sender.sendMessage(s.replace("{prefix}", getPrefix()));
        }
    }

    public static String getExchangeSuccess() {
        return getPrefix() + colorize(cfg.getString("messages.exchange_success"));
    }

    public static String getExchangeSuccessCustom() {
        return getPrefix() + colorize(cfg.getString("messages.exchange_success_custom"));
    }

    public static String getExchangeSuccessCustomOther() {
        return getPrefix() + colorize(cfg.getString("messages.exchange_success_custom_other"));
    }

    public static String getExchangeRateSet() {
        return getPrefix() + colorize(cfg.getString("messages.exchange_rate_set"));
    }

    public static String getExchangeNoPermCustom() {
        return getPrefix() + colorize(cfg.getString("messages.exchange_command.no_perms.custom"));
    }

    public static String getExchangeNoPermPreset() {
        return getPrefix() + colorize(cfg.getString("messages.exchange_command.no_perms.preset"));
    }

    public static String getDebugStatus() {
        return getPrefix() + colorize(cfg.getString("messages.debug_command.current_status"));
    }

}

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

    private static final GemsEconomy plugin = GemsEconomy.inst();
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

    public static String consoleName() {
        return colorize(cfg.getString("console_name"));
    }

    public static String prefix() {
        return colorize(cfg.getString("messages.prefix"));
    }

    public static String noPerms() {
        return prefix() + colorize(cfg.getString("messages.nopermission"));
    }

    public static String noConsole() {
        return prefix() + colorize(cfg.getString("messages.noconsole"));
    }

    public static String insufficientFunds() {
        return prefix() + colorize(cfg.getString("messages.insufficientFunds"));
    }

    public static String targetInsufficientFunds() {
        return prefix() + colorize(cfg.getString("messages.targetInsufficientFunds"));
    }

    public static String payerMessage() {
        return prefix() + colorize(cfg.getString("messages.payer"));
    }

    public static String paidMessage() {
        return prefix() + colorize(cfg.getString("messages.paid"));
    }

    public static String payUsage() {
        return colorize(cfg.getString("messages.usage.pay_command"));
    }

    public static String addMessage() {
        return prefix() + colorize(cfg.getString("messages.add"));
    }

    public static String takeMessage() {
        return prefix() + colorize(cfg.getString("messages.take"));
    }

    public static String setMessage() {
        return prefix() + colorize(cfg.getString("messages.set"));
    }

    public static String playerDoesNotExist() {
        return prefix() + colorize(cfg.getString("messages.player_is_null"));
    }

    public static String payYourself() {
        return prefix() + colorize(cfg.getString("messages.pay_yourself"));
    }

    public static String unknownCurrency() {
        return prefix() + colorize(cfg.getString("messages.unknownCurrency"));
    }

    public static String unknownSubCommand() {
        return prefix() + colorize(cfg.getString("messages.unknownCommand"));
    }

    public static void manageHelp(CommandSender sender) {
        for (String s : cfg.getStringList("messages.help.eco_command")) {
            sender.sendMessage(colorize(s.replace("{prefix}", prefix())));
        }
    }

    public static void exchangeHelp(CommandSender sender) {
        for (String s : cfg.getStringList("messages.help.exchange_command")) {
            sender.sendMessage(colorize(s.replace("{prefix}", prefix())));
        }
    }

    public static String balance() {
        return prefix() + colorize(cfg.getString("messages.balance.current"));
    }

    public static String balanceMultiple() {
        return prefix() + colorize(cfg.getString("messages.balance.multiple"));
    }

    public static String balanceList() {
        return colorize(cfg.getString("messages.balance.list"));
    }

    public static String invalidAmount() {
        return prefix() + colorize(cfg.getString("messages.invalidamount"));
    }

    public static String invalidPage() {
        return prefix() + colorize(cfg.getString("messages.invalidpage"));
    }

    public static void chequeHelp(CommandSender sender) {
        for (String s : cfg.getStringList("messages.help.cheque_command")) {
            sender.sendMessage(colorize(s.replace("{prefix}", prefix())));
        }
    }

    public static String chequeSuccess() {
        return prefix() + colorize(cfg.getString("messages.cheque.success"));
    }

    public static String chequeRedeemed() {
        return prefix() + colorize(cfg.getString("messages.cheque.redeemed"));
    }

    public static String chequeInvalid() {
        return prefix() + colorize(cfg.getString("messages.cheque.invalid"));
    }

    public static String giveUsage() {
        return colorize(cfg.getString("messages.usage.give_command"));
    }

    public static String takeUsage() {
        return colorize(cfg.getString("messages.usage.take_command"));
    }

    public static String setUsage() {
        return colorize(cfg.getString("messages.usage.set_command"));
    }

    public static String balanceTopHeader() {
        return colorize(cfg.getString("messages.balance_top.header"));
    }

    public static String balanceTopEmpty() {
        return colorize(cfg.getString("messages.balance_top.empty"));
    }

    public static String balanceTopNext() {
        return colorize(cfg.getString("messages.balance_top.next"));
    }

    public static String balanceTop() {
        return colorize(cfg.getString("messages.balance_top.balance"));
    }

    public static String noDefaultCurrency() {
        return prefix() + colorize(cfg.getString("messages.noDefaultCurrency"));
    }

    public static String balanceNone() {
        return prefix() + colorize(cfg.getString("messages.balance.none"));
    }

    public static String balanceTopNoSupport() {
        return prefix() + colorize(cfg.getString("messages.balance_top.nosupport"));
    }

    public static String payNoPerms() {
        return prefix() + colorize(cfg.getString("messages.payNoPermission"));
    }

    public static String currencyNotPayable() {
        return prefix() + colorize(cfg.getString("messages.currencyNotPayable"));
    }

    public static String accountMissing() {
        return prefix() + colorize(cfg.getString("messages.accountMissing"));
    }

    public static String cannotReceive() {
        return prefix() + colorize(cfg.getString("messages.cannotReceiveMoney"));
    }

    public static String currencyOverflow() {
        return prefix() + colorize(cfg.getString("messages.currencyOverflow"));
    }

    public static String currencyUsageCreate() {
        return get("messages.usage.currency_create");
    }

    public static String currencyUsageDelete() {
        return get("messages.usage.currency_delete");
    }

    public static String currencyUsageView() {
        return get("messages.usage.currency_view");
    }

    public static String currencyUsageDefault() {
        return get("messages.usage.currency_default");
    }

    public static String currencyUsageList() {
        return get("messages.usage.currency_list");
    }

    public static String currencyUsageColor() {
        return get("messages.usage.currency_color");
    }

    public static String currencyUsageColorlist() {
        return get("messages.usage.currency_colorlist");
    }

    public static String currencyUsagePayable() {
        return get("messages.usage.currency_payable");
    }

    public static String currencyUsageStartbal() {
        return get("messages.usage.currency_startbal");
    }

    public static String currencyUsageMaxbal() {
        return get("messages.usage.currency_maxbal");
    }

    public static String currencyUsageDecimals() {
        return get("messages.usage.currency_decimals");
    }

    public static String currencyUsageSymbol() {
        return get("messages.usage.currency_symbol");
    }

    public static String currencyUsageRate() {
        return get("messages.usage.currency_setrate");
    }

    public static String currencyUsageBackend() {
        return get("messages.usage.currency_backend");
    }

    public static String currencyUsageConvert() {
        return get("messages.usage.currency_convert");
    }

    public static void currencyUsage(CommandSender sender) {
        for (String s : getList("messages.help.currency_command")) {
            sender.sendMessage(s.replace("{prefix}", prefix()));
        }
    }

    public static String exchangeSuccess() {
        return prefix() + colorize(cfg.getString("messages.exchange_success"));
    }

    public static String exchangeSuccessCustom() {
        return prefix() + colorize(cfg.getString("messages.exchange_success_custom"));
    }

    public static String exchangeSuccessCustomOther() {
        return prefix() + colorize(cfg.getString("messages.exchange_success_custom_other"));
    }

    public static String exchangeRateSet() {
        return prefix() + colorize(cfg.getString("messages.exchange_rate_set"));
    }

    public static String exchangeNoPermCustom() {
        return prefix() + colorize(cfg.getString("messages.exchange_command.no_perms.custom"));
    }

    public static String exchangeNoPermPreset() {
        return prefix() + colorize(cfg.getString("messages.exchange_command.no_perms.preset"));
    }

    public static String debugStatus() {
        return prefix() + colorize(cfg.getString("messages.debug_command.current_status"));
    }

}

/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.file;

import me.xanium.gemseconomy.GemsEconomy;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.List;

public class Configuration {

    private final GemsEconomy plugin;

    public Configuration(GemsEconomy plugin) {
        this.plugin = plugin;
    }

    public void loadDefaultConfig() {

        FileConfiguration config = plugin.getConfig();

        config.options().setHeader(List.of(
                "",
                "Version: " + plugin.getDescription().getVersion(),
                "GemsEconomy Main Configuration file.",
                "Developer(s): " + plugin.getDescription().getAuthors(),
                "",
                "You have two valid storage methods, yaml, mysql. If you choose mysql you would have to enter the database credentials down below.",
                "",
                "All messages below are configurable, I hope you use them because it took 1 hour to make all of them into the plugin and configurable."
        ));

        String path = "messages.";

        config.addDefault("storage", "mysql");
        config.addDefault("debug", true);
        config.addDefault("vault", true);
        config.addDefault("transaction_log", true);

        config.addDefault("mysql.database", "test");
        config.addDefault("mysql.tableprefix", "gemseconomy");
        config.addDefault("mysql.host", "localhost");
        config.addDefault("mysql.port", 3306);
        config.addDefault("mysql.username", "minecraft");
        config.addDefault("mysql.password", "");

        config.addDefault("console_name", "控制台");

        config.addDefault("cheque.material", Material.PAPER.toString());
        config.addDefault("cheque.name", "&a支票");
        config.addDefault("cheque.lore", List.of("&7价值: {value}.", "&7发行人: {player}"));
        config.addDefault("cheque.enabled", true);

        config.addDefault(path + "debug_command.current_status", "&7当前调试模式: &a{status}&7.");

        config.addDefault(path + "prefix", "&8[&2$&8] ");
        config.addDefault(path + "nopermission", "&7你没有权限这么做.");
        config.addDefault(path + "noconsole", "&7控制台不能这么做.");
        config.addDefault(path + "invalidamount", "&7无效数值.");
        config.addDefault(path + "invalidpage", "&7无效页数.");
        config.addDefault(path + "pay_yourself", "&7你不能给自己转账.");
        config.addDefault(path + "player_is_null", "&7指定玩家不存在.");
        config.addDefault(path + "unknownCurrency", "&7未知货币.");
        config.addDefault(path + "unknownCommand", "&7未知子命令.");
        config.addDefault(path + "noDefaultCurrency", "&7无默认货币.");
        config.addDefault(path + "currencyExists", "&7货币已经存在.");
        config.addDefault(path + "accountMissing", "&7你的账户已丢失, 请重登服务器.");
        config.addDefault(path + "cannotReceiveMoney", "&a{player} &7不能收取货币.");
        config.addDefault(path + "insufficientFunds", "&7你没有足够的 {currencycolor}{currency}&7!");
        config.addDefault(path + "targetInsufficientFunds", "&e{target} &7没有足够的 {currencycolor}{currency}&7!");
        config.addDefault(path + "paid", "&7你收到了来自 &a{player} &7的 {currencycolor}{amount}&7.");
        config.addDefault(path + "payer", "&7你给 &a{player} &7支付了 {currencycolor}{amount}&7.");
        config.addDefault(path + "payNoPermission", "&7你给 &a{player} &7支付了 {currencycolor}{amount}&7.");
        config.addDefault(path + "currencyNotPayable", "&7你没有权限支付 {currencycolor}{currency}&7.");
        config.addDefault(path + "currencyOverflow", "&a{player} &7的 {currencycolor}{currency} &7已达到上限.");
        config.addDefault(path + "add", "&7你给了 &a{player}&7: {currencycolor}{amount}.");
        config.addDefault(path + "take", "&7你从 &a{player} &7拿走了 {currencycolor}{amount}&7.");
        config.addDefault(path + "set", "&7你将 &a{player} &7的余额设置为 {currencycolor}{amount}&7.");

        config.addDefault(path + "exchange_rate_set", "&7设置 {currencycolor}{currency} &7的汇率为 &a{amount}&7.");
        config.addDefault(path + "exchange_success_custom_other", "&7成功将 {currencycolor}({currEx}) &7换为了 {currencycolor2}{receivedCurr} &7给予玩家 &a{player}&7.");
        config.addDefault(path + "exchange_success_custom", "&7成功将 {currencycolor}({currEx}) &7换为了 {currencycolor2}{receivedCurr}&7.");
        config.addDefault(path + "exchange_success", "&7成功将 {currencycolor}{ex_curr} &7换为了 {currencycolor2}{re_curr}&7.");
        config.addDefault(path + "exchange_command.no_perms.preset", "&7你没有权限用预设的汇率兑换货币.");
        config.addDefault(path + "exchange_command.no_perms.custom", "&7你没有权限用自定义汇率兑换货币.");

        config.addDefault(path + "balance.current", "&a{player} &7的余额: {currencycolor}{balance}");
        config.addDefault(path + "balance.multiple", "&a{player} &7的余额:");
        config.addDefault(path + "balance.list", "    &8▶ {currencycolor}{format}");
        config.addDefault(path + "balance.none", "&7&c{player} 没有余额可供展示&7.");

        config.addDefault(path + "balance_top.balance", "&a&l-> {number}. {currencycolor}{player} &7- {currencycolor}{balance}");
        config.addDefault(path + "balance_top.header", "&f----- {currencycolor} 货币 {currencyplural} 的财富榜 &7(页码 {page})&f -----");
        config.addDefault(path + "balance_top.empty", "&7没有要显示的账户.");
        config.addDefault(path + "balance_top.next", "{currencycolor}/baltop {currencyplural} {page} &7查询更多.");
        config.addDefault(path + "balance_top.nosupport", "&a{storage} &7不支持 /baltop.");

        config.addDefault(path + "cheque.success", "&7成功开出支票.");
        config.addDefault(path + "cheque.redeemed", "&7成功兑现支票.");
        config.addDefault(path + "cheque.invalid", "&7这是张无效支票.");

        config.addDefault(path + "help.eco_command", List.of(
                "{prefix}&e&lEconomy Help",
                "&8▶ &a/eco give <user> <amount> [currency] &8- &7Give a player an amount of a currency.",
                "&8▶ &a/eco take <user> <amount> [currency] &8- &7Take an amount of a currency from a player.",
                "&8▶ &a/eco set <user> <amount> [currency] &8- &7Set a players amount of a currency."));

        config.addDefault(path + "help.exchange_command", List.of(
                "{prefix}&b&lExchange Help",
                "&8▶ &a/exchange <account> <currency_to_exchange> <amount> <currency_to_receive> <amount> &8- &7Exchange between currencies with a custom rate for an account.",
                "&8▶ &a/exchange <currency_to_exchange> <amount> <currency_to_receive> <amount> &8- &7Exchange between currencies with a custom rate.",
                "&8▶ &a/exchange <currency_to_exchange> <amount> <currency_to_receive> &8- &7Exchange with a pre-set exchange rate."));

        config.addDefault(path + "usage.pay_command", "&8▶ &a/pay <user> <amount> [currency] &8- &7Pay the specified user the specified amount.");
        config.addDefault(path + "usage.give_command", "&8▶ &a/eco give <user> <amount> [currency] &8- &7Give a player an amount of a currency.");
        config.addDefault(path + "usage.take_command", "&8▶ &a/eco take <user> <amount> [currency] &8- &7Take an amount of a currency from a player.");
        config.addDefault(path + "usage.set_command", "&8▶ &a/eco set <user> <amount> [currency] &8- &7Set a players amount of a currency.");

        config.addDefault(path + "help.cheque_command", List.of("{prefix}&e&lCheque Help",
                "&8▶ &a/cheque write <amount> [currency] &8- &7Write a cheque with a specified amount and currency.",
                "&8▶ &a/cheque redeem &8- &7Redeem the cheque."));

        config.addDefault(path + "help.currency_command", List.of("{prefix}&e&lCurrency Help",
                "&8▶ &a/currency create <singular> <plural> &8- &7Create a currency.",
                "&8▶ &a/currency delete <plural> &8- &7Delete a currency.",
                "&8▶ &a/currency convert <method> &8- &7Convert storage method. WARN: Take backups first and make sure the storage you are switching to is empty!",
                "&8▶ &a/currency backend <method> &8- &7Switch backend. This does not convert.",
                "&8▶ &a/currency view <plural> &8- &7View information about a currency.",
                "&8▶ &a/currency list &8- &7List of currencies.",
                "&8▶ &a/currency symbol <plural> <char|remove> &8- &7Select a symbol for a currency or remove it.",
                "&8▶ &a/currency color <plural> <color> &8- &7Select a color for a currency.",
                "&8▶ &a/currency colorlist &8- &7List of Colors.",
                "&8▶ &a/currency decimals <plural> &8- &7Enable decimals for a currency.",
                "&8▶ &a/currency payable <plural> &8- &7Set a currency payable or not.",
                "&8▶ &a/currency default <plural> &8- &7Set a currency as default.",
                "&8▶ &a/currency startbal <plural> <amount> &8- &7Set the starting balance for a currency.",
                "&8▶ &a/currency maxbal <plural> <amount> &8- &7Set the maximum balance for a currency.",
                "&8▶ &a/currency setrate <plural> <amount> &8- &7Sets the currency's exchange rate."));

        config.addDefault(path + "usage.currency_create", "&8▶ &a/currency create <singular> <plural> &8- &7Create a currency.");
        config.addDefault(path + "usage.currency_delete", "&8▶ &a/currency delete <plural> &8- &7Delete a currency.");
        config.addDefault(path + "usage.currency_convert", "&8▶ &a/currency convert <method> &8- &7Convert storage method. WARN: Take backups first and make sure the storage you are switching to is empty!");
        config.addDefault(path + "usage.currency_backend", "&8▶ &a/currency backend <method> &8- &7Switch backend. This does not convert.");
        config.addDefault(path + "usage.currency_view", "&8▶ &a/currency view <plural> &8- &7View information about a currency.");
        config.addDefault(path + "usage.currency_list", "&8▶ &a/currency list &8- &7List of currencies.");
        config.addDefault(path + "usage.currency_symbol", "&8▶ &a/currency symbol <plural> <char|remove> &8- &7Select a symbol for a currency or remove it.");
        config.addDefault(path + "usage.currency_color", "&8▶ &a/currency color <plural> <color> &8- &7Select a color for a currency.");
        config.addDefault(path + "usage.currency_colorlist", "&8▶ &a/currency colorlist &8- &7List of Colors.");
        config.addDefault(path + "usage.currency_payable", "&8▶ &a/currency payable <plural> &8- &7Set a currency payable or not.");
        config.addDefault(path + "usage.currency_default", "&8▶ &a/currency default <plural> &8- &7Set a currency as default.");
        config.addDefault(path + "usage.currency_decimals", "&8▶ &a/currency decimals <plural> &8- &7Enable decimals for a currency.");
        config.addDefault(path + "usage.currency_startbal", "&8▶ &a/currency startbal <plural> <amount> &8- &7Set the starting balance for a currency.");
        config.addDefault(path + "usage.currency_maxbal", "&8▶ &a/currency maxbal <plural> <amount> &8- &7Set the maximum balance for a currency.");
        config.addDefault(path + "usage.currency_setrate", "&8▶ &a/currency setrate <plural> <amount> &8- &7Sets the currency's exchange rate.");

        config.options().copyDefaults(true);
        plugin.saveConfig();
        plugin.reloadConfig();
    }

}

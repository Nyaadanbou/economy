/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.api;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.currency.Currency;

import java.util.UUID;

@SuppressWarnings("unused")
public class GemsEconomyAPI {

    public final GemsEconomy plugin = GemsEconomy.getInstance();

    public GemsEconomyAPI() {
        if (plugin.getCurrencyManager().getDefaultCurrency() == null) {
            GemsEconomy.getInstance().getLogger().warning("||");
            GemsEconomy.getInstance().getLogger().warning("||");
            GemsEconomy.getInstance().getLogger().warning("||");
            GemsEconomy.getInstance().getLogger().warning("There is no default currency, so therefore none of the API will work!!!");
            GemsEconomy.getInstance().getLogger().warning("There is no default currency, so therefore none of the API will work!!!");
            GemsEconomy.getInstance().getLogger().warning("||");
            GemsEconomy.getInstance().getLogger().warning("||");
            GemsEconomy.getInstance().getLogger().warning("||");
        }
    }

    public Account pullAccount(UUID uuid) {
        return plugin.getAccountManager().getAccount(uuid);
    }

    /**
     * @param uuid   - the user's unique ID
     * @param amount - an amount of the default Currency
     */
    public void deposit(UUID uuid, double amount) {
        Account acc = pullAccount(uuid);
        acc.deposit(plugin.getCurrencyManager().getDefaultCurrency(), amount);
    }

    /**
     * @param uuid     - the user's unique ID
     * @param amount   - an amount of a Currency, if the Currency is null, the default will be used
     * @param currency - a specified Currency
     */
    public void deposit(UUID uuid, double amount, Currency currency) {
        Account acc = pullAccount(uuid);
        if (currency != null) {
            acc.deposit(currency, amount);
        } else {
            acc.deposit(plugin.getCurrencyManager().getDefaultCurrency(), amount);
        }
    }

    /**
     * @param uuid   - the user's unique ID
     * @param amount - an amount of the default Currency
     */
    public void withdraw(UUID uuid, double amount) {
        Account acc = pullAccount(uuid);
        acc.withdraw(plugin.getCurrencyManager().getDefaultCurrency(), amount);
    }

    /**
     * @param uuid     - the user's unique ID
     * @param amount   - an amount of the currency
     * @param currency - the Currency you withdraw from
     */
    public void withdraw(UUID uuid, double amount, Currency currency) {
        Account acc = pullAccount(uuid);
        if (currency != null) {
            acc.withdraw(currency, amount);
        } else {
            acc.withdraw(plugin.getCurrencyManager().getDefaultCurrency(), amount);
        }
    }

    /**
     * @param uuid - the user's unique ID
     *
     * @return - the default Currency balance of the user
     */
    public double getBalance(UUID uuid) {
        Account acc = pullAccount(uuid);
        return acc.getBalance(plugin.getCurrencyManager().getDefaultCurrency());
    }

    /**
     * @param uuid     - the user's unique ID
     * @param currency - an amount of the default Currency
     *
     * @return - the balance of the specified Currency
     */
    public double getBalance(UUID uuid, Currency currency) {
        Account acc = pullAccount(uuid);
        if (currency != null) {
            return acc.getBalance(currency);
        } else {
            return acc.getBalance(plugin.getCurrencyManager().getDefaultCurrency());
        }
    }

    /**
     * @param name - currency singular or plural
     *
     * @return - Currency object
     */
    public Currency getCurrency(String name) {
        if (plugin.getCurrencyManager().getCurrency(name) != null) {
            return plugin.getCurrencyManager().getCurrency(name);
        }
        return null;
    }

}

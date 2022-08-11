/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.account;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.event.GemsConversionEvent;
import me.xanium.gemseconomy.event.GemsPostTransactionEvent;
import me.xanium.gemseconomy.event.GemsPreTransactionEvent;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import me.xanium.gemseconomy.utils.TranactionType;
import me.xanium.gemseconomy.utils.UtilServer;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Account {

    private final UUID uuid;
    private String nickname;
    private final Map<Currency, Double> balances;
    private boolean canReceiveCurrency = true;

    public Account(UUID uuid, String nickname) {
        this.uuid = uuid;
        this.nickname = nickname;
        this.balances = new HashMap<>();
    }

    public boolean isOverflow(Currency currency, double amount) {
        return getBalances().get(currency) + amount > currency.getMaxBalance();
    }

    public boolean withdraw(Currency currency, double amount) {
        if (hasEnough(currency, amount)) {
            GemsPreTransactionEvent pre = new GemsPreTransactionEvent(currency, this, amount, TranactionType.WITHDRAW);
            SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(pre));
            if (pre.isCancelled()) return false;

            double finalAmount = getBalance(currency) - amount;
            double cappedAmount = Math.min(finalAmount, currency.getMaxBalance());

            this.modifyBalance(currency, cappedAmount, true);

            GemsPostTransactionEvent post = new GemsPostTransactionEvent(currency, this, amount, TranactionType.WITHDRAW);
            SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(post));

            GemsEconomy.inst().getEconomyLogger().log("[WITHDRAW] Account: " + getDisplayName() + " were withdrawn: " + currency.format(amount) + " and now has " + currency.format(cappedAmount));
            return true;
        }
        return false;
    }

    public boolean deposit(Currency currency, double amount) {
        if (canReceiveCurrency()) {
            GemsPreTransactionEvent pre = new GemsPreTransactionEvent(currency, this, amount, TranactionType.DEPOSIT);
            SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(pre));
            if (pre.isCancelled()) return false;

            double finalAmount = getBalance(currency) + amount;
            double cappedAmount = Math.min(finalAmount, currency.getMaxBalance());

            this.modifyBalance(currency, cappedAmount, true);

            GemsPostTransactionEvent post = new GemsPostTransactionEvent(currency, this, amount, TranactionType.DEPOSIT);
            SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(post));

            GemsEconomy.inst().getEconomyLogger().log("[DEPOSIT] Account: " + getDisplayName() + " were deposited: " + currency.format(amount) + " and now has " + currency.format(cappedAmount));
            return true;
        }
        return false;
    }

    public boolean convert(Currency exchanged, double exchangeAmount, Currency received, double amount) {
        GemsConversionEvent event = new GemsConversionEvent(exchanged, received, this, exchangeAmount, amount);
        SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(event));
        if (event.isCancelled()) return false;

        if (amount != -1) {
            double removed = getBalance(exchanged) - exchangeAmount;
            double added = getBalance(received) + amount;

            // cap balance
            double cappedRemoved = Math.min(removed, exchanged.getMaxBalance());
            double cappedAdded = Math.min(added, received.getMaxBalance());

            modifyBalance(exchanged, cappedRemoved, false);
            modifyBalance(received, cappedAdded, false);
            GemsEconomy.inst().getDataStore().saveAccount(this);
            GemsEconomy.inst().getEconomyLogger().log("[CONVERSION - Custom Amount] Account: " + getDisplayName() + " converted " + exchanged.format(exchangeAmount) + " to " + received.format(amount));
            return true;
        }
        double rate;
        boolean receiveRate = false;

        if (exchanged.getExchangeRate() > received.getExchangeRate()) {
            rate = exchanged.getExchangeRate();
        } else {
            rate = received.getExchangeRate();
            receiveRate = true;
        }

        double finalAmount = Math.round(exchangeAmount * rate);
        double removed, cappedRemoved;
        double added, cappedAdded;
        if (!receiveRate) {

            removed = getBalance(exchanged) - exchangeAmount;
            added = getBalance(received) + finalAmount;

            // cap the amount
            cappedRemoved = Math.min(removed, exchanged.getMaxBalance());
            cappedAdded = Math.min(added, received.getMaxBalance());

            if (GemsEconomy.inst().isDebug()) {
                UtilServer.consoleLog("Rate: " + rate);
                UtilServer.consoleLog("Finalized amount: " + finalAmount);
                UtilServer.consoleLog("Amount to remove: " + exchanged.format(cappedRemoved) + " (before capping: " + removed + ")");
                UtilServer.consoleLog("Amount to add: " + received.format(cappedAdded) + " (before capping: " + added + ")");
            }

            if (hasEnough(exchanged, exchangeAmount)) {
                this.modifyBalance(exchanged, cappedRemoved, false);
                this.modifyBalance(received, cappedAdded, false);
                GemsEconomy.inst().getDataStore().saveAccount(this);
                GemsEconomy.inst().getEconomyLogger().log("[CONVERSION - Preset Rate] Account: " + getDisplayName() + " converted " + exchanged.format(cappedRemoved) + " (Rate: " + rate + ") to " + received.format(cappedAdded));
                return true;
            }
        } else {
            removed = getBalance(exchanged) - finalAmount;
            added = getBalance(received) + exchangeAmount;

            // cap the amount
            cappedRemoved = Math.min(removed, exchanged.getMaxBalance());
            cappedAdded = Math.min(added, received.getMaxBalance());

            if (GemsEconomy.inst().isDebug()) {
                UtilServer.consoleLog("Rate: " + rate);
                UtilServer.consoleLog("Finalized amount: " + finalAmount);
                UtilServer.consoleLog("Amount to remove: " + exchanged.format(cappedRemoved) + " (before capping: " + removed + ")");
                UtilServer.consoleLog("Amount to add: " + received.format(cappedAdded) + " (before capping: " + added + ")");
            }

            if (hasEnough(exchanged, finalAmount)) {
                this.modifyBalance(exchanged, cappedRemoved, false);
                this.modifyBalance(received, cappedAdded, false);
                GemsEconomy.inst().getDataStore().saveAccount(this);
                GemsEconomy.inst().getEconomyLogger().log("[CONVERSION - Preset Rate] Account: " + getDisplayName() + " converted " + exchanged.format(cappedRemoved) + " (Rate: " + rate + ") to " + received.format(cappedAdded));
                return true;
            }

        }
        return false;
    }

    public void setBalance(Currency currency, double amount) {
        // cap the amount
        double cappedAmount = Math.min(amount, currency.getMaxBalance());

        GemsPreTransactionEvent pre = new GemsPreTransactionEvent(currency, this, amount, TranactionType.SET);
        SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(pre));
        if (pre.isCancelled()) return;

        getBalances().put(currency, cappedAmount);

        GemsPostTransactionEvent post = new GemsPostTransactionEvent(currency, this, cappedAmount, TranactionType.SET);
        SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(post));

        GemsEconomy.inst().getEconomyLogger().log("[BALANCE SET] Account: " + getDisplayName() + " were set to: " + currency.format(cappedAmount));
        GemsEconomy.inst().getDataStore().saveAccount(this);
    }

    /**
     * DO NOT USE UNLESS YOU HAVE VIEWED WHAT THIS DOES!
     * <p>
     * This directly modifies the account balance for a currency, with the
     * option of saving.
     *
     * @param currency - Currency to modify
     * @param amount   - Amount of cash to modify.
     * @param save     - Save the account or not. Should be done async!
     */
    public void modifyBalance(Currency currency, double amount, boolean save) {
        // we don't do cap amount in this method
        // cap should be done by other methods
        getBalances().put(currency, amount);

        if (save) GemsEconomy.inst().getDataStore().saveAccount(this);
    }

    public double getBalance(Currency currency) {
        if (getBalances().containsKey(currency)) {
            return getBalances().get(currency);
        }
        return currency.getDefaultBalance();
    }

    public double getBalance(String identifier) {
        for (Currency currency : getBalances().keySet()) {
            if (currency.getSingular().equalsIgnoreCase(identifier) || currency.getPlural().equalsIgnoreCase(identifier)) {
                return getBalances().get(currency);
            }
        }
        return 0; // Do not edit this
    }

    public String getDisplayName() {
        return getNickname() != null ? getNickname() : getUuid().toString();
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean hasEnough(double amount) {
        return hasEnough(GemsEconomy.inst().getCurrencyManager().getDefaultCurrency(), amount);
    }

    public boolean hasEnough(Currency currency, double amount) {
        return getBalance(currency) >= amount;
    }

    public boolean canReceiveCurrency() {
        return canReceiveCurrency;
    }

    public void setCanReceiveCurrency(boolean canReceiveCurrency) {
        this.canReceiveCurrency = canReceiveCurrency;
    }

    public Map<Currency, Double> getBalances() {
        return balances;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Account account = (Account) o;

        return uuid.equals(account.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}


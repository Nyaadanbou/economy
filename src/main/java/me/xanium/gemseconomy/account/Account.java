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
import me.xanium.gemseconomy.utils.TransactionType;
import me.xanium.gemseconomy.utils.UtilServer;
import org.bukkit.Bukkit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Account {

    private final UUID uuid;
    private String nickname;
    private final Map<Currency, Double> balances;
    private boolean canReceiveCurrency;

    public Account(UUID uuid, String nickname) {
        this.uuid = uuid;
        this.nickname = nickname;
        this.balances = new HashMap<>();
        this.canReceiveCurrency = true;
    }

    public boolean isOverflow(Currency currency, double amount) {
        return this.balances.get(currency) + amount > currency.getMaxBalance();
    }

    public boolean withdraw(Currency currency, double amount) {
        if (hasEnough(currency, amount)) {
            GemsPreTransactionEvent pre = new GemsPreTransactionEvent(currency, this, amount, TransactionType.WITHDRAW);
            SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(pre));
            if (pre.isCancelled()) return false;

            double finalAmount = getBalance(currency) - amount;
            double cappedAmount = Math.min(finalAmount, currency.getMaxBalance());

            this.modifyBalance(currency, cappedAmount, true);

            GemsPostTransactionEvent post = new GemsPostTransactionEvent(currency, this, amount, TransactionType.WITHDRAW);
            SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(post));

            GemsEconomy.getInstance().getEconomyLogger().log("[WITHDRAW] Account: " + getDisplayName() + " were withdrawn: " + currency.format(amount) + " and now has " + currency.format(cappedAmount));
            return true;
        }
        return false;
    }

    public boolean deposit(Currency currency, double amount) {
        if (this.canReceiveCurrency) {
            GemsPreTransactionEvent pre = new GemsPreTransactionEvent(currency, this, amount, TransactionType.DEPOSIT);
            SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(pre));
            if (pre.isCancelled()) return false;

            double finalAmount = getBalance(currency) + amount;
            double cappedAmount = Math.min(finalAmount, currency.getMaxBalance());

            this.modifyBalance(currency, cappedAmount, true);

            GemsPostTransactionEvent post = new GemsPostTransactionEvent(currency, this, amount, TransactionType.DEPOSIT);
            SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(post));

            GemsEconomy.getInstance().getEconomyLogger().log("[DEPOSIT] Account: " + getDisplayName() + " were deposited: " + currency.format(amount) + " and now has " + currency.format(cappedAmount));
            return true;
        }
        return false;
    }

    public boolean convert(Currency exchanged, double exchangeAmount, Currency received, double receiveAmount) {
        GemsConversionEvent event = new GemsConversionEvent(exchanged, received, this, exchangeAmount, receiveAmount);
        SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(event));
        if (event.isCancelled()) return false;

        if (receiveAmount != -1) {
            double removed = getBalance(exchanged) - exchangeAmount;
            double added = getBalance(received) + receiveAmount;

            // cap balance
            double cappedRemoved = Math.min(removed, exchanged.getMaxBalance());
            double cappedAdded = Math.min(added, received.getMaxBalance());

            modifyBalance(exchanged, cappedRemoved, false);
            modifyBalance(received, cappedAdded, false);
            GemsEconomy.getInstance().getDataStore().saveAccount(this);
            GemsEconomy.getInstance().getEconomyLogger().log("[CONVERSION - Custom Amount] Account: " + getDisplayName() + " converted " + exchanged.format(exchangeAmount) + " to " + received.format(receiveAmount));
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

            if (GemsEconomy.getInstance().isDebug()) {
                UtilServer.consoleLog("Rate: " + rate);
                UtilServer.consoleLog("Finalized amount: " + finalAmount);
                UtilServer.consoleLog("Amount to remove: " + exchanged.format(cappedRemoved) + " (before capping: " + removed + ")");
                UtilServer.consoleLog("Amount to add: " + received.format(cappedAdded) + " (before capping: " + added + ")");
            }

            if (hasEnough(exchanged, exchangeAmount)) {
                this.modifyBalance(exchanged, cappedRemoved, false);
                this.modifyBalance(received, cappedAdded, false);
                GemsEconomy.getInstance().getDataStore().saveAccount(this);
                GemsEconomy.getInstance().getEconomyLogger().log("[CONVERSION - Preset Rate] Account: " + getDisplayName() + " converted " + exchanged.format(cappedRemoved) + " (Rate: " + rate + ") to " + received.format(cappedAdded));
                return true;
            }
        } else {
            removed = getBalance(exchanged) - finalAmount;
            added = getBalance(received) + exchangeAmount;

            // cap the amount
            cappedRemoved = Math.min(removed, exchanged.getMaxBalance());
            cappedAdded = Math.min(added, received.getMaxBalance());

            if (GemsEconomy.getInstance().isDebug()) {
                UtilServer.consoleLog("Rate: " + rate);
                UtilServer.consoleLog("Finalized amount: " + finalAmount);
                UtilServer.consoleLog("Amount to remove: " + exchanged.format(cappedRemoved) + " (before capping: " + removed + ")");
                UtilServer.consoleLog("Amount to add: " + received.format(cappedAdded) + " (before capping: " + added + ")");
            }

            if (hasEnough(exchanged, finalAmount)) {
                this.modifyBalance(exchanged, cappedRemoved, false);
                this.modifyBalance(received, cappedAdded, false);
                GemsEconomy.getInstance().getDataStore().saveAccount(this);
                GemsEconomy.getInstance().getEconomyLogger().log("[CONVERSION - Preset Rate] Account: " + getDisplayName() + " converted " + exchanged.format(cappedRemoved) + " (Rate: " + rate + ") to " + received.format(cappedAdded));
                return true;
            }

        }
        return false;
    }

    public void setBalance(Currency currency, double amount) {
        double cappedAmount = Math.min(amount, currency.getMaxBalance());

        GemsPreTransactionEvent pre = new GemsPreTransactionEvent(currency, this, amount, TransactionType.SET);
        SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(pre));
        if (pre.isCancelled()) return;

        this.modifyBalance(currency, cappedAmount, false);

        GemsPostTransactionEvent post = new GemsPostTransactionEvent(currency, this, cappedAmount, TransactionType.SET);
        SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(post));

        GemsEconomy.getInstance().getEconomyLogger().log("[BALANCE SET] Account: " + getDisplayName() + " were set to: " + currency.format(cappedAmount));
        GemsEconomy.getInstance().getDataStore().saveAccount(this);
    }

    /**
     * Directly modifies the account balance for a currency, with the option of saving.
     *
     * @param currency - the Currency to modify with
     * @param amount   - the amount of cash to set to
     * @param save     - true to save the Account; false to not (should be done async)
     */
    public void modifyBalance(Currency currency, double amount, boolean save) {
        // We don't cap amount in this method - it's others job
        this.balances.put(currency, amount);
        if (save)
            GemsEconomy.getInstance().getDataStore().saveAccount(this);
    }

    public double getBalance(Currency currency) {
        return this.balances.computeIfAbsent(currency, Currency::getDefaultBalance);
    }

    public double getBalance(String identifier) {
        for (Currency currency : this.balances.keySet()) {
            if (currency.getSingular().equalsIgnoreCase(identifier) || currency.getPlural().equalsIgnoreCase(identifier))
                return this.balances.get(currency);
        }
        return 0; // Do not edit this
    }

    public String getDisplayName() {
        return this.nickname != null ? this.nickname : this.uuid.toString();
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
        return hasEnough(GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency(), amount);
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


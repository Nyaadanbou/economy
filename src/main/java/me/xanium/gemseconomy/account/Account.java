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
import me.xanium.gemseconomy.event.GemsPostTransactionEvent;
import me.xanium.gemseconomy.event.GemsPreTransactionEvent;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import me.xanium.gemseconomy.utils.TransactionType;
import org.bukkit.Bukkit;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Account {

    private @NonNull final UUID uuid;
    private @MonotonicNonNull String nickname;
    private @NonNull final Map<Currency, Double> balances = new HashMap<>();
    private boolean canReceiveCurrency = true;

    public Account(@NonNull UUID uuid, @Nullable String nickname) {
        this.uuid = uuid;
        this.nickname = nickname;
    }

    public synchronized boolean withdraw(@NonNull Currency currency, double amount) {
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

    public synchronized boolean deposit(@NonNull Currency currency, double amount) {
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

    public synchronized void setBalance(@NonNull Currency currency, double amount) {
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
    public synchronized void modifyBalance(@NonNull Currency currency, double amount, boolean save) {
        // We don't cap amount in this method - it's others job
        this.balances.put(currency, amount);
        if (save)
            GemsEconomy.getInstance().getDataStore().saveAccount(this);
    }

    public double getBalance(@NonNull Currency currency) {
        return this.balances.computeIfAbsent(currency, Currency::getDefaultBalance);
    }

    public double getBalance(@NonNull String identifier) {
        for (Currency currency : this.balances.keySet()) {
            if (currency.getSingular().equalsIgnoreCase(identifier) || currency.getPlural().equalsIgnoreCase(identifier))
                return this.balances.get(currency);
        }
        return 0; // Do not edit this
    }

    public @NonNull Map<Currency, Double> getBalances() {
        return balances;
    }

    public @NonNull String getDisplayName() {
        return this.nickname != null ? this.nickname : this.uuid.toString();
    }

    public @MonotonicNonNull String getNickname() {
        return nickname;
    }

    public @NonNull UUID getUuid() {
        return uuid;
    }

    public boolean isOverflow(@NonNull Currency currency, double amount) {
        return this.balances.get(currency) + amount > currency.getMaxBalance();
    }

    public boolean hasEnough(double amount) {
        return hasEnough(GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency(), amount);
    }

    public boolean hasEnough(@NonNull Currency currency, double amount) {
        return getBalance(currency) >= amount;
    }

    public boolean canReceiveCurrency() {
        return canReceiveCurrency;
    }

    public void setCanReceiveCurrency(boolean canReceiveCurrency) {
        this.canReceiveCurrency = canReceiveCurrency;
    }

    public void setNickname(@NonNull String nickname) {
        this.nickname = nickname;
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


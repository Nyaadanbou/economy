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
import me.xanium.gemseconomy.message.Action;
import me.xanium.gemseconomy.utils.TransactionType;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class Account {

    private final @NonNull UUID uuid;
    private @MonotonicNonNull String nickname;
    private final @NonNull Map<Currency, Double> balances = new ConcurrentHashMap<>(8);
    private final @NonNull Map<Currency, Double> cumulativeBalances = new ConcurrentHashMap<>(4);
    private boolean canReceiveCurrency = true;

    private final ReadWriteLock lock = new ReentrantReadWriteLock(); // Ensure thread safety

    public Account(@NonNull UUID uuid, @Nullable String nickname) {
        this.uuid = uuid;
        this.nickname = nickname;
    }

    public boolean withdraw(@NonNull Currency currency, double amount) {
        if (!hasEnough(currency, amount))
            return false;

        GemsPreTransactionEvent preEvent = new GemsPreTransactionEvent(currency, this, amount, TransactionType.WITHDRAW);
        if (!preEvent.callEvent())
            return false;

        lock.writeLock().lock();
        try {
            double finalAmount = getBalance(currency) - amount;
            double cappedAmount = Math.min(finalAmount, currency.getMaxBalance());
            balances.put(currency, cappedAmount); // Update balance
            GemsEconomy.getInstance().getDataStore().saveAccount(this); // Save it to database
            GemsEconomy.getInstance().getUpdateForwarder().sendMessage(Action.UPDATE_ACCOUNT, getUuid()); // Sync between servers
            GemsEconomy.getInstance().getEconomyLogger().log("[WITHDRAW] Account: " + getDisplayName() + " were withdrawn: " + currency.format(amount) + " and now has " + currency.format(cappedAmount));
        } finally {
            lock.writeLock().unlock();
        }

        GemsPostTransactionEvent postEvent = new GemsPostTransactionEvent(currency, this, amount, TransactionType.WITHDRAW);
        postEvent.callEvent();

        return true;
    }

    public boolean deposit(@NonNull Currency currency, double amount) {
        if (!canReceiveCurrency)
            return false;

        GemsPreTransactionEvent preEvent = new GemsPreTransactionEvent(currency, this, amount, TransactionType.DEPOSIT);
        if (!preEvent.callEvent())
            return false;

        lock.writeLock().lock();
        try {
            double finalAmount = getBalance(currency) + amount;
            double cappedAmount = Math.min(finalAmount, currency.getMaxBalance());
            balances.put(currency, cappedAmount); // Update balance
            cumulativeBalances.merge(currency, amount, Double::sum); // Accumulate deposited amount
            GemsEconomy.getInstance().getDataStore().saveAccount(this); // Save it to database
            GemsEconomy.getInstance().getUpdateForwarder().sendMessage(Action.UPDATE_ACCOUNT, getUuid()); // Sync between servers
            GemsEconomy.getInstance().getEconomyLogger().log("[DEPOSIT] Account: " + getDisplayName() + " were deposited: " + currency.format(amount) + " and now has " + currency.format(cappedAmount));
        } finally {
            lock.writeLock().unlock();
        }

        GemsPostTransactionEvent postEvent = new GemsPostTransactionEvent(currency, this, amount, TransactionType.DEPOSIT);
        postEvent.callEvent();

        return true;
    }

    public void setBalance(@NonNull Currency currency, double amount) {
        GemsPreTransactionEvent preEvent = new GemsPreTransactionEvent(currency, this, amount, TransactionType.SET);
        if (!preEvent.callEvent())
            return;

        lock.writeLock().lock();
        try {
            double cappedAmount = Math.min(amount, currency.getMaxBalance());
            balances.put(currency, cappedAmount); // Update balance
            GemsEconomy.getInstance().getDataStore().saveAccount(this); // Save it to database
            GemsEconomy.getInstance().getUpdateForwarder().sendMessage(Action.UPDATE_ACCOUNT, getUuid()); // Sync between servers
            GemsEconomy.getInstance().getEconomyLogger().log("[BALANCE SET] Account: " + getDisplayName() + " were set to: " + currency.format(cappedAmount));
        } finally {
            lock.writeLock().unlock();
        }

        GemsPostTransactionEvent postEvent = new GemsPostTransactionEvent(currency, this, amount, TransactionType.SET);
        postEvent.callEvent();
    }

    public double getBalance(@NonNull Currency currency) {
        lock.readLock().lock();
        try {
            return balances.computeIfAbsent(currency, Currency::getDefaultBalance);
        } finally {
            lock.readLock().unlock();
        }
    }

    public double getBalance(@NonNull String identifier) {
        lock.readLock().lock();
        try {
            return balances.keySet().stream().filter(currency ->
                currency.getSingular().equalsIgnoreCase(identifier) ||
                currency.getPlural().equalsIgnoreCase(identifier)
            ).findAny().map(balances::get).orElse(0D); // Do not edit this
        } finally {
            lock.readLock().unlock();
        }
    }

    public @NonNull Map<Currency, Double> getBalances() {
        return balances;
    }

    public double getCumulativeBalance(@NonNull Currency currency) {
        return cumulativeBalances.computeIfAbsent(currency, ignored -> 0D);
    }

    public double getCumulativeBalance(@NonNull String identifier) {
        return cumulativeBalances.keySet().stream().filter(currency ->
            currency.getSingular().equalsIgnoreCase(identifier) ||
            currency.getPlural().equalsIgnoreCase(identifier)
        ).findAny().map(cumulativeBalances::get).orElse(0D);
    }

    public @NonNull Map<Currency, Double> getCumulativeBalances() {
        return cumulativeBalances;
    }

    public @NonNull String getDisplayName() {
        return nickname != null ? nickname : uuid.toString();
    }

    public @MonotonicNonNull String getNickname() {
        return nickname;
    }

    public @NonNull UUID getUuid() {
        return uuid;
    }

    public boolean testOverflow(@NonNull Currency currency, double amount) {
        return getBalance(currency) + amount > currency.getMaxBalance();
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


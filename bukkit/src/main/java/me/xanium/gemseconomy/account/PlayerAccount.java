package me.xanium.gemseconomy.account;

import com.google.common.base.Preconditions;
import me.xanium.gemseconomy.GemsEconomyPlugin;
import me.xanium.gemseconomy.api.Account;
import me.xanium.gemseconomy.api.Currency;
import me.xanium.gemseconomy.event.GemsPostTransactionEvent;
import me.xanium.gemseconomy.event.GemsPreTransactionEvent;
import me.xanium.gemseconomy.message.Action;
import me.xanium.gemseconomy.utils.TransactionType;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PlayerAccount implements Account {

    private final @NonNull UUID uuid;
    private final @NonNull Map<Currency, Double> balances;
    private final @NonNull Map<Currency, Double> cumulativeBalances;
    private @Nullable String nickname;
    private boolean canReceiveCurrency = true;

    private final Map<Currency, ReadWriteLock> lockHolders;

    public PlayerAccount(@NonNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid");
        this.uuid = uuid;
        this.balances = new HashMap<>(8, 1f);
        this.cumulativeBalances = new HashMap<>(8, 1f);
        this.lockHolders = new ConcurrentHashMap<>(8, 1f); // Ensure thread safety
    }

    public PlayerAccount(@NonNull UUID uuid, @Nullable String nickname) {
        this(uuid);
        this.nickname = nickname;
    }

    @Override
    public boolean withdraw(@NonNull Currency currency, double amount) {
        Preconditions.checkNotNull(currency, "currency");
        GemsPreTransactionEvent preEvent = new GemsPreTransactionEvent(currency, this, amount, TransactionType.WITHDRAW);
        if (!preEvent.callEvent())
            return false;

        if (!hasEnough(currency, amount))
            return false;

        ReadWriteLock lock = lockHolders.computeIfAbsent(currency, k -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        try {
            double finalAmount = getBalance(currency) - amount;
            double cappedAmount = Math.min(finalAmount, currency.getMaximumBalance());
            balances.put(currency, cappedAmount); // Update balance
            GemsEconomyPlugin.getInstance().getDataStore().saveAccount(this); // Save it to database
            GemsEconomyPlugin.getInstance().getMessenger().sendMessage(Action.UPDATE_ACCOUNT, getUuid()); // Sync between servers
            GemsEconomyPlugin.getInstance().getEconomyLogger().log("[WITHDRAW] Account: " + getDisplayName() + " were withdrawn: " + currency.simpleFormat(amount) + " and now has " + currency.simpleFormat(cappedAmount));
        } finally {
            lock.writeLock().unlock();
        }

        GemsPostTransactionEvent postEvent = new GemsPostTransactionEvent(currency, this, amount, TransactionType.WITHDRAW);
        postEvent.callEvent();

        return true;
    }

    @Override
    public boolean deposit(@NonNull Currency currency, double amount) {
        Preconditions.checkNotNull(currency, "currency");
        if (!canReceiveCurrency)
            return false;

        GemsPreTransactionEvent preEvent = new GemsPreTransactionEvent(currency, this, amount, TransactionType.DEPOSIT);
        if (!preEvent.callEvent())
            return false;

        ReadWriteLock lock = lockHolders.computeIfAbsent(currency, k -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        try {
            double finalAmount = getBalance(currency) + amount;
            double cappedAmount = Math.min(finalAmount, currency.getMaximumBalance());
            balances.put(currency, cappedAmount); // Update balance
            cumulativeBalances.merge(currency, amount, Double::sum); // Accumulate deposited amount
            GemsEconomyPlugin.getInstance().getDataStore().saveAccount(this); // Save it to database
            GemsEconomyPlugin.getInstance().getMessenger().sendMessage(Action.UPDATE_ACCOUNT, getUuid()); // Sync between servers
            GemsEconomyPlugin.getInstance().getEconomyLogger().log("[DEPOSIT] Account: " + getDisplayName() + " were deposited: " + currency.simpleFormat(amount) + " and now has " + currency.simpleFormat(cappedAmount));
        } finally {
            lock.writeLock().unlock();
        }

        GemsPostTransactionEvent postEvent = new GemsPostTransactionEvent(currency, this, amount, TransactionType.DEPOSIT);
        postEvent.callEvent();

        return true;
    }

    @Override
    public void setBalance(@NonNull Currency currency, double amount) {
        Preconditions.checkNotNull(currency, "currency");
        GemsPreTransactionEvent preEvent = new GemsPreTransactionEvent(currency, this, amount, TransactionType.SET);
        if (!preEvent.callEvent())
            return;

        ReadWriteLock lock = lockHolders.computeIfAbsent(currency, k -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        try {
            double cappedAmount = Math.min(amount, currency.getMaximumBalance());
            balances.put(currency, cappedAmount); // Update balance
            GemsEconomyPlugin.getInstance().getDataStore().saveAccount(this); // Save it to database
            GemsEconomyPlugin.getInstance().getMessenger().sendMessage(Action.UPDATE_ACCOUNT, getUuid()); // Sync between servers
            GemsEconomyPlugin.getInstance().getEconomyLogger().log("[BALANCE SET] Account: " + getDisplayName() + " were set to: " + currency.simpleFormat(cappedAmount));
        } finally {
            lock.writeLock().unlock();
        }

        GemsPostTransactionEvent postEvent = new GemsPostTransactionEvent(currency, this, amount, TransactionType.SET);
        postEvent.callEvent();
    }

    @Override
    public double getBalance(@NonNull Currency currency) {
        Preconditions.checkNotNull(currency, "currency");
        ReadWriteLock lock = lockHolders.computeIfAbsent(currency, k -> new ReentrantReadWriteLock());
        lock.readLock().lock();
        try {
            return balances.computeIfAbsent(currency, Currency::getDefaultBalance);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public double getBalance(@NonNull String identifier) {
        Preconditions.checkNotNull(identifier, "identifier");
        return balances
            .keySet()
            .stream()
            .filter(currency -> currency.getName().equalsIgnoreCase(identifier))
            .findAny()
            .map(this::getBalance)
            .orElse(0D);
    }

    @Override
    public @NonNull Map<Currency, Double> getBalances() {
        return balances;
    }

    @Override
    public double getCumulativeBalance(@NonNull Currency currency) {
        Preconditions.checkNotNull(currency, "currency");
        ReadWriteLock lock = lockHolders.computeIfAbsent(currency, k -> new ReentrantReadWriteLock());
        lock.readLock().lock();
        try {
            return cumulativeBalances.computeIfAbsent(currency, ignored -> 0D);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public double getCumulativeBalance(@NonNull String identifier) {
        Preconditions.checkNotNull(identifier, "identifier");
        return cumulativeBalances
            .keySet()
            .stream()
            .filter(currency -> currency.getName().equalsIgnoreCase(identifier))
            .findAny()
            .map(cumulativeBalances::get)
            .orElse(0D);
    }

    @Override
    public @NonNull Map<Currency, Double> getCumulativeBalances() {
        return cumulativeBalances;
    }

    @Override
    public @NonNull String getDisplayName() {
        return nickname != null ? nickname : uuid.toString();
    }

    @Override
    public @NonNull String getNickname() {
        return nickname != null ? nickname : "null";
    }

    @Override
    public @NonNull UUID getUuid() {
        return uuid;
    }

    @Override
    public boolean testOverflow(@NonNull Currency currency, double amount) {
        Preconditions.checkNotNull(currency, "currency");
        return getBalance(currency) + amount > currency.getMaximumBalance();
    }

    @Override
    public boolean hasEnough(double amount) {
        return hasEnough(GemsEconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency(), amount);
    }

    @Override
    public boolean hasEnough(@NonNull Currency currency, double amount) {
        Preconditions.checkNotNull(currency, "currency");
        ReadWriteLock lock = lockHolders.computeIfAbsent(currency, k -> new ReentrantReadWriteLock());
        lock.readLock().lock();
        try {
            return getBalance(currency) >= amount;
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean canReceiveCurrency() {
        return canReceiveCurrency;
    }

    @Override
    public void setCanReceiveCurrency(boolean canReceiveCurrency) {
        this.canReceiveCurrency = canReceiveCurrency;
    }

    @Override
    public void setNickname(@Nullable String nickname) {
        this.nickname = nickname;
    }

    /*@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlayerAccount account = (PlayerAccount) o;
        return uuid.equals(account.uuid);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }*/
}

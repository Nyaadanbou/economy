package cc.mewcraft.economy.account;

import cc.mewcraft.economy.EconomyPlugin;
import cc.mewcraft.economy.api.Account;
import cc.mewcraft.economy.api.Currency;
import cc.mewcraft.economy.event.EconomyPostTransactionEvent;
import cc.mewcraft.economy.event.EconomyPreTransactionEvent;
import cc.mewcraft.economy.message.Action;
import cc.mewcraft.economy.utils.TransactionType;
import com.google.common.base.Preconditions;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import net.kyori.examination.string.StringExaminer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Stream;

public class PlayerAccount implements Account, Examinable {
    private final @NonNull UUID uuid;
    private final @NonNull Map<Currency, Double> balances;
    private final @NonNull Map<Currency, Double> heapBalances;
    private @Nullable String nickname;
    private boolean canReceiveCurrency = true;

    private final Map<Currency, ReadWriteLock> locks;

    public PlayerAccount(@NonNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid");
        this.uuid = uuid;
        this.balances = new HashMap<>(8, 1f);
        this.heapBalances = new HashMap<>(8, 1f);
        this.locks = new ConcurrentHashMap<>(8, 1f); // Ensure thread safety
    }

    public PlayerAccount(@NonNull UUID uuid, @Nullable String nickname) {
        this(uuid);
        this.nickname = nickname;
    }

    @Override
    public boolean withdraw(@NonNull Currency currency, double amount) {
        Preconditions.checkNotNull(currency, "currency");
        EconomyPreTransactionEvent preEvent = new EconomyPreTransactionEvent(currency, this, amount, TransactionType.WITHDRAW);
        if (!preEvent.callEvent())
            return false;

        if (!hasEnough(currency, amount))
            return false;

        ReadWriteLock lock = locks.computeIfAbsent(currency, k -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        try {
            double finalAmount = getBalance(currency) - amount;
            double cappedAmount = Math.min(finalAmount, currency.getMaximumBalance());
            balances.put(currency, cappedAmount); // Update balance
            EconomyPlugin.getInstance().getDataStore().saveAccount(this); // Save it to database
            EconomyPlugin.getInstance().getMessenger().sendMessage(Action.UPDATE_ACCOUNT, getUuid()); // Sync between servers
            EconomyPlugin.getInstance().getEconomyLogger().log("[WITHDRAW] Account: " + getDisplayName() + " were withdrawn: " + currency.simpleFormat(amount) + " and now has " + currency.simpleFormat(cappedAmount));
        } finally {
            lock.writeLock().unlock();
        }

        EconomyPostTransactionEvent postEvent = new EconomyPostTransactionEvent(currency, this, amount, TransactionType.WITHDRAW);
        postEvent.callEvent();

        return true;
    }

    @Override
    public boolean deposit(@NonNull Currency currency, double amount) {
        Preconditions.checkNotNull(currency, "currency");
        if (!canReceiveCurrency)
            return false;

        EconomyPreTransactionEvent preEvent = new EconomyPreTransactionEvent(currency, this, amount, TransactionType.DEPOSIT);
        if (!preEvent.callEvent())
            return false;

        ReadWriteLock lock = locks.computeIfAbsent(currency, k -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        try {
            double finalAmount = getBalance(currency) + amount;
            double cappedAmount = Math.min(finalAmount, currency.getMaximumBalance());
            balances.put(currency, cappedAmount); // Update balance
            heapBalances.merge(currency, amount, Double::sum); // Accumulate deposited amount
            EconomyPlugin.getInstance().getDataStore().saveAccount(this); // Save it to database
            EconomyPlugin.getInstance().getMessenger().sendMessage(Action.UPDATE_ACCOUNT, getUuid()); // Sync between servers
            EconomyPlugin.getInstance().getEconomyLogger().log("[DEPOSIT] Account: " + getDisplayName() + " were deposited: " + currency.simpleFormat(amount) + " and now has " + currency.simpleFormat(cappedAmount));
        } finally {
            lock.writeLock().unlock();
        }

        EconomyPostTransactionEvent postEvent = new EconomyPostTransactionEvent(currency, this, amount, TransactionType.DEPOSIT);
        postEvent.callEvent();

        return true;
    }

    @Override
    public void setBalance(@NonNull Currency currency, double amount) {
        Preconditions.checkNotNull(currency, "currency");
        EconomyPreTransactionEvent preEvent = new EconomyPreTransactionEvent(currency, this, amount, TransactionType.SET);
        if (!preEvent.callEvent())
            return;

        ReadWriteLock lock = locks.computeIfAbsent(currency, k -> new ReentrantReadWriteLock());
        lock.writeLock().lock();
        try {
            double cappedAmount = Math.min(amount, currency.getMaximumBalance());
            balances.put(currency, cappedAmount); // Update balance
            EconomyPlugin.getInstance().getDataStore().saveAccount(this); // Save it to database
            EconomyPlugin.getInstance().getMessenger().sendMessage(Action.UPDATE_ACCOUNT, getUuid()); // Sync between servers
            EconomyPlugin.getInstance().getEconomyLogger().log("[BALANCE SET] Account: " + getDisplayName() + " were set to: " + currency.simpleFormat(cappedAmount));
        } finally {
            lock.writeLock().unlock();
        }

        EconomyPostTransactionEvent postEvent = new EconomyPostTransactionEvent(currency, this, amount, TransactionType.SET);
        postEvent.callEvent();
    }

    @Override
    public double getBalance(@NonNull Currency currency) {
        Preconditions.checkNotNull(currency, "currency");
        ReadWriteLock lock = locks.computeIfAbsent(currency, k -> new ReentrantReadWriteLock());
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
    public double getHeapBalance(@NonNull Currency currency) {
        Preconditions.checkNotNull(currency, "currency");
        ReadWriteLock lock = locks.computeIfAbsent(currency, k -> new ReentrantReadWriteLock());
        lock.readLock().lock();
        try {
            return heapBalances.computeIfAbsent(currency, ignored -> 0D);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public double getHeapBalance(@NonNull String identifier) {
        Preconditions.checkNotNull(identifier, "identifier");
        return heapBalances
                .keySet()
                .stream()
                .filter(currency -> currency.getName().equalsIgnoreCase(identifier))
                .findAny()
                .map(heapBalances::get)
                .orElse(0D);
    }

    @Override
    public @NonNull Map<Currency, Double> getHeapBalances() {
        return heapBalances;
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
        return hasEnough(EconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency(), amount);
    }

    @Override
    public boolean hasEnough(@NonNull Currency currency, double amount) {
        Preconditions.checkNotNull(currency, "currency");
        ReadWriteLock lock = locks.computeIfAbsent(currency, k -> new ReentrantReadWriteLock());
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

    @Override
    public @NonNull Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(
                ExaminableProperty.of("uuid", uuid),
                ExaminableProperty.of("nickname", nickname),
                ExaminableProperty.of("balances", balances),
                ExaminableProperty.of("heapBalances", heapBalances),
                ExaminableProperty.of("canReceiveCurrency", canReceiveCurrency)
        );
    }

    @Override
    public String toString() {
        return StringExaminer.simpleEscaping().examine(this);
    }
}

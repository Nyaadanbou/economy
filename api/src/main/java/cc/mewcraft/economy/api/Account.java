package cc.mewcraft.economy.api;

import java.util.Map;
import java.util.UUID;

import org.jetbrains.annotations.ApiStatus;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public interface Account {
    @NonNull UUID getUuid();

    @NonNull String getDisplayName();

    @NonNull String getNickname();

    boolean withdraw(@NonNull Currency currency, double amount);

    boolean deposit(@NonNull Currency currency, double amount);

    void setBalance(@NonNull Currency currency, double amount);

    double getBalance(@NonNull Currency currency);

    double getBalance(@NonNull String identifier);

    @ApiStatus.Internal
    @NonNull Map<Currency, Double> getBalances();

    double getHeapBalance(@NonNull Currency currency);

    double getHeapBalance(@NonNull String identifier);

    @ApiStatus.Internal
    @NonNull Map<Currency, Double> getHeapBalances();

    boolean testOverflow(@NonNull Currency currency, double amount);

    boolean hasEnough(double amount);

    boolean hasEnough(@NonNull Currency currency, double amount);

    boolean canReceiveCurrency();

    void setCanReceiveCurrency(boolean canReceiveCurrency);

    void setNickname(@Nullable String nickname);

    /*@Override boolean equals(Object o);

    @Override int hashCode();*/
}

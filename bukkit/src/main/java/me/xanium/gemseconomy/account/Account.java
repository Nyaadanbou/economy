package me.xanium.gemseconomy.account;

import me.xanium.gemseconomy.currency.Currency;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.ApiStatus;

import java.util.Map;
import java.util.UUID;

public interface Account {

    boolean withdraw(@NonNull Currency currency, double amount);

    boolean deposit(@NonNull Currency currency, double amount);

    void setBalance(@NonNull Currency currency, double amount);

    double getBalance(@NonNull Currency currency);

    double getBalance(@NonNull String identifier);

    @ApiStatus.Internal
    @NonNull Map<Currency, Double> getBalances();

    double getCumulativeBalance(@NonNull Currency currency);

    double getCumulativeBalance(@NonNull String identifier);

    @ApiStatus.Internal
    @NonNull Map<Currency, Double> getCumulativeBalances();

    @NonNull String getDisplayName();

    @Nullable String getNickname();

    @NonNull UUID getUuid();

    @ApiStatus.Internal
    boolean testOverflow(@NonNull Currency currency, double amount);

    boolean hasEnough(double amount);

    boolean hasEnough(@NonNull Currency currency, double amount);

    @ApiStatus.Internal
    boolean canReceiveCurrency();

    @ApiStatus.Internal
    void setCanReceiveCurrency(boolean canReceiveCurrency);

    void setNickname(@Nullable String nickname);

    /*@Override boolean equals(Object o);

    @Override int hashCode();*/

}

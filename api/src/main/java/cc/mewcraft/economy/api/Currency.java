package cc.mewcraft.economy.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.UUID;

public interface Currency {
    @NonNull UUID getUuid();

    @NonNull String getName();

    void setName(@NonNull String name);

    double getDefaultBalance();

    void setDefaultBalance(double defaultBalance);

    double getMaximumBalance();

    void setMaximumBalance(double maxBalance);

    /**
     * Gets a plain string describing the balance amount.
     * <p>
     * This string can be used for logging or anywhere that does not support {@link Component}.
     *
     * @param amount the balance amount
     * @return a plain string describing the balance amount
     */
    @NonNull String simpleFormat(double amount);

    /**
     * Gets a MiniMessage string describing the balance amount.
     * <p>
     * Since this is a MiniMessage string, it is meant to be used where {@link Component} is supported.
     * The caller of this method is responsible to deserialize the MiniMessage string on their own.
     *
     * @param amount the balance amount
     * @return a MiniMessage string describing the balance amount
     */
    @NonNull String fancyFormat(double amount);

    @NonNull String getDisplayName();

    @NonNull TextColor getColor();

    void setColor(@Nullable TextColor color);

    @Nullable String getSymbolNullable();

    @NonNull String getSymbolOrEmpty();

    void setSymbol(@Nullable String symbol);

    boolean isDefaultCurrency();

    void setDefaultCurrency(boolean defaultCurrency);

    boolean isDecimalSupported();

    void setDecimalSupported(boolean decimal);

    boolean isPayable();

    void setPayable(boolean payable);

    double getExchangeRate();

    void setExchangeRate(double exchangeRate);

    /**
     * Copy all states of specific object to this object.
     *
     * @param other the currency from which the states are copied
     */
    void update(@NonNull Currency other);
}

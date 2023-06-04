/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.currency;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.utils.UtilString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.UUID;

@SuppressWarnings("unused")
@DefaultQualifier(NonNull.class)
public class Currency {

    private final UUID uuid;
    private @Nullable String name;
    private @Nullable String symbol;
    private TextColor color = NamedTextColor.WHITE;
    private boolean decimalSupported = true;
    private boolean payable = true;
    private boolean defaultCurrency = false;
    private double defaultBalance = 0D;
    private double maxBalance = 0D; // zero means unlimited
    private double exchangeRate = 0D;

    public Currency(final UUID uuid) {
        this.uuid = uuid;
    }

    public Currency(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    /**
     * Copy all states of specific Currency to this Currency.
     *
     * @param other the Currency which the states are copied from
     */
    public void update(Currency other) {
        this.name = other.name;
        this.symbol = other.symbol;
        this.color = other.color;
        this.decimalSupported = other.decimalSupported;
        this.payable = other.payable;
        this.defaultCurrency = other.defaultCurrency;
        this.defaultBalance = other.defaultBalance;
        this.maxBalance = other.maxBalance;
        this.exchangeRate = other.exchangeRate;
    }

    /* ---------------- Balance Amount ---------------- */

    /**
     * @deprecated use {@link #getName()} instead
     */
    @Deprecated
    public @Nullable String getSingular() {
        return getName();
    }

    public @Nullable String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getDefaultBalance() {
        return this.defaultBalance;
    }

    public void setDefaultBalance(double defaultBalance) {
        this.defaultBalance = defaultBalance;
    }

    public double getMaxBalance() {
        return this.maxBalance == 0D ? Integer.MAX_VALUE : this.maxBalance;
    }

    public void setMaximumBalance(double maxBalance) {
        this.maxBalance = maxBalance;
    }

    /* ---------------- Balance Display ---------------- */

    /**
     * @deprecated in favor of {@link #simpleFormat(double)} or {@link #fancyFormat(double)}
     */
    @Deprecated
    public String format(double amount) {
        return simpleFormat(amount);
    }

    /**
     * Gets a plain string describing the balance amount.
     * <p>
     * This string is used for logging or anywhere that don't support {@link Component}.
     *
     * @param amount the balance amount
     *
     * @return a plain string describing the balance amount
     */
    public String simpleFormat(double amount) {
        String amountString = UtilString.format(amount, this.decimalSupported);
        String nameString = getName().replace("_", " ");
        return GemsEconomy.lang().raw("msg_balance_simple_format",
            "amount", amountString,
            "name", nameString
        );
    }

    /**
     * Gets a MiniMessage string describing the balance amount.
     * <p>
     * Since this is a MiniMessage string, it is meant to be used for display that support {@link Component}. The caller
     * of this method should deserialize the MiniMessage string on their own.
     *
     * @param amount the balance amount
     *
     * @return a MiniMessage string describing the balance amount
     */
    public String fancyFormat(double amount) {
        String amountString = UtilString.format(amount, this.decimalSupported);
        String symbolString = getSymbolOrEmpty();
        String nameString = getName().replace("_", " ");
        return GemsEconomy.lang().raw("msg_balance_fancy_format",
            "amount", amountString,
            "name", nameString,
            "symbol", symbolString
        );
    }

    public @Nullable String getDisplayName() {
        return this.name;
    }

    public TextColor getColor() {
        return this.color;
    }

    public void setColor(TextColor color) {
        this.color = color;
    }

    @Deprecated
    public @Nullable String getSymbol() {
        return this.symbol;
    }

    public @Nullable String getSymbolNullable() {
        return this.symbol;
    }

    public String getSymbolOrEmpty() {
        return this.symbol != null ? this.symbol : "";
    }

    public void setSymbol(@Nullable String symbol) {
        this.symbol = symbol;
    }

    /* ---------------- Other ---------------- */

    public UUID getUuid() {
        return this.uuid;
    }

    public boolean isDefaultCurrency() {
        return this.defaultCurrency;
    }

    public void setDefaultCurrency(boolean defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public boolean isDecimalSupported() {
        return this.decimalSupported;
    }

    public void setDecimalSupported(boolean decimal) {
        this.decimalSupported = decimal;
    }

    public boolean isPayable() {
        return this.payable;
    }

    public void setPayable(boolean payable) {
        this.payable = payable;
    }

    public double getExchangeRate() {
        return this.exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @Override public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Currency currency = (Currency) o;
        return this.uuid.equals(currency.uuid);
    }

    @Override public int hashCode() {
        return this.uuid.hashCode();
    }

}


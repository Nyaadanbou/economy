/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.currency;

import me.xanium.gemseconomy.utils.UtilString;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.text.NumberFormat;
import java.util.UUID;

@SuppressWarnings("unused")
@DefaultQualifier(NonNull.class)
public class Currency {

    private final UUID uuid;
    @MonotonicNonNull private String singular;
    @MonotonicNonNull private String plural;
    @Nullable private String symbol;
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

    public Currency(UUID uuid, String singular, String plural) {
        this.uuid = uuid;
        this.singular = singular;
        this.plural = plural;
    }

    /**
     * Copy all states of specific Currency to this Currency.
     *
     * @param other the Currency which the states are copied from
     */
    public void update(Currency other) {
        this.singular = other.singular;
        this.plural = other.plural;
        this.symbol = other.symbol;
        this.color = other.color;
        this.decimalSupported = other.decimalSupported;
        this.payable = other.payable;
        this.defaultCurrency = other.defaultCurrency;
        this.defaultBalance = other.defaultBalance;
        this.maxBalance = other.maxBalance;
        this.exchangeRate = other.exchangeRate;
    }

    public void setSingular(String singular) {
        this.singular = singular;
    }

    public void setPlural(String plural) {
        this.plural = plural;
    }

    public void setDefaultBalance(double defaultBalance) {
        this.defaultBalance = defaultBalance;
    }

    public void setMaximumBalance(double maxBalance) {
        this.maxBalance = maxBalance;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getSingular() {
        return this.singular;
    }

    public String getPlural() {
        return this.plural;
    }

    public double getDefaultBalance() {
        return this.defaultBalance;
    }

    public double getMaxBalance() {
        return this.maxBalance == 0D ? Integer.MAX_VALUE : this.maxBalance;
    }

    public String format(double amount) {
        StringBuilder amt = new StringBuilder();
        if (this.getSymbol() != null) {
            amt.append(this.getSymbol());
        }
        if (this.isDecimalSupported()) {
            amt.append(UtilString.format(amount));
        } else {
            String s = String.valueOf(amount);
            String[] ss = s.split("\\.");
            if (ss.length > 0) {
                s = ss[0];
            }
            amt.append(NumberFormat.getInstance().format(Double.parseDouble(s)));
        }
        amt.append(" ");
        if (amount != 1.0) {
            amt.append(this.getPlural().replace("_", " "));
        } else {
            amt.append(this.getSingular().replace("_", " "));
        }
        return amt.toString();
    }

    public Component componentFormat(double amount) {
        return Component.text(format(amount)).color(color);
    }

    public String getDisplayNameLegacy() {
        return LegacyComponentSerializer.legacyAmpersand().serialize(getDisplayName());
    }

    public Component getDisplayName() {
        return Component.text(singular).color(color);
    }

    public boolean isDefaultCurrency() {
        return this.defaultCurrency;
    }

    public void setDefaultCurrency(boolean defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public boolean isPayable() {
        return this.payable;
    }

    public void setPayable(boolean payable) {
        this.payable = payable;
    }

    public boolean isDecimalSupported() {
        return this.decimalSupported;
    }

    public void setDecimalSupported(boolean decimalSupported) {
        this.decimalSupported = decimalSupported;
    }

    public TextColor getColor() {
        return this.color;
    }

    public void setColor(TextColor color) {
        this.color = color;
    }

    public @Nullable String getSymbol() {
        return this.symbol;
    }

    public void setSymbol(@Nullable String symbol) {
        this.symbol = symbol;
    }

    public double getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @Override public boolean equals(@Nullable final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Currency currency = (Currency) o;
        return uuid.equals(currency.uuid);
    }

    @Override public int hashCode() {
        return uuid.hashCode();
    }

}


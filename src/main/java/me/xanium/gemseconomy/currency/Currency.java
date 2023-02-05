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
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.jetbrains.annotations.NotNull;

import java.text.NumberFormat;
import java.util.UUID;

@SuppressWarnings("unused")
@DefaultQualifier(NonNull.class)
public class Currency {

    private final UUID uuid;
    private String singular;
    private String plural;
    @Nullable private String symbol = null;
    private TextColor color = NamedTextColor.WHITE;
    private boolean decimalSupported = true;
    private boolean payable = true;
    private boolean defaultCurrency = false;
    private double defaultBalance = 0D;
    private double maxBalance = 0D; // zero means unlimited
    private double exchangeRate = 0D;

    public Currency(@NotNull UUID uuid, @NotNull String singular, @NotNull String plural) {
        this.uuid = uuid;
        this.singular = singular;
        this.plural = plural;
    }

    public void setSingular(@NotNull String singular) {
        this.singular = singular;
    }

    public void setPlural(@NotNull String plural) {
        this.plural = plural;
    }

    public void setDefaultBalance(double defaultBalance) {
        this.defaultBalance = defaultBalance;
    }

    public void setMaxBalance(double maxBalance) {
        this.maxBalance = maxBalance;
    }

    public @NotNull UUID getUuid() {
        return this.uuid;
    }

    public @NotNull String getSingular() {
        return this.singular;
    }

    public @NotNull String getPlural() {
        return this.plural;
    }

    public double getDefaultBalance() {
        return this.defaultBalance;
    }

    public double getMaxBalance() {
        return this.maxBalance == 0D ? Integer.MAX_VALUE : this.maxBalance;
    }

    public @NotNull String format(double amount) {
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

    public @NotNull Component componentFormat(double amount) {
        return Component.text(format(amount)).color(color);
    }

    public @NotNull String getDisplayNameLegacy() {
        return LegacyComponentSerializer.legacyAmpersand().serialize(getDisplayName());
    }

    public @NotNull Component getDisplayName() {
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

    public @NotNull TextColor getColor() {
        return this.color;
    }

    public void setColor(@NotNull TextColor color) {
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
}


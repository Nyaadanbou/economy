/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package cc.mewcraft.economy.api;

import cc.mewcraft.economy.EconomyPlugin;

import com.google.common.base.Preconditions;

import java.util.List;
import java.util.UUID;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("unused")
public class EconomyImpl implements Economy {
    public final EconomyPlugin plugin;

    public EconomyImpl(EconomyPlugin plugin) {
        this.plugin = plugin;
    }

    @Override public @NonNull Account pullAccount(@NonNull UUID uuid) {
        return plugin.getAccountManager().createAccount(uuid);
    }

    @Override public @Nullable Account getAccount(@NonNull UUID uuid) {
        return plugin.getAccountManager().fetchAccount(uuid);
    }

    @Override public boolean hasAccount(@NonNull UUID uuid) {
        return plugin.getAccountManager().hasAccount(uuid);
    }

    @Override public void deposit(@NonNull UUID uuid, double amount) {
        Preconditions.checkNotNull(uuid, "uuid");
        pullAccount(uuid).deposit(plugin.getCurrencyManager().getDefaultCurrency(), amount);
    }

    @Override public void deposit(@NonNull UUID uuid, double amount, @NonNull Currency currency) {
        Preconditions.checkNotNull(uuid, "uuid");
        Preconditions.checkNotNull(currency, "currency");
        pullAccount(uuid).deposit(currency, amount);
    }

    @Override public void withdraw(@NonNull UUID uuid, double amount) {
        Preconditions.checkNotNull(uuid, "uuid");
        pullAccount(uuid).withdraw(plugin.getCurrencyManager().getDefaultCurrency(), amount);
    }

    @Override public void withdraw(@NonNull UUID uuid, double amount, @NonNull Currency currency) {
        Preconditions.checkNotNull(uuid, "uuid");
        Preconditions.checkNotNull(currency, "currency");
        pullAccount(uuid).withdraw(currency, amount);
    }

    @Override public double getBalance(@NonNull UUID uuid) {
        Preconditions.checkNotNull(uuid, "uuid");
        return pullAccount(uuid).getBalance(plugin.getCurrencyManager().getDefaultCurrency());
    }

    @Override public double getBalance(@NonNull UUID uuid, @NonNull Currency currency) {
        Preconditions.checkNotNull(uuid, "uuid");
        Preconditions.checkNotNull(currency, "currency");
        return pullAccount(uuid).getBalance(currency);
    }

    @Override public @Nullable Currency getCurrency(@NonNull String name) {
        Preconditions.checkNotNull(name, "name");
        return plugin.getCurrencyManager().getCurrency(name);
    }

    @Override public @NonNull Currency getDefaultCurrency() {
        return plugin.getCurrencyManager().getDefaultCurrency();
    }

    @Override public @NonNull List<Currency> getLoadedCurrencies() {
        return plugin.getCurrencyManager().getLoadedCurrencies();
    }
}

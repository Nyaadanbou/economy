package cc.mewcraft.economy.event;

import cc.mewcraft.economy.api.Account;
import cc.mewcraft.economy.api.Currency;
import org.bukkit.Bukkit;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public class EconomyPayEvent extends Event implements Cancellable {

    private static final HandlerList HANDLERS = new HandlerList();
    private boolean cancel;
    private final Currency currency;
    private final Account payer;
    private final Account received;
    private final double amount;

    public EconomyPayEvent(Currency currency, Account payer, Account received, double amount) {
        super(!Bukkit.isPrimaryThread());
        this.currency = currency;
        this.payer = payer;
        this.received = received;
        this.amount = amount;
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public Account getPayer() {
        return this.payer;
    }

    public Account getReceived() {
        return this.received;
    }

    public double getAmount() {
        return this.amount;
    }

    public String getAmountFormatted() {
        return getCurrency().simpleFormat(getAmount());
    }

    @Override
    public boolean isCancelled() {
        return this.cancel;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancel = cancelled;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS;
    }

}

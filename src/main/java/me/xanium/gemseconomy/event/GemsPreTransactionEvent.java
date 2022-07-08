package me.xanium.gemseconomy.event;

import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.utils.TranactionType;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GemsPreTransactionEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private final Currency currency;
    private final Account account;
    private final double amount;
    private final TranactionType type;
    private boolean cancelled = false;

    public GemsPreTransactionEvent(Currency currency, Account account, double amount, TranactionType type) {
        this.currency = currency;
        this.account = account;
        this.amount = amount;
        this.type = type;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Account getAccount() {
        return account;
    }

    public double getAmount() {
        return amount;
    }

    public TranactionType getType() {
        return type;
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
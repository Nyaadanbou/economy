package me.xanium.gemseconomy.event;

import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.currency.Currency;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class GemsPayEvent extends Event implements Cancellable {

    private static final HandlerList handlerList = new HandlerList();
    private boolean cancel;
    private final Currency currency;
    private final Account payer;
    private final Account received;
    private final double amount;

    public GemsPayEvent(Currency currency, Account payer, Account received, double amount) {
        this.currency = currency;
        this.payer = payer;
        this.received = received;
        this.amount = amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public Account getPayer() {
        return payer;
    }

    public Account getReceived() {
        return received;
    }

    public double getAmount() {
        return amount;
    }

    public String getAmountFormatted(){
        return getCurrency().format(getAmount());
    }

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancel = cancelled;
    }
}

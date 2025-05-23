package cc.mewcraft.economy.event;

import cc.mewcraft.economy.api.Account;
import cc.mewcraft.economy.api.Currency;
import cc.mewcraft.economy.utils.TransactionType;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@SuppressWarnings("unused")
@DefaultQualifier(NonNull.class)
public class EconomyPostTransactionEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();
    private final Currency currency;
    private final Account account;
    private final double amount;
    private final TransactionType type;

    public EconomyPostTransactionEvent(Currency currency, Account account, double amount, TransactionType type) {
        super(!Bukkit.isPrimaryThread());
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

    public TransactionType getType() {
        return type;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return HANDLERS;
    }

    public static @NonNull HandlerList getHandlerList() {
        return HANDLERS;
    }

}

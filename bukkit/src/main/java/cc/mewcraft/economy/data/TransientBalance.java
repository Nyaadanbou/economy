package cc.mewcraft.economy.data;

import cc.mewcraft.economy.currency.BalanceTop;
import cc.mewcraft.economy.currency.BalanceTopRepository;

/**
 * Keeps a bit of information about how much balance an account has.
 * <p>
 * This record is NOT meant to stored in memory for long time.
 *
 * @param name   account name
 * @param amount balance amount
 * @see BalanceTop
 * @see BalanceTopRepository
 */
public record TransientBalance(String name, double amount) {
    public boolean significant() {
        return this.amount > 1;
    }
}

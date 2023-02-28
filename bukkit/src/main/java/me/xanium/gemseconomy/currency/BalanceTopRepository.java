package me.xanium.gemseconomy.currency;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import me.lucko.helper.promise.Promise;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.data.TransientBalance;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

/**
 * Provides access to get {@link BalanceTop} instances.
 */
public class BalanceTopRepository {

    private final GemsEconomy plugin;
    private final LoadingCache<UUID, BalanceTop> topLists;

    public BalanceTopRepository(GemsEconomy plugin) {
        this.plugin = plugin;
        this.topLists = CacheBuilder.newBuilder()
            .expireAfterWrite(Duration.ofMinutes(5))
            .build(CacheLoader.from(uuid -> {
                Currency currency = BalanceTopRepository.this.plugin.getCurrencyManager().getCurrency(uuid);

                if (currency == null)
                    return BalanceTop.EMPTY; // should not happen, but anyway

                List<TransientBalance> balances = BalanceTopRepository.this.plugin.getDataStore()
                    .getTransientBalances(currency).join()
                    .stream()
                    .filter(bal -> bal.amount() >= 1) // ignore "ghost" accounts
                    .toList();

                return new BalanceTop(balances);
            }));
    }

    /**
     * Gets a balance top list for specific currency.
     *
     * @param currency the currency from which the balance top list is fetched
     *
     * @return a promise which contains the results
     */
    public Promise<BalanceTop> computeByCurrency(Currency currency) {
        return Promise.supplyingAsync(() -> this.topLists.getUnchecked(currency.getUuid()));
    }

    /**
     * Flush all cached lists.
     */
    public void flushLists() {
        this.topLists.invalidateAll();
    }

}
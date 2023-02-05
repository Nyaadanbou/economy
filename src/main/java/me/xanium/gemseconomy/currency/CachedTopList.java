package me.xanium.gemseconomy.currency;

import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

public class CachedTopList {

    private final Currency currency;
    private final int amount;
    private final long cacheTime;
    private LinkedList<CachedTopListEntry> results;

    public CachedTopList(Currency currency, int amount, long cacheTime) {
        this.results = new LinkedList<>();
        this.currency = currency;
        this.amount = amount;
        this.cacheTime = cacheTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - this.getCacheTime() > TimeUnit.MINUTES.toMillis(3L);
    }

    public Currency getCurrency() {
        return this.currency;
    }

    public int getAmount() {
        return this.amount;
    }

    public long getCacheTime() {
        return this.cacheTime;
    }

    public LinkedList<CachedTopListEntry> getResults() {
        return this.results;
    }

    public void setResults(LinkedList<CachedTopListEntry> results) {
        this.results = results;
    }
}

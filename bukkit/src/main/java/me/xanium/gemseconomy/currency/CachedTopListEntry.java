package me.xanium.gemseconomy.currency;

public class CachedTopListEntry {

    private String name;
    private double amount;

    public CachedTopListEntry(String name, double amount) {
        this.name = name;
        this.amount = amount;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}

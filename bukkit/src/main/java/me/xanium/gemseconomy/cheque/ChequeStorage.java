package me.xanium.gemseconomy.cheque;

import me.xanium.gemseconomy.GemsEconomy;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

public class ChequeStorage {
    private String issuer;
    private String currency;
    private double value;

    public static final NamespacedKey key = new NamespacedKey(GemsEconomy.getInstance(), "cheque");

    public static ChequeStorage read(ItemStack itemStack) {
        return itemStack.getItemMeta().getPersistentDataContainer().get(key, ChequeStorageType.INSTANCE);
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public ChequeStorage(String issuer, String currency, double value) {
        this.issuer = issuer;
        this.currency = currency;
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChequeStorage that = (ChequeStorage) o;
        return Objects.equals(issuer, that.issuer) && Objects.equals(currency, that.currency) && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(issuer, currency, value);
    }

}
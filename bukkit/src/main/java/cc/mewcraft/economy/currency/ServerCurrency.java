package cc.mewcraft.economy.currency;

import cc.mewcraft.economy.EconomyPlugin;
import cc.mewcraft.economy.api.Currency;
import cc.mewcraft.economy.utils.UtilString;
import com.google.common.base.Preconditions;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.examination.Examinable;
import net.kyori.examination.ExaminableProperty;
import net.kyori.examination.string.StringExaminer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.UUID;
import java.util.stream.Stream;

@SuppressWarnings("unused")
@DefaultQualifier(NonNull.class)
public class ServerCurrency implements Currency, Examinable {
    private final UUID uuid;
    private String name;
    private @Nullable String symbol;
    private TextColor color = NamedTextColor.WHITE;
    private boolean decimalSupported = true;
    private boolean payable = true;
    private boolean defaultCurrency = false;
    private double defaultBalance = 0D;
    private double maxBalance = 0D; // zero means unlimited
    private double exchangeRate = 0D;

    public ServerCurrency(@NonNull UUID uuid, @NonNull String name) {
        Preconditions.checkNotNull(uuid, "uuid");
        Preconditions.checkNotNull(name, "name");
        this.uuid = uuid;
        this.name = name;
    }

    @Override public void update(@NonNull Currency other) {
        Preconditions.checkNotNull(other, "other");
        name = other.getName();
        symbol = other.getSymbolNullable();
        color = other.getColor();
        decimalSupported = other.isDecimalSupported();
        payable = other.isPayable();
        defaultCurrency = other.isDefaultCurrency();
        defaultBalance = other.getDefaultBalance();
        maxBalance = other.getMaximumBalance();
        exchangeRate = other.getExchangeRate();
    }

    /* ---------------- Balance Amount ---------------- */

    @Override public String getName() {
        return name;
    }

    @Override public void setName(@NonNull String name) {
        Preconditions.checkNotNull(name, "name");
        this.name = name;
    }

    @Override public double getDefaultBalance() {
        return defaultBalance;
    }

    @Override public void setDefaultBalance(double defaultBalance) {
        Preconditions.checkArgument(defaultBalance >= 0, "defaultBalance >= 0");
        this.defaultBalance = defaultBalance;
    }

    @Override public double getMaximumBalance() {
        return maxBalance == 0D ? Integer.MAX_VALUE : maxBalance;
    }

    @Override public void setMaximumBalance(double maxBalance) {
        Preconditions.checkArgument(maxBalance >= 0, "maxBalance >= 0");
        this.maxBalance = maxBalance;
    }

    /* ---------------- Balance Display ---------------- */

    @Override public String simpleFormat(double amount) {
        String amountString = UtilString.format(amount, decimalSupported);
        String nameString = getName().replace("_", " ");
        return EconomyPlugin.lang().raw("msg_balance_simple_format",
                "amount", amountString,
                "name", nameString
        );
    }

    @Override public String fancyFormat(double amount) {
        String amountString = UtilString.format(amount, decimalSupported);
        String symbolString = getSymbolOrEmpty();
        String nameString = getName().replace("_", " ");
        return EconomyPlugin.lang().raw("msg_balance_fancy_format",
                "amount", amountString,
                "name", nameString,
                "symbol", symbolString
        );
    }

    @Override public @NonNull String getDisplayName() {
        return name;
    }

    @Override public TextColor getColor() {
        return color;
    }

    @Override public void setColor(@Nullable TextColor color) {
        this.color = color == null ? NamedTextColor.WHITE : color;
    }

    @Override public @Nullable String getSymbolNullable() {
        return symbol;
    }

    @Override public String getSymbolOrEmpty() {
        return symbol != null ? symbol : "";
    }

    @Override public void setSymbol(@Nullable String symbol) {
        this.symbol = symbol;
    }

    /* ---------------- Other ---------------- */

    @Override public UUID getUuid() {
        return uuid;
    }

    @Override public boolean isDefaultCurrency() {
        return defaultCurrency;
    }

    @Override public void setDefaultCurrency(boolean defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    @Override public boolean isDecimalSupported() {
        return decimalSupported;
    }

    @Override public void setDecimalSupported(boolean decimal) {
        decimalSupported = decimal;
    }

    @Override public boolean isPayable() {
        return this.payable;
    }

    @Override public void setPayable(boolean payable) {
        this.payable = payable;
    }

    @Override public double getExchangeRate() {
        return exchangeRate;
    }

    @Override public void setExchangeRate(double exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @Override public @NonNull Stream<? extends ExaminableProperty> examinableProperties() {
        return Stream.of(
                ExaminableProperty.of("uuid", uuid),
                ExaminableProperty.of("name", name),
                ExaminableProperty.of("symbol", symbol),
                ExaminableProperty.of("color", color),
                ExaminableProperty.of("decimalSupported", decimalSupported),
                ExaminableProperty.of("payable", payable),
                ExaminableProperty.of("defaultCurrency", defaultCurrency),
                ExaminableProperty.of("defaultBalance", defaultBalance),
                ExaminableProperty.of("maxBalance", maxBalance),
                ExaminableProperty.of("exchangeRate", exchangeRate)
        );
    }

    @Override public boolean equals(final @Nullable Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final ServerCurrency currency = (ServerCurrency) o;
        return uuid.equals(currency.uuid);
    }

    @Override public int hashCode() {
        return uuid.hashCode();
    }

    @Override public String toString() {
        return StringExaminer.simpleEscaping().examine(this);
    }
}


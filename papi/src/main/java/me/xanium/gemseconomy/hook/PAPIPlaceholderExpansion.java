package me.xanium.gemseconomy.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.lucko.helper.terminable.Terminable;
import me.xanium.gemseconomy.api.Account;
import me.xanium.gemseconomy.api.Currency;
import me.xanium.gemseconomy.api.GemsEconomy;
import me.xanium.gemseconomy.api.GemsEconomyProvider;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;

public class PAPIPlaceholderExpansion implements Terminable {
    private final Expansion expansion;
    private final GemsEconomy economy;

    public PAPIPlaceholderExpansion() {
        expansion = new Expansion();
        economy = GemsEconomyProvider.get();
    }

    public void register() {
        expansion.register();
    }

    @Override public void close() {
        expansion.unregister();
    }

    class Expansion extends PlaceholderExpansion {
        @Override
        public @NonNull String getIdentifier() {
            return "econ";
        }

        @Override
        public @NonNull String getAuthor() {
            return "Nailm";
        }

        @Override
        public @NonNull String getVersion() {
            return "1.1";
        }

        @Override
        public @Nullable String onRequest(OfflinePlayer player, @NonNull String params) {
            if (player == null) {
                return "";
            }

            Account account = economy.getAccount(player.getUniqueId());
            if (account == null) {
                return "";
            }

            if (params.startsWith("balance")) {
                // <econ_balance:r>
                // <econ_balance:c>
                // <econ_balance:c:acc>
                return parse(account, params.substring(7), (currency, balance) -> String.valueOf(balance));

            } else if (params.startsWith("simple_balance")) {
                // <econ_simple_balance:r>
                // <econ_simple_balance:c>
                // <econ_simple_balance:c:acc>
                return parse(account, params.substring(14), Currency::simpleFormat);

            } else if (params.startsWith("fancy_balance")) {
                // <econ_fancy_balance:r>
                // <econ_fancy_balance:c>
                // <econ_fancy_balance:c:acc>
                return parse(account, params.substring(13), Currency::fancyFormat);
            }

            return "";
        }

        private @Nullable String parse(@NonNull Account account, @NonNull String input, @NonNull BiFunction<Currency, Double, String> balanceStringFunc) {
            Currency currency;
            boolean asHeap = false;

            String[] arguments = input.split(":");
            if (arguments.length == 1) {
                currency = economy.getCurrency(arguments[0]);
            } else if (arguments.length == 2) {
                currency = economy.getCurrency(arguments[0]);
                asHeap = Objects.equals(arguments[1], "heap");
            } else {
                // no arguments provided
                throw new IllegalArgumentException("no arguments are provided");
            }

            if (currency == null) {
                // unknown currency - return empty string
                return "";
            }

            if (asHeap) {
                return balanceStringFunc.apply(currency, account.getBalance(currency));
            } else {
                return balanceStringFunc.apply(currency, account.getHeapBalance(currency));
            }
        }
    }
}
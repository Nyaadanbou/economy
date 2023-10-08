package me.xanium.gemseconomy.hook;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.lucko.helper.terminable.Terminable;
import me.xanium.gemseconomy.api.Account;
import me.xanium.gemseconomy.api.Currency;
import me.xanium.gemseconomy.api.GemsEconomy;
import me.xanium.gemseconomy.api.GemsEconomyProvider;
import org.bukkit.OfflinePlayer;

import java.util.Objects;
import java.util.function.BiFunction;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

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
        // --- Keys ---
        private static final String BALANCE_KEY = "balance";
        private static final String SIMPLE_BALANCE_KEY = "simple_balance";
        private static final String FANCY_BALANCE_KEY = "fancy_balance";
        // --- Lengths ---
        private static final int BALANCE_KEY_LENGTH = BALANCE_KEY.length() + 1; // plus 1 to not include the first ":" symbol
        private static final int SIMPLE_BALANCE_KEY_LENGTH = SIMPLE_BALANCE_KEY.length() + 1;
        private static final int FANCY_BALANCE_KEY_LENGTH = FANCY_BALANCE_KEY.length() + 1;

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

            if (params.startsWith(BALANCE_KEY)) {
                // <econ_balance:r>
                // <econ_balance:c>
                // <econ_balance:c:heap>
                return parse(account, params.substring(BALANCE_KEY_LENGTH), (currency, balance) -> String.valueOf(balance));

            } else if (params.startsWith(SIMPLE_BALANCE_KEY)) {
                // <econ_simple_balance:r>
                // <econ_simple_balance:c>
                // <econ_simple_balance:c:heap>
                return parse(account, params.substring(SIMPLE_BALANCE_KEY_LENGTH), Currency::simpleFormat);

            } else if (params.startsWith(FANCY_BALANCE_KEY)) {
                // <econ_fancy_balance:r>
                // <econ_fancy_balance:c>
                // <econ_fancy_balance:c:heap>
                return parse(account, params.substring(FANCY_BALANCE_KEY_LENGTH), Currency::fancyFormat);
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

            return asHeap
                    ? balanceStringFunc.apply(currency, account.getHeapBalance(currency))
                    : balanceStringFunc.apply(currency, account.getBalance(currency));
        }
    }
}
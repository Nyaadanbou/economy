package me.xanium.gemseconomy.command.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.currency.Currency;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

@NonnullByDefault
public class CurrencyArgument extends CommandArgument<CommandSender, Currency> {

    public CurrencyArgument(
            boolean required,
            String name,
            String defaultValue,
            @Nullable BiFunction<@NonNull CommandContext<CommandSender>, String, List<String>> suggestionsProvider,
            ArgumentDescription defaultDescription) {
        super(required, name, new Parser(), defaultValue, Currency.class, suggestionsProvider, defaultDescription);
    }

    public static CurrencyArgument of(final String name) {
        return builder(name).build();
    }

    public static CurrencyArgument optional(final String name) {
        return builder(name).asOptional().build();
    }

    public static CurrencyArgument.Builder builder(final String name) {
        return new Builder(name);
    }

    public static final class Parser implements ArgumentParser<CommandSender, Currency> {
        @Override
        public ArgumentParseResult<Currency> parse(
                final CommandContext<CommandSender> commandContext,
                final Queue<String> inputQueue
        ) {
            CommandSender sender = commandContext.getSender();
            String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(CurrencyArgument.Parser.class, commandContext));
            }

            Currency currency = GemsEconomy.inst().getCurrencyManager().getCurrency(input);
            if (currency != null) {
                inputQueue.remove();
                return ArgumentParseResult.success(currency);
            }

            return ArgumentParseResult.failure(new IllegalArgumentException(
                    GemsEconomy.lang().toLegacy(sender, "err_unknown_currency")
            ));
        }

        @Override
        public List<String> suggestions(
                final CommandContext<CommandSender> commandContext,
                final String input
        ) {
            // Sender must have the permission:
            // gemseconomy.currency.<singular>.completion
            // to see corresponding tab completions
            CommandSender sender = commandContext.getSender();
            List<String> suggestions = new ArrayList<>();
            List<Currency> currencies = GemsEconomy.inst().getCurrencyManager().getCurrencies();
            for (Currency currency : currencies) {
                String singular = currency.getSingular();
                if (sender.hasPermission("gemseconomy.currency.completion." + singular)) {
                    suggestions.add(singular);
                }
            }
            return suggestions;
        }
    }

    public static final class Builder extends CommandArgument.TypedBuilder<CommandSender, Currency, CurrencyArgument.Builder> {
        private Builder(final String name) {
            super(Currency.class, name);
        }

        @Override
        public CurrencyArgument build() {
            return new CurrencyArgument(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }
    }

}


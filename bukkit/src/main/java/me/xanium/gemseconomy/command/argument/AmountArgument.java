package me.xanium.gemseconomy.command.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import me.xanium.gemseconomy.GemsEconomyPlugin;
import me.xanium.gemseconomy.currency.Currency;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
@NonnullByDefault
public class AmountArgument extends CommandArgument<CommandSender, Double> {

    public AmountArgument(
        boolean required,
        String name,
        String defaultValue,
        @Nullable BiFunction<CommandContext<CommandSender>, String, List<String>> suggestionsProvider,
        ArgumentDescription defaultDescription) {
        super(required, name, new Parser(), defaultValue, Double.class, suggestionsProvider, defaultDescription);
    }

    public static AmountArgument of(final String name) {
        return builder(name).build();
    }

    public static AmountArgument optional(final String name) {
        return builder(name).asOptional().build();
    }

    public static AmountArgument.Builder builder(final String name) {
        return new AmountArgument.Builder(name);
    }

    public static final class Parser implements ArgumentParser<CommandSender, Double> {

        @Override
        public ArgumentParseResult<Double> parse(
            final CommandContext<CommandSender> commandContext,
            final Queue<String> inputQueue
        ) {
            CommandSender sender = commandContext.getSender();
            String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(AmountArgument.Parser.class, commandContext));
            }

            Optional<String> currencyReferrer = commandContext.getOptional("currencyReferrer");
            if (currencyReferrer.isPresent()) { // If this AmountArgument should refer to a specific Currency
                String key = currencyReferrer.get();
                Optional<Currency> currency = commandContext.getOptional(key);
                if (currency.isPresent()) {
                    inputQueue.remove();
                    return parseAmount(sender, input, currency.get());
                } else {
                    return ArgumentParseResult.failure(new IllegalArgumentException());
                }
            } else if (commandContext.contains("currency")) { // If no Currency is specified, check the CurrencyArgument with "currency" key
                Currency currency = commandContext.get("currency");
                inputQueue.remove();
                return parseAmount(sender, input, currency);
            } else { // Else, fallback to the default Currency
                Currency currency = GemsEconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency();
                inputQueue.remove();
                return parseAmount(sender, input, currency);
            }
        }

        @Override
        public List<String> suggestions(
            final CommandContext<CommandSender> commandContext,
            final String input
        ) {
            return List.of("1", "10", "100", "1000");
        }

        private ArgumentParseResult<Double> parseAmount(CommandSender sender, String input, Currency currency) {
            double amount;
            if (currency.isDecimalSupported()) {
                try {
                    amount = Double.parseDouble(input);
                    validateInput(sender, amount);
                } catch (NumberFormatException ex) {
                    return ArgumentParseResult.failure(new IllegalArgumentException(
                        GemsEconomyPlugin.lang().legacy(sender, "err_invalid_amount")
                    ));
                }
            } else {
                try {
                    amount = Integer.parseInt(input);
                    validateInput(sender, amount);
                } catch (NumberFormatException ex) {
                    return ArgumentParseResult.failure(new IllegalArgumentException(
                        GemsEconomyPlugin.lang().legacy(sender, "err_invalid_amount")
                    ));
                }
            }
            return ArgumentParseResult.success(amount);
        }

        private static void validateInput(CommandSender sender, double amount) {
            if (sender instanceof ConsoleCommandSender) {
                if (amount < 0) {
                    throw new NumberFormatException();
                }
            } else if (amount <= 0) {
                throw new NumberFormatException();
            }
        }
    }

    public static final class Builder extends CommandArgument.TypedBuilder<CommandSender, Double, AmountArgument.Builder> {
        private Builder(final String name) {
            super(Double.class, name);
        }

        @Override
        public AmountArgument build() {
            return new AmountArgument(
                this.isRequired(),
                this.getName(),
                this.getDefaultValue(),
                this.getSuggestionsProvider(),
                this.getDefaultDescription()
            );
        }
    }

}



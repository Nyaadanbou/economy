package me.xanium.gemseconomy.command.argument;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.xanium.gemseconomy.GemsEconomyPlugin;
import me.xanium.gemseconomy.api.Currency;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;

import java.util.List;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

@SuppressWarnings("UnstableApiUsage")
@DefaultQualifier(NonNull.class)
public class AmountParser implements ArgumentParser<CommandSourceStack, Double>, BlockingSuggestionProvider.Strings<CommandSourceStack> {

    public static @NonNull ParserDescriptor<CommandSourceStack, Double> amountParser() {
        return ParserDescriptor.of(new AmountParser(), Double.class);
    }

    public static CommandComponent.@NonNull Builder<CommandSourceStack, Double> amountComponent() {
        return CommandComponent.<CommandSourceStack, Double>builder().parser(amountParser());
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Double> parse(@NonNull CommandContext<@NonNull CommandSourceStack> commandContext, @NonNull CommandInput commandInput) {
        CommandSender sender = commandContext.sender().getSender();
        String input = commandInput.peekString();

        Optional<String> currencyReferrer = commandContext.optional("currencyReferrer");
        if (currencyReferrer.isPresent()) { // If this AmountParser should refer to a specific currency
            String key = currencyReferrer.get();
            Optional<Currency> currency = commandContext.optional(key);
            if (currency.isPresent()) {
                commandInput.peekString();
                return parseAmount(sender, input, currency.get());
            } else {
                return ArgumentParseResult.failure(new IllegalArgumentException());
            }
        } else if (commandContext.contains("currency")) { // If no Currency is specified, check the currencyArgument with "currency" key
            Currency currency = commandContext.get("currency");
            commandInput.peekString();
            return parseAmount(sender, input, currency);
        } else { // Else, fallback to the default currency
            Currency currency = GemsEconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency();
            commandInput.peekString();
            return parseAmount(sender, input, currency);
        }
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<CommandSourceStack> commandContext, @NonNull CommandInput input) {
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



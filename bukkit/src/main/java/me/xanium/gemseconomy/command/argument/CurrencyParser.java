package me.xanium.gemseconomy.command.argument;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.xanium.gemseconomy.GemsEconomyPlugin;
import me.xanium.gemseconomy.api.Currency;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
public class CurrencyParser implements ArgumentParser<CommandSourceStack, Currency>, BlockingSuggestionProvider.Strings<CommandSourceStack> {

    public static @NonNull ParserDescriptor<CommandSourceStack, Currency> currencyParser() {
        return ParserDescriptor.of(new CurrencyParser(), Currency.class);
    }

    public static CommandComponent.@NonNull Builder<CommandSourceStack, Currency> currencyComponent() {
        return CommandComponent.<CommandSourceStack, Currency>builder().parser(currencyParser());
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Currency> parse(@NonNull CommandContext<@NonNull CommandSourceStack> commandContext, @NonNull CommandInput commandInput) {
        CommandSender sender = commandContext.sender().getSender();
        String input = commandInput.peekString();

        Currency currency = GemsEconomyPlugin.getInstance().getCurrencyManager().getCurrency(input);
        if (currency != null) {
            commandInput.readString();
            return ArgumentParseResult.success(currency);
        }

        return ArgumentParseResult.failure(new IllegalArgumentException(
                GemsEconomyPlugin.lang().legacy(sender, "err_unknown_currency")
        ));
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<CommandSourceStack> commandContext, @NonNull CommandInput input) {
        // Sender must have the permission:
        // gemseconomy.currency.completion.<name>
        // to see corresponding tab completions
        CommandSender sender = commandContext.sender().getSender();
        List<String> suggestions = new ArrayList<>();
        Collection<Currency> currencies = GemsEconomyPlugin.getInstance().getCurrencyManager().getLoadedCurrencies();
        for (Currency currency : currencies) {
            String name = currency.getName();
            if (sender.hasPermission("gemseconomy.currency.completion." + name)) {
                suggestions.add(name);
            }
        }
        return suggestions;
    }
}


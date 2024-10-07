package me.xanium.gemseconomy.command.argument;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.xanium.gemseconomy.GemsEconomyPlugin;
import me.xanium.gemseconomy.api.Account;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.bukkit.BukkitCommandContextKeys;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

@SuppressWarnings("UnstableApiUsage")
@DefaultQualifier(NonNull.class)
public class AccountParser implements ArgumentParser<CommandSourceStack, Account>, BlockingSuggestionProvider.Strings<CommandSourceStack> {

    public static @NonNull ParserDescriptor<CommandSourceStack, Account> accountParser() {
        return ParserDescriptor.of(new AccountParser(), Account.class);
    }

    public static CommandComponent.@NonNull Builder<CommandSourceStack, Account> accountComponent() {
        return CommandComponent.<CommandSourceStack, Account>builder().parser(accountParser());
    }

    @Override
    public @NonNull ArgumentParseResult<@NonNull Account> parse(@NonNull CommandContext<@NonNull CommandSourceStack> commandContext, @NonNull CommandInput commandInput) {
        String input = commandInput.peekString();
        CommandSender sender = commandContext.sender().getSender();

        // Prevent players from creating trash accounts
        if (!sender.hasPermission("gemseconomy.account.internal") && Bukkit.getOfflinePlayerIfCached(input) == null) {
            return ArgumentParseResult.failure(
                    new IllegalArgumentException(GemsEconomyPlugin.lang().legacy(sender, "err_player_is_null"))
            );
        }

        Account target = GemsEconomyPlugin.getInstance().getAccountManager().fetchAccount(input);
        if (target != null) {
            commandInput.readString();
            return ArgumentParseResult.success(target);
        }

        return ArgumentParseResult.failure(
                new IllegalArgumentException(GemsEconomyPlugin.lang().legacy(sender, "err_player_is_null"))
        );
    }

    @Override
    public @NonNull Iterable<@NonNull String> stringSuggestions(@NonNull CommandContext<CommandSourceStack> commandContext, @NonNull CommandInput input) {
        List<String> output = new ArrayList<>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            final CommandSender bukkit = commandContext.get(BukkitCommandContextKeys.BUKKIT_COMMAND_SENDER);
            if (bukkit instanceof Player && !((Player) bukkit).canSee(player)) {
                continue;
            }
            output.add(player.getName());
        }

        return output;
    }
}


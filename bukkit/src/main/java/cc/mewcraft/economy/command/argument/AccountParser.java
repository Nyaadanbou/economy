package cc.mewcraft.economy.command.argument;

import cc.mewcraft.economy.EconomyPlugin;
import cc.mewcraft.economy.api.Account;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.incendo.cloud.bukkit.BukkitCommandContextKeys;
import org.incendo.cloud.component.CommandComponent;
import org.incendo.cloud.context.CommandContext;
import org.incendo.cloud.context.CommandInput;
import org.incendo.cloud.parser.ArgumentParseResult;
import org.incendo.cloud.parser.ArgumentParser;
import org.incendo.cloud.parser.ParserDescriptor;
import org.incendo.cloud.suggestion.BlockingSuggestionProvider;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings({"UnstableApiUsage", "unused"})
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
        if (!sender.hasPermission("economy.account.internal") && Bukkit.getOfflinePlayerIfCached(input) == null) {
            return ArgumentParseResult.failure(
                    new IllegalArgumentException(EconomyPlugin.lang().legacy(sender, "err_player_is_null"))
            );
        }

        Account target = EconomyPlugin.getInstance().getAccountManager().fetchAccount(input);
        if (target != null) {
            commandInput.readString();
            return ArgumentParseResult.success(target);
        }

        return ArgumentParseResult.failure(
                new IllegalArgumentException(EconomyPlugin.lang().legacy(sender, "err_player_is_null"))
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


package me.xanium.gemseconomy.commandsv3.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.bukkit.BukkitCommandContextKeys;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.function.BiFunction;

@NonnullByDefault
public class AccountArgument extends CommandArgument<CommandSender, Account> {

    public AccountArgument(boolean required,
            String name,
            String defaultValue,
            @Nullable BiFunction<CommandContext<CommandSender>, String, List<String>> suggestionsProvider,
            ArgumentDescription defaultDescription) {
        super(required, name, new Parser(), defaultValue, Account.class, suggestionsProvider, defaultDescription);
    }

    public static AccountArgument of(final String name) {
        return builder(name).build();
    }

    public static AccountArgument optional(final String name) {
        return builder(name).asOptional().build();
    }

    public static AccountArgument.Builder builder(final String name) {
        return new AccountArgument.Builder(name);
    }

    public static final class Parser implements ArgumentParser<CommandSender, Account> {
        @Override
        public ArgumentParseResult<Account> parse(
                final CommandContext<CommandSender> commandContext,
                final Queue<String> inputQueue
        ) {
            String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(CurrencyArgument.Parser.class, commandContext));
            }

            CommandSender sender = commandContext.getSender();

            // Prevent players from creating trash accounts
            if (!sender.hasPermission("gemseconomy.account.internal") && Bukkit.getOfflinePlayerIfCached(input) == null) {
                return ArgumentParseResult.failure(
                        new IllegalArgumentException(GemsEconomy.lang().toLegacy(sender, "err_player_is_null"))
                );
            }

            Account target = GemsEconomy.inst().getAccountManager().getAccount(input);
            if (target != null) {
                inputQueue.remove();
                return ArgumentParseResult.success(target);
            }

            return ArgumentParseResult.failure(
                    new IllegalArgumentException(GemsEconomy.lang().toLegacy(sender, "err_player_is_null"))
            );
        }

        @Override
        public List<String> suggestions(
                final CommandContext<CommandSender> commandContext,
                final String input
        ) {
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

    public static final class Builder extends CommandArgument.TypedBuilder<CommandSender, Account, AccountArgument.Builder> {
        private Builder(final String name) {
            super(Account.class, name);
        }

        @Override
        public AccountArgument build() {
            return new AccountArgument(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }
    }

}


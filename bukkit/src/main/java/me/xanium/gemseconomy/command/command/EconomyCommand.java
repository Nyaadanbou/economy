package me.xanium.gemseconomy.command.command;

import cloud.commandframework.Command;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.command.AbstractCommand;
import me.xanium.gemseconomy.command.CommandManager;
import me.xanium.gemseconomy.command.argument.AccountArgument;
import me.xanium.gemseconomy.command.argument.AmountArgument;
import me.xanium.gemseconomy.command.argument.CurrencyArgument;
import me.xanium.gemseconomy.currency.Currency;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;

import static me.xanium.gemseconomy.GemsMessages.*;

public class EconomyCommand extends AbstractCommand {

    public EconomyCommand(GemsEconomy plugin, CommandManager manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command.Builder<CommandSender> builder = manager
            .commandBuilder("economy", "eco")
            .permission("gemseconomy.command.economy");

        Command<CommandSender> give = builder
            .literal("give")
            .argument(AccountArgument.of("account"))
            .argument(AmountArgument.of("amount"))
            .argument(CurrencyArgument.of("currency"))
            .flag(manager.flagBuilder("silent"))
            .handler(context -> {
                CommandSender sender = context.getSender();
                Account account = context.get("account");
                double amount = context.get("amount");
                Currency currency = context.get("currency");
                boolean silent = context.flags().hasFlag("silent");

                changeBalance(sender, account, amount, currency, false, silent);
            })
            .build();

        Command<CommandSender> take = builder
            .literal("take")
            .argument(AccountArgument.of("account"))
            .argument(AmountArgument.of("amount"))
            .argument(CurrencyArgument.of("currency"))
            .handler(context -> {
                CommandSender sender = context.getSender();
                Account account = context.get("account");
                double amount = context.get("amount");
                Currency currency = context.get("currency");

                changeBalance(sender, account, amount, currency, true, true);
            })
            .build();

        Command<CommandSender> set = builder
            .literal("set")
            .argument(AccountArgument.of("account"))
            .argument(AmountArgument.of("amount"))
            .argument(CurrencyArgument.of("currency"))
            .handler(context -> {
                CommandSender sender = context.getSender();
                Account account = context.get("account");
                double amount = context.get("amount");
                Currency currency = context.get("currency");

                setBalance(sender, account, amount, currency);
            })
            .build();

        Command<CommandSender> cached = builder
            .literal("cached")
            .handler(context -> {
                CommandSender sender = context.getSender();
                Collection<Account> cachedAccounts = GemsEconomy.getInstance().getAccountManager().getCachedAccounts();
                for (Account account : cachedAccounts) {
                    sender.sendMessage("Account: " + account.getDisplayName());
                }
                sender.sendMessage("Total cached: " + cachedAccounts.size());
            })
            .build();

        Command<CommandSender> flush = builder
            .literal("flush")
            .handler(context -> {
                GemsEconomy.getInstance().getAccountManager().flushAccounts();
                context.getSender().sendMessage("All cache flushed!");
            })
            .build();

        Command<CommandSender> debug = builder
            .literal("debug")
            .permission("gemseconomy.command.debug")
            .senderType(ConsoleCommandSender.class)
            .handler(context -> {
                CommandSender sender = context.getSender();
                GemsEconomy.getInstance().setDebug(!GemsEconomy.getInstance().isDebug());
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_debug_status")
                    .replaceText(STATUS_REPLACEMENT.apply(GemsEconomy.getInstance().isDebug()))
                );
            })
            .build();

        manager.register(List.of(
            give,
            take,
            set,
            cached,
            flush,
            debug
        ));
    }

    @NonnullByDefault
    private void changeBalance(CommandSender sender, Account account, double amount, Currency currency, boolean withdraw, boolean silent) {
        if (withdraw) {
            if (account.withdraw(currency, amount)) {
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_eco_taken")
                    .replaceText(AMOUNT_REPLACEMENT.apply(currency, amount))
                    .replaceText(ACCOUNT_REPLACEMENT.apply(account.getNickname()))
                );
            } else {
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "err_player_insufficient_funds")
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                    .replaceText(ACCOUNT_REPLACEMENT.apply(account.getNickname()))
                );
            }
        } else {
            if (account.deposit(currency, amount)) {
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_eco_added")
                    .replaceText(AMOUNT_REPLACEMENT.apply(currency, amount))
                    .replaceText(ACCOUNT_REPLACEMENT.apply(account.getNickname()))
                );
                Player target = Bukkit.getPlayer(account.getUuid());
                if (target != null && !silent) { // Send message if target player is online
                    GemsEconomy.lang().sendComponent(target, GemsEconomy.lang()
                        .component(target, "msg_received_currency")
                        .replaceText(AMOUNT_REPLACEMENT.apply(currency, amount))
                        .replaceText(ACCOUNT_REPLACEMENT.apply(GemsEconomy.lang().legacy(target, "msg_console_name")))
                    );
                }
            }
        }
    }

    @NonnullByDefault
    private void setBalance(CommandSender sender, Account account, double amount, Currency currency) {
        account.setBalance(currency, amount);
        GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
            .component(sender, "msg_eco_set")
            .replaceText(AMOUNT_REPLACEMENT.apply(currency, amount))
            .replaceText(ACCOUNT_REPLACEMENT.apply(account.getNickname()))
        );
    }

}

package cc.mewcraft.economy.command.command;

import cc.mewcraft.economy.EconomyPlugin;
import cc.mewcraft.economy.api.Account;
import cc.mewcraft.economy.api.Currency;
import cc.mewcraft.economy.command.AbstractCommand;
import cc.mewcraft.economy.command.CommandManager;
import cc.mewcraft.economy.command.argument.AccountParser;
import cc.mewcraft.economy.command.argument.AmountParser;
import cc.mewcraft.economy.command.argument.CurrencyParser;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.incendo.cloud.Command;
import org.incendo.cloud.parser.flag.CommandFlag;

import java.util.Collection;
import java.util.List;

import static cc.mewcraft.economy.EconomyMessages.ACCOUNT_REPLACEMENT;
import static cc.mewcraft.economy.EconomyMessages.AMOUNT_REPLACEMENT;
import static cc.mewcraft.economy.EconomyMessages.CURRENCY_REPLACEMENT;
import static cc.mewcraft.economy.EconomyMessages.STATUS_REPLACEMENT;

@SuppressWarnings("UnstableApiUsage")
public class EconomyCommand extends AbstractCommand {

    public EconomyCommand(EconomyPlugin plugin, CommandManager manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command.Builder<CommandSourceStack> builder = this.manager.getCommandManager()
                .commandBuilder("eco")
                .permission("economy.command.economy");

        Command<CommandSourceStack> give = builder
                .literal("give")
                .required("account", AccountParser.accountParser())
                .required("amount", AmountParser.amountParser())
                .required("currency", CurrencyParser.currencyParser())
                .flag(CommandFlag.builder("silent"))
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Account account = context.get("account");
                    double amount = context.get("amount");
                    Currency currency = context.get("currency");
                    boolean silent = context.flags().hasFlag("silent");

                    changeBalance(sender, account, amount, currency, false, silent);
                })
                .build();

        Command<CommandSourceStack> take = builder
                .literal("take")
                .required("account", AccountParser.accountParser())
                .required("amount", AmountParser.amountParser())
                .required("currency", CurrencyParser.currencyParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Account account = context.get("account");
                    double amount = context.get("amount");
                    Currency currency = context.get("currency");

                    changeBalance(sender, account, amount, currency, true, true);
                })
                .build();

        Command<CommandSourceStack> set = builder
                .literal("set")
                .required("account", AccountParser.accountParser())
                .required("amount", AmountParser.amountParser())
                .required("currency", CurrencyParser.currencyParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Account account = context.get("account");
                    double amount = context.get("amount");
                    Currency currency = context.get("currency");

                    setBalance(sender, account, amount, currency);
                })
                .build();

        Command<CommandSourceStack> cached = builder
                .literal("cached")
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Collection<Account> cachedAccounts = EconomyPlugin.getInstance().getAccountManager().getCachedAccounts();
                    for (Account account : cachedAccounts) {
                        sender.sendMessage("Account: " + account.getDisplayName());
                    }
                    sender.sendMessage("Total cached: " + cachedAccounts.size());
                })
                .build();

        Command<CommandSourceStack> flush = builder
                .literal("flush")
                .handler(context -> {
                    EconomyPlugin.getInstance().getAccountManager().flushAccounts();
                    EconomyPlugin.getInstance().getBalanceTopRepository().flushLists();
                    context.sender().getSender().sendMessage("All caches flushed!");
                })
                .build();

        Command<CommandSourceStack> debug = builder
                .literal("debug")
                .permission("economy.command.debug")
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    EconomyPlugin.getInstance().setDebug(!EconomyPlugin.getInstance().isDebug());
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_debug_status")
                            .replaceText(STATUS_REPLACEMENT.apply(EconomyPlugin.getInstance().isDebug()))
                    );
                })
                .build();

        this.manager.register(List.of(
                give,
                take,
                set,
                cached,
                flush,
                debug
        ));
    }

    private void changeBalance(CommandSender sender, Account account, double amount, Currency currency, boolean withdraw, boolean silent) {
        if (withdraw) {
            if (account.withdraw(currency, amount)) {
                EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                        .component(sender, "msg_eco_taken")
                        .replaceText(AMOUNT_REPLACEMENT.apply(currency, amount))
                        .replaceText(ACCOUNT_REPLACEMENT.apply(account))
                );
            } else {
                EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                        .component(sender, "err_player_insufficient_funds")
                        .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                        .replaceText(ACCOUNT_REPLACEMENT.apply(account))
                );
            }
        } else {
            if (account.deposit(currency, amount)) {
                EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                        .component(sender, "msg_eco_added")
                        .replaceText(AMOUNT_REPLACEMENT.apply(currency, amount))
                        .replaceText(ACCOUNT_REPLACEMENT.apply(account))
                );
                @Nullable Player target = Bukkit.getPlayer(account.getUuid());
                if (target != null && !silent) { // Send message if target player is online
                    EconomyPlugin.lang().sendComponent(target, EconomyPlugin.lang()
                            .component(target, "msg_received_currency")
                            .replaceText(AMOUNT_REPLACEMENT.apply(currency, amount))
                            .replaceText(config -> config
                                    .matchLiteral("{account}")
                                    .replacement(EconomyPlugin.lang().legacy(target, "msg_console_name"))
                            ));
                }
            }
        }
    }

    private void setBalance(CommandSender sender, Account account, double amount, Currency currency) {
        account.setBalance(currency, amount);
        EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                .component(sender, "msg_eco_set")
                .replaceText(AMOUNT_REPLACEMENT.apply(currency, amount))
                .replaceText(ACCOUNT_REPLACEMENT.apply(account))
        );
    }

}

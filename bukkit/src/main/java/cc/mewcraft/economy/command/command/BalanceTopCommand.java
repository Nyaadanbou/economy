package cc.mewcraft.economy.command.command;

import cc.mewcraft.economy.EconomyPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.lucko.helper.promise.Promise;
import cc.mewcraft.economy.api.Currency;
import cc.mewcraft.economy.command.AbstractCommand;
import cc.mewcraft.economy.command.CommandManager;
import cc.mewcraft.economy.command.argument.CurrencyParser;
import cc.mewcraft.economy.currency.BalanceTop;
import cc.mewcraft.economy.data.TransientBalance;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;
import org.incendo.cloud.parser.standard.IntegerParser;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static cc.mewcraft.economy.EconomyMessages.AMOUNT_REPLACEMENT;
import static cc.mewcraft.economy.EconomyMessages.CURRENCY_REPLACEMENT;

@SuppressWarnings("UnstableApiUsage")
public class BalanceTopCommand extends AbstractCommand {

    public BalanceTopCommand(EconomyPlugin plugin, CommandManager manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command<CommandSourceStack> balanceTop = this.manager.getCommandManager()
                .commandBuilder("baltop")
                .permission("economy.command.baltop")
                .optional("currency", CurrencyParser.currencyParser())
                .optional("page", IntegerParser.integerParser(1))
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Currency currency = context.getOrDefault("currency", EconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency());
                    int page = context.getOrDefault("page", 1);

                    if (!EconomyPlugin.getInstance().getDataStore().isTopSupported()) {
                        EconomyPlugin.lang().sendComponent(sender, "err_balance_top_no_support");
                        return;
                    }
                    if (!sender.hasPermission("economy.command.baltop." + currency.getName())) {
                        EconomyPlugin.lang().sendComponent(sender, "err_balance_top_no_permission");
                        return;
                    }

                    Promise<BalanceTop> promise = this.plugin.getBalanceTopRepository().computeByCurrency(currency);
                    if (promise.isDone()) { // it's completed - send the top list
                        sendTopList(sender, currency, promise.join(), page);
                    } else { // tell sender we're still computing it
                        EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang().component(sender, "msg_balance_top_computing"));
                        promise.thenAcceptSync(topList -> sendTopList(sender, currency, topList, page));
                    }
                })
                .build();

        this.manager.register(List.of(balanceTop));
    }

    /**
     * @param sender     (omitted)
     * @param currency   (omitted)
     * @param balanceTop (omitted)
     * @param page       page index starting from 1
     */
    private static void sendTopList(final CommandSender sender, final Currency currency, final BalanceTop balanceTop, final int page) {
        // in case the user input a non-existent page number
        final int pageBounded = Math.min(page, balanceTop.getMaxPage());

        // send list header
        EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                .component(sender, "msg_balance_top_header")
                .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                .replaceText(config -> config.matchLiteral("{page}").replacement(Integer.toString(pageBounded)))
        );

        // send list entries
        AtomicInteger index = new AtomicInteger(1 + (pageBounded - 1) * BalanceTop.ENTRY_PER_PAGE);
        List<TransientBalance> resultsAt = balanceTop.getResultsAt(pageBounded - 1);
        for (final TransientBalance entry : resultsAt) {
            EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                    .component(sender, "msg_balance_top_entry")
                    .replaceText(AMOUNT_REPLACEMENT.apply(currency, entry.amount()))
                    .replaceText(config -> config.matchLiteral("{account}").replacement(entry.name()))
                    .replaceText(config -> config.matchLiteral("{index}").replacement(String.valueOf(index.getAndIncrement())))
            );
        }

        // send last line
        if (resultsAt.isEmpty()) {
            EconomyPlugin.lang().sendComponent(sender, "err_balance_top_empty");
        } else {
            EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                    .component(sender, "msg_balance_top_next")
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                    .replaceText(config -> config.matchLiteral("{page}").replacement(String.valueOf(pageBounded + 1)))
            );
        }

        // send last update
        EconomyPlugin.lang().sendComponent(sender, "msg_balance_top_last_update", "time", balanceTop.getLastUpdate());
    }

}

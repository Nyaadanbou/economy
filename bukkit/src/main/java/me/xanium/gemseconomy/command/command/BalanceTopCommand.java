package me.xanium.gemseconomy.command.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import me.lucko.helper.promise.Promise;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.command.AbstractCommand;
import me.xanium.gemseconomy.command.CommandManager;
import me.xanium.gemseconomy.command.argument.CurrencyArgument;
import me.xanium.gemseconomy.currency.BalanceTop;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.data.TransientBalance;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static me.xanium.gemseconomy.GemsMessages.AMOUNT_REPLACEMENT;
import static me.xanium.gemseconomy.GemsMessages.CURRENCY_REPLACEMENT;

public class BalanceTopCommand extends AbstractCommand {

    public BalanceTopCommand(GemsEconomy plugin, CommandManager manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command<CommandSender> balanceTop = this.manager
            .commandBuilder("balancetop", "baltop")
            .permission("gemseconomy.command.baltop")
            .argument(CurrencyArgument.optional("currency"))
            .argument(IntegerArgument.<CommandSender>builder("page").withMin(1).asOptional())
            .handler(context -> {
                CommandSender sender = context.getSender();
                Currency currency = context.getOrDefault("currency", GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency());
                int page = context.getOrDefault("page", 1);

                if (!GemsEconomy.getInstance().getDataStore().isTopSupported()) {
                    GemsEconomy.lang().sendComponent(sender, "err_balance_top_no_support");
                    return;
                }
                if (!sender.hasPermission("gemseconomy.command.baltop." + currency.getName())) {
                    GemsEconomy.lang().sendComponent(sender, "err_balance_top_no_permission");
                    return;
                }

                Promise<BalanceTop> promise = this.plugin.getBalanceTopRepository().computeByCurrency(currency);
                if (promise.isDone()) { // it's completed - send the top list
                    sendTopList(sender, currency, promise.join(), page);
                } else { // tell sender we're still computing it
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang().component(sender, "msg_balance_top_computing"));
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
        GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
            .component(sender, "msg_balance_top_header")
            .replaceText(CURRENCY_REPLACEMENT.apply(currency))
            .replaceText(config -> config.matchLiteral("{page}").replacement(Integer.toString(pageBounded)))
        );

        // send list entries
        AtomicInteger index = new AtomicInteger(1 + (pageBounded - 1) * BalanceTop.ENTRY_PER_PAGE);
        List<TransientBalance> resultsAt = balanceTop.getResultsAt(pageBounded - 1);
        for (final TransientBalance entry : resultsAt) {
            GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                .component(sender, "msg_balance_top_entry")
                .replaceText(AMOUNT_REPLACEMENT.apply(currency, entry.amount()))
                .replaceText(config -> config.matchLiteral("{account}").replacement(entry.name()))
                .replaceText(config -> config.matchLiteral("{index}").replacement(String.valueOf(index.getAndIncrement())))
            );
        }

        // send last line
        if (resultsAt.isEmpty()) {
            GemsEconomy.lang().sendComponent(sender, "err_balance_top_empty");
        } else {
            GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                .component(sender, "msg_balance_top_next")
                .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                .replaceText(config -> config.matchLiteral("{page}").replacement(String.valueOf(pageBounded + 1)))
            );
        }

        // send last update
        GemsEconomy.lang().sendComponent(sender, "msg_balance_top_last_update", "time", balanceTop.getLastUpdate());
    }

}

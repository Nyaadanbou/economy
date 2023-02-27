package me.xanium.gemseconomy.command.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.command.AbstractCommand;
import me.xanium.gemseconomy.command.CommandManager;
import me.xanium.gemseconomy.command.argument.CurrencyArgument;
import me.xanium.gemseconomy.currency.CachedTopListEntry;
import me.xanium.gemseconomy.currency.Currency;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static me.xanium.gemseconomy.GemsMessages.AMOUNT_REPLACEMENT;
import static me.xanium.gemseconomy.GemsMessages.CURRENCY_REPLACEMENT;

public class BalanceTopCommand extends AbstractCommand {

    private static final int ACCOUNTS_PER_PAGE = 10;

    public BalanceTopCommand(GemsEconomy plugin, CommandManager manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command<CommandSender> balanceTop = this.manager
            .commandBuilder("balancetop", "baltop")
            .permission("gemseconomy.command.baltop")
            .argument(CurrencyArgument.optional("currency"))
            .argument(IntegerArgument.optional("page"))
            .handler(context -> {
                CommandSender sender = context.getSender();
                Currency currency = context.getOrDefault("currency", GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency());
                if (!GemsEconomy.getInstance().getDataStore().isTopSupported()) {
                    GemsEconomy.lang().sendComponent(sender, "err_balance_top_no_support");
                    return;
                }
                if (!sender.hasPermission("gemseconomy.command.baltop." + currency.getName())) {
                    GemsEconomy.lang().sendComponent(sender, "err_balance_top_no_permission");
                    return;
                }
                int page = Math.min(Math.max(context.getOrDefault("page", 1), 1), 100);
                int offset = 10 * (page - 1);
                sendBalanceTop(sender, currency, offset, page);
            })
            .build();

        this.manager.register(List.of(balanceTop));
    }

    private void sendBalanceTop(CommandSender sender, Currency currency, int offset, int pageNum) {
        GemsEconomy.getInstance().getDataStore().getTopList(currency, offset, ACCOUNTS_PER_PAGE).thenAcceptSync(result -> {

            GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                .component(sender, "msg_balance_top_header")
                .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                .replaceText(config -> config.matchLiteral("{page}").replacement(Integer.toString(pageNum)))
            );

            final AtomicInteger index = new AtomicInteger((10 * (pageNum - 1)) + 1);
            for (CachedTopListEntry entry : result) {
                double balance = entry.getAmount();
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_balance_top_entry")
                    .replaceText(AMOUNT_REPLACEMENT.apply(currency, balance))
                    .replaceText(config -> config.matchLiteral("{account}").replacement(entry.getName()))
                    .replaceText(config -> config.matchLiteral("{index}").replacement(index.toString()))
                );
                index.incrementAndGet();
            }

            if (result.isEmpty()) {
                GemsEconomy.lang().sendComponent(sender, "err_balance_top_empty");
            } else {
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_balance_top_next")
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency))
                    .replaceText(config -> config.matchLiteral("{page}").replacement(Integer.toString(pageNum + 1)))
                );
            }

        });
    }

}

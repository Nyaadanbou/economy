package me.xanium.gemseconomy.command.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.standard.IntegerArgument;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.GemsMessages;
import me.xanium.gemseconomy.command.GemsCommand;
import me.xanium.gemseconomy.command.GemsCommands;
import me.xanium.gemseconomy.command.argument.CurrencyArgument;
import me.xanium.gemseconomy.currency.CachedTopListEntry;
import me.xanium.gemseconomy.currency.Currency;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BalanceTopCommand extends GemsCommand {

    private static final int ACCOUNTS_PER_PAGE = 10;

    public BalanceTopCommand(GemsEconomy plugin, GemsCommands manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command<CommandSender> balanceTop = manager
                .commandBuilder("balancetop", "baltop")
                .permission("gemseconomy.command.baltop")
                .argument(CurrencyArgument.optional("currency"))
                .argument(IntegerArgument.optional("page"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Currency currency = context.getOrDefault("currency", GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency());
                    if (currency == null) { // No default currency is set
                        GemsEconomy.lang().sendComponent(sender, "err_no_default_currency");
                        return;
                    }
                    if (!GemsEconomy.getInstance().getDataStore().isTopSupported()) {
                        GemsEconomy.lang().sendComponent(sender, "err_balance_top_no_support");
                        return;
                    }
                    if (!sender.hasPermission("gemseconomy.command.baltop." + currency.getSingular())) {
                        GemsEconomy.lang().sendComponent(sender, "err_balance_top_no_permission");
                        return;
                    }
                    @SuppressWarnings("ConstantConditions")
                    int page = Math.min(Math.max(context.getOrDefault("page", 1), 1), 100);
                    int offset = 10 * (page - 1);
                    sendBalanceTop(sender, currency, offset, page);
                })
                .build();

        manager.register(List.of(balanceTop));
    }

    @NonnullByDefault
    private void sendBalanceTop(CommandSender sender, Currency currency, int offset, int pageNum) {
        GemsEconomy.getInstance().getDataStore().getTopList(currency, offset, ACCOUNTS_PER_PAGE, cachedTopListEntries -> {

            GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                    .component(sender, "msg_balance_top_header")
                    .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                    .replaceText(config -> config.matchLiteral("{page}").replacement(Integer.toString(pageNum)))
            );

            final AtomicInteger index = new AtomicInteger((10 * (pageNum - 1)) + 1);
            for (CachedTopListEntry entry : cachedTopListEntries) {
                double balance = entry.getAmount();
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                        .component(sender, "msg_balance_top_entry")
                        .replaceText(GemsMessages.AMOUNT_REPLACEMENT.apply(currency, balance))
                        .replaceText(GemsMessages.ACCOUNT_REPLACEMENT.apply(entry.getName()))
                        .replaceText(config -> config.matchLiteral("{index}").replacement(index.toString()))
                );
                index.incrementAndGet();
            }
            if (cachedTopListEntries.isEmpty()) {
                GemsEconomy.lang().sendComponent(sender, "err_balance_top_empty");
            } else {
                GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                        .component(sender, "msg_balance_top_next")
                        .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
                        .replaceText(config -> config.matchLiteral("{page}").replacement(Integer.toString(pageNum + 1)))
                );
            }

        });
    }

}

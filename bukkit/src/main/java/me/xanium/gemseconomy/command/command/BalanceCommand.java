package me.xanium.gemseconomy.command.command;

import cloud.commandframework.Command;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.command.AbstractCommand;
import me.xanium.gemseconomy.command.CommandManager;
import me.xanium.gemseconomy.command.argument.AccountArgument;
import me.xanium.gemseconomy.currency.Currency;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import java.util.List;
import java.util.Optional;

import static me.xanium.gemseconomy.GemsMessages.ACCOUNT_REPLACEMENT;
import static me.xanium.gemseconomy.GemsMessages.AMOUNT_REPLACEMENT;

@DefaultQualifier(NonNull.class)
public class BalanceCommand extends AbstractCommand {

    public BalanceCommand(GemsEconomy plugin, CommandManager manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command<CommandSender> balance = this.manager
            .commandBuilder("balance", "bal", "money")
            .permission("gemseconomy.command.balance")
            .argument(AccountArgument.optional("account"))
            .handler(context -> {
                CommandSender sender = context.getSender();
                Optional<Account> account = context.getOptional("account");
                if (sender instanceof Player player) {
                    if (account.isEmpty()) { // Player did not specify account, so view the account of their own
                        @Nullable Account ownAccount = GemsEconomy.getInstance().getAccountManager().fetchAccount(player);
                        if (ownAccount == null) { // Double check in case the player's account is not loaded for some reason
                            GemsEconomy.lang().sendComponent(sender, "err_account_missing");
                            return;
                        }
                        sendBalance(player, ownAccount);
                    } else if (account.get().getNickname().equalsIgnoreCase(sender.getName())) { // Player specified his own name
                        sendBalance(player, account.get());
                    } else if (sender.hasPermission("gemseconomy.command.balance.other")) { // Player specified an account, so view other's account
                        sendBalance(player, account.get());
                    } else {
                        GemsEconomy.lang().sendComponent(sender, "err_no_permission", "permission", "gemseconomy.command.balance.other");
                    }
                } else { // It is a ConsoleSender (usually)
                    if (account.isEmpty()) { // Console must specify an account
                        GemsEconomy.lang().sendComponent(sender, "err_player_is_null");
                    } else {
                        sendBalance(sender, account.get());
                    }
                }
            })
            .build();

        this.manager.register(List.of(balance));
    }

    private void sendBalance(CommandSender sender, Account account) {
        int currencies = GemsEconomy.getInstance().getCurrencyManager().getCurrencies().size();
        if (currencies == 0) {
            GemsEconomy.lang().sendComponent(sender, "err_no_default_currency");
        } else if (currencies == 1) {
            Currency currency = GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency();
            double balance = account.getBalance(currency);
            GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                .component(sender, "msg_balance_current")
                .replaceText(ACCOUNT_REPLACEMENT.apply(account))
                .replaceText(AMOUNT_REPLACEMENT.apply(currency, balance))
            );
        } else {
            GemsEconomy.lang().sendComponent(sender, "msg_balance_multiple", "account", account.getNickname());
            account.getBalances().forEach((currency, balance) -> {
                if (sender.hasPermission("gemseconomy.currency.balance." + currency.getName())) {
                    GemsEconomy.lang().sendComponent(sender, GemsEconomy.lang()
                        .component(sender, "msg_balance_list")
                        .replaceText(AMOUNT_REPLACEMENT.apply(currency, balance))
                    );
                }
            });
        }
    }

}

package me.xanium.gemseconomy.command.command;

import cloud.commandframework.Command;
import me.xanium.gemseconomy.GemsEconomyPlugin;
import me.xanium.gemseconomy.api.Account;
import me.xanium.gemseconomy.api.Currency;
import me.xanium.gemseconomy.command.AbstractCommand;
import me.xanium.gemseconomy.command.CommandManager;
import me.xanium.gemseconomy.command.argument.AccountArgument;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;

import static me.xanium.gemseconomy.GemsMessages.ACCOUNT_REPLACEMENT;
import static me.xanium.gemseconomy.GemsMessages.AMOUNT_REPLACEMENT;

@DefaultQualifier(NonNull.class)
public class BalanceAccCommand extends AbstractCommand {

    public BalanceAccCommand(GemsEconomyPlugin plugin, CommandManager manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command<CommandSender> balance = this.manager
                .commandBuilder("balanceacc", "balacc")
                .permission("gemseconomy.command.balanceacc")
                .argument(AccountArgument.optional("account"))
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    Optional<Account> account = context.getOptional("account");
                    if (sender instanceof Player player) {
                        if (account.isEmpty()) { // Player did not specify account, so view the account of their own
                            @Nullable Account ownAccount = GemsEconomyPlugin.getInstance().getAccountManager().fetchAccount(player);
                            if (ownAccount == null) { // Double check in case the player's account is not loaded for some reason
                                GemsEconomyPlugin.lang().sendComponent(sender, "err_account_missing");
                                return;
                            }
                            sendBalance(player, ownAccount);
                        } else if (account.get().getNickname().equalsIgnoreCase(sender.getName())) { // Player specified his own name
                            sendBalance(player, account.get());
                        } else if (sender.hasPermission("gemseconomy.command.balanceacc.other")) { // Player specified an account, so view other's account
                            sendBalance(player, account.get());
                        } else {
                            GemsEconomyPlugin.lang().sendComponent(sender, "err_no_permission", "permission", "gemseconomy.command.balanceacc.other");
                        }
                    } else { // It is a ConsoleSender (usually)
                        if (account.isEmpty()) { // Console must specify an account
                            GemsEconomyPlugin.lang().sendComponent(sender, "err_player_is_null");
                        } else {
                            sendBalance(sender, account.get());
                        }
                    }
                })
                .build();

        this.manager.register(List.of(balance));
    }

    private void sendBalance(CommandSender sender, Account account) {
        int currencies = GemsEconomyPlugin.getInstance().getCurrencyManager().getLoadedCurrencies().size();
        if (currencies == 0) {
            GemsEconomyPlugin.lang().sendComponent(sender, "err_no_default_currency");
        } else if (currencies == 1) {
            Currency currency = GemsEconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency();
            double balance = account.getHeapBalance(currency);
            GemsEconomyPlugin.lang().sendComponent(sender, GemsEconomyPlugin.lang()
                    .component(sender, "msg_balance_acc_current")
                    .replaceText(ACCOUNT_REPLACEMENT.apply(account))
                    .replaceText(AMOUNT_REPLACEMENT.apply(currency, balance))
            );
        } else {
            GemsEconomyPlugin.lang().sendComponent(sender, "msg_balance_acc_multiple", "account", account.getNickname());
            account.getHeapBalances().forEach((currency, acc) -> {
                if (sender.hasPermission("gemseconomy.currency.balanceacc." + currency.getName())) {
                    double balance = account.getHeapBalance(currency);
                    GemsEconomyPlugin.lang().sendComponent(sender, GemsEconomyPlugin.lang()
                            .component(sender, "msg_balance_list")
                            .replaceText(AMOUNT_REPLACEMENT.apply(currency, balance))
                    );
                }
            });
        }
    }

}

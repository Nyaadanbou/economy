package cc.mewcraft.economy.command.command;

import cc.mewcraft.economy.EconomyPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import cc.mewcraft.economy.api.Account;
import cc.mewcraft.economy.api.Currency;
import cc.mewcraft.economy.command.AbstractCommand;
import cc.mewcraft.economy.command.CommandManager;
import cc.mewcraft.economy.command.argument.AccountParser;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.framework.qual.DefaultQualifier;
import org.incendo.cloud.Command;

import static cc.mewcraft.economy.EconomyMessages.ACCOUNT_REPLACEMENT;
import static cc.mewcraft.economy.EconomyMessages.AMOUNT_REPLACEMENT;

@SuppressWarnings("UnstableApiUsage")
@DefaultQualifier(NonNull.class)
public class BalanceCommand extends AbstractCommand {

    public BalanceCommand(EconomyPlugin plugin, CommandManager manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command<CommandSourceStack> balance = this.manager.getCommandManager()
                .commandBuilder("balance", "bal", "money")
                .permission("economy.command.balance")
                .optional("account", AccountParser.accountParser())
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    Optional<Account> account = context.optional("account");
                    if (sender instanceof Player player) {
                        if (account.isEmpty()) { // Player did not specify account, so view the account of their own
                            @Nullable Account ownAccount = EconomyPlugin.getInstance().getAccountManager().fetchAccount(player);
                            if (ownAccount == null) { // Double check in case the player's account is not loaded for some reason
                                EconomyPlugin.lang().sendComponent(sender, "err_account_missing");
                                return;
                            }
                            sendBalance(player, ownAccount);
                        } else if (account.get().getNickname().equalsIgnoreCase(sender.getName())) { // Player specified his own name
                            sendBalance(player, account.get());
                        } else if (sender.hasPermission("economy.command.balance.other")) { // Player specified an account, so view other's account
                            sendBalance(player, account.get());
                        } else {
                            EconomyPlugin.lang().sendComponent(sender, "err_no_permission", "permission", "economy.command.balance.other");
                        }
                    } else { // It is a ConsoleSender (usually)
                        if (account.isEmpty()) { // Console must specify an account
                            EconomyPlugin.lang().sendComponent(sender, "err_player_is_null");
                        } else {
                            sendBalance(sender, account.get());
                        }
                    }
                })
                .build();

        this.manager.register(List.of(balance));
    }

    private void sendBalance(CommandSender sender, Account account) {
        int currencies = EconomyPlugin.getInstance().getCurrencyManager().getLoadedCurrencies().size();
        if (currencies == 0) {
            EconomyPlugin.lang().sendComponent(sender, "err_no_default_currency");
        } else if (currencies == 1) {
            Currency currency = EconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency();
            double balance = account.getBalance(currency);
            EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                    .component(sender, "msg_balance_current")
                    .replaceText(ACCOUNT_REPLACEMENT.apply(account))
                    .replaceText(AMOUNT_REPLACEMENT.apply(currency, balance))
            );
        } else {
            EconomyPlugin.lang().sendComponent(sender, "msg_balance_multiple", "account", account.getNickname());
            account.getBalances().forEach((currency, balance) -> {
                if (sender.hasPermission("economy.currency.balance." + currency.getName())) {
                    EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                            .component(sender, "msg_balance_list")
                            .replaceText(AMOUNT_REPLACEMENT.apply(currency, balance))
                    );
                }
            });
        }
    }

}

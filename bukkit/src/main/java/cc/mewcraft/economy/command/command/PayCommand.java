package cc.mewcraft.economy.command.command;

import cc.mewcraft.economy.EconomyPlugin;
import cc.mewcraft.economy.api.Account;
import cc.mewcraft.economy.api.Currency;
import cc.mewcraft.economy.command.AbstractCommand;
import cc.mewcraft.economy.command.CommandManager;
import cc.mewcraft.economy.command.argument.AmountParser;
import cc.mewcraft.economy.command.argument.CurrencyParser;
import cc.mewcraft.economy.event.EconomyPayEvent;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.entity.Player;
import org.incendo.cloud.Command;
import org.incendo.cloud.bukkit.data.MultiplePlayerSelector;
import org.incendo.cloud.bukkit.parser.selector.MultiplePlayerSelectorParser;

import java.util.List;

import static cc.mewcraft.economy.EconomyMessages.ACCOUNT_REPLACEMENT;
import static cc.mewcraft.economy.EconomyMessages.AMOUNT_REPLACEMENT;
import static cc.mewcraft.economy.EconomyMessages.CURRENCY_REPLACEMENT;

@SuppressWarnings("UnstableApiUsage")
public class PayCommand extends AbstractCommand {

    public PayCommand(EconomyPlugin plugin, CommandManager manager) {
        super(plugin, manager);
    }

    public void register() {
        Command<CommandSourceStack> pay = this.manager.getCommandManager()
                .commandBuilder("pay")
                .permission("economy.command.pay")
                .required("player", MultiplePlayerSelectorParser.multiplePlayerSelectorParser())
                .required("amount", AmountParser.amountParser())
                .required("currency", CurrencyParser.currencyParser())
                .handler(context -> {
                    Player sender = (Player) context.sender().getSender();
                    MultiplePlayerSelector selector = context.get("player");
                    double amount = context.get("amount");
                    Currency currency = context.getOrDefault("currency", EconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency());
                    if (!selector.values().isEmpty()) {
                        selector.values().forEach(p -> pay(sender, p, amount, currency));
                    } else {
                        EconomyPlugin.lang().sendComponent(sender, "err_player_is_null");
                    }
                })
                .build();

        this.manager.register(List.of(pay));
    }

    private void pay(Player sender, Player targetPlayer, double amount, Currency currency) {
        if (!sender.hasPermission("economy.command.pay." + currency.getName().toLowerCase())) {
            EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                    .component(sender, "msg_pay_no_permission")
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency))
            );
            return;
        }

        if (!currency.isPayable()) {
            EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                    .component(sender, "msg_currency_is_not_payable")
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency))
            );
            return;
        }

        // Check target account
        Account targetAccount = EconomyPlugin.getInstance().getAccountManager().fetchAccount(targetPlayer);
        if (targetAccount == null) {
            EconomyPlugin.lang().sendComponent(sender, "err_player_is_null");
            return;
        }

        // Check if sender account missing
        Account myselfAccount = EconomyPlugin.getInstance().getAccountManager().fetchAccount(sender);
        if (myselfAccount == null) {
            EconomyPlugin.lang().sendComponent(sender, "err_account_missing");
            return;
        }

        // Check self pay
        if (targetAccount.getUuid().equals(myselfAccount.getUuid())) {
            EconomyPlugin.lang().sendComponent(sender, "err_cannot_pay_yourself");
            return;
        }

        // Check target receivable
        if (!targetAccount.canReceiveCurrency()) {
            EconomyPlugin.lang().sendComponent(sender, "err_cannot_receive_money", "account", targetAccount.getNickname());
            return;
        }

        // Check insufficient funds
        if (!myselfAccount.hasEnough(currency, amount)) {
            EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                    .component(sender, "err_insufficient_funds")
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency))
            );
            return;
        }

        // Check target balance overflow
        if (targetAccount.testOverflow(currency, amount)) {
            EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                    .component(sender, "msg_currency_overflow")
                    .replaceText(ACCOUNT_REPLACEMENT.apply(targetAccount))
                    .replaceText(CURRENCY_REPLACEMENT.apply(currency))
            );
            return;
        }

        EconomyPayEvent event = new EconomyPayEvent(currency, myselfAccount, targetAccount, amount);
        if (!event.callEvent()) return;

        myselfAccount.withdraw(currency, amount);
        targetAccount.deposit(currency, amount);

        EconomyPlugin.getInstance().getEconomyLogger().log(
                "[PAYMENT] " + myselfAccount.getDisplayName() +
                " (New bal: " + currency.simpleFormat(myselfAccount.getBalance(currency)) + ") -> paid " + targetAccount.getDisplayName() +
                " (New bal: " + currency.simpleFormat(targetAccount.getBalance(currency)) + ") - An amount of " + currency.simpleFormat(amount)
        );

        EconomyPlugin.lang().sendComponent(targetPlayer, EconomyPlugin.lang()
                .component(targetPlayer, "msg_received_currency")
                .replaceText(ACCOUNT_REPLACEMENT.apply(myselfAccount))
                .replaceText(AMOUNT_REPLACEMENT.apply(currency, amount))
        );
        EconomyPlugin.lang().sendComponent(sender, EconomyPlugin.lang()
                .component(sender, "msg_paid_currency")
                .replaceText(ACCOUNT_REPLACEMENT.apply(targetAccount))
                .replaceText(AMOUNT_REPLACEMENT.apply(currency, amount))
        );
    }

}

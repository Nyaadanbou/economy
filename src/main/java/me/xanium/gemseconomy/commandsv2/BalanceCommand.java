package me.xanium.gemseconomy.commandsv2;

import dev.jorel.commandapi.CommandAPICommand;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.commandsv2.argument.BaseArguments;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.file.F;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BalanceCommand {

    private static final String NAME = "balance";
    private static final String[] ALIASES = new String[]{"bal", "money"};
    private static final String PERM_MONEY = "gemseconomy.command.balance";
    private static final String PERM_MONEY_OTHER = "gemseconomy.command.balance.other";

    public BalanceCommand() {
        new CommandAPICommand(NAME)
                .withAliases(ALIASES)
                .withPermission(PERM_MONEY)
                .executesPlayer((sender, args) -> {
                    SchedulerUtils.runAsync(() -> sendBalance(sender, sender.getName()));
                })
                .register();
        new CommandAPICommand(NAME)
                .withAliases(ALIASES)
                .withPermission(PERM_MONEY)
                .withPermission(PERM_MONEY_OTHER)
                .withArguments(BaseArguments.ACCOUNT)
                .executes((sender, args) -> {
                    SchedulerUtils.runAsync(() -> sendBalance(sender, (String) args[0]));
                })
                .register();
    }

    private void sendBalance(@NotNull CommandSender sender, @Nullable String rawAccount) {
        Account account = GemsEconomy.inst().getAccountManager().getAccount(rawAccount);
        if (account != null) {
            int currencies = GemsEconomy.inst().getCurrencyManager().getCurrencies().size();
            if (currencies == 0) {
                sender.sendMessage(F.noDefaultCurrency());
            } else if (currencies == 1) {
                Currency currency = GemsEconomy.inst().getCurrencyManager().getDefaultCurrency();
                if (currency == null) {
                    sender.sendMessage(F.balanceNone().replace("{player}", account.getNickname()));
                    return;
                }
                double balance = account.getBalance(currency);
                sender.sendMessage(F.balance().replace("{player}", account.getDisplayName()).replace("{currencycolor}", "" + currency.getColor()).replace("{balance}", currency.format(balance)));
            } else {
                sender.sendMessage(F.balanceMultiple().replace("{player}", account.getDisplayName()));
                for (Currency currency : GemsEconomy.inst().getCurrencyManager().getCurrencies()) {
                    double balance = account.getBalance(currency);
                    sender.sendMessage(F.balanceList().replace("{currencycolor}", currency.getColor() + "").replace("{format}", currency.format(balance)));
                }
            }
        } else sender.sendMessage(F.playerDoesNotExist());
    }
}

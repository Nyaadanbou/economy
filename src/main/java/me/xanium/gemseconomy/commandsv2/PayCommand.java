package me.xanium.gemseconomy.commandsv2;

import dev.jorel.commandapi.CommandAPICommand;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.commandsv2.argument.BaseArguments;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.event.GemsPayEvent;
import me.xanium.gemseconomy.file.F;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import me.xanium.gemseconomy.utils.UtilMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class PayCommand {

    private static final String NAME = "pay";
    private static final String PERM_PAY = "gemseconomy.command.pay";

    public PayCommand() {
        new CommandAPICommand(NAME)
                .withPermission(PERM_PAY)
                .withArguments(BaseArguments.PLAYER)
                .withArguments(BaseArguments.AMOUNT)
                .executesPlayer((sender, args) -> {
                    SchedulerUtils.runAsync(() -> {
                        //noinspection unchecked
                        ((Collection<Player>) args[0]).forEach(p -> pay(sender, p, (String) args[1], null));
                    });
                })
                .register();
        new CommandAPICommand(NAME)
                .withPermission(PERM_PAY)
                .withArguments(BaseArguments.PLAYER)
                .withArguments(BaseArguments.AMOUNT)
                .withArguments(BaseArguments.CURRENCY)
                .executesPlayer((sender, args) -> {
                    SchedulerUtils.runAsync(() -> {
                        //noinspection unchecked
                        ((Collection<Player>) args[0]).forEach(p -> pay(sender, p, (String) args[1], (String) args[2]));
                    });
                })
                .register();
    }

    private void pay(Player sender, @NotNull Player rawTarget, @NotNull String rawAmount, @Nullable String rawCurrency) {
        if (GemsEconomy.inst().getCurrencyManager().getDefaultCurrency() == null) {
            sender.sendMessage(F.noDefaultCurrency());
            return;
        }

        // Check target
        Account target = GemsEconomy.inst().getAccountManager().getAccount(rawTarget);
        if (target == null) {
            sender.sendMessage(F.playerDoesNotExist());
            return;
        }

        // Check currency
        Currency currency = rawCurrency == null
                ? GemsEconomy.inst().getCurrencyManager().getDefaultCurrency()
                : GemsEconomy.inst().getCurrencyManager().getCurrency(rawCurrency);
        if (currency == null) {
            sender.sendMessage(F.unknownCurrency());
            return;
        }
        if (!sender.hasPermission("gemseconomy.command.pay." + currency.getPlural().toLowerCase()) && !sender.hasPermission("gemseconomy.command.pay." + currency.getSingular().toLowerCase())) {
            sender.sendMessage(F.payNoPerms()
                    .replace("{currencycolor}", "" + currency.getColor())
                    .replace("{currency}", currency.getPlural()));
            return;
        }
        if (!currency.isPayable()) {
            sender.sendMessage(F.currencyNotPayable()
                    .replace("{currencycolor}", "" + currency.getColor())
                    .replace("{currency}", currency.getPlural()));
            return;
        }

        // Check amount
        Optional<Double> amountOpt = parseAmount(sender, rawAmount, currency);
        if (amountOpt.isEmpty()) return;
        double amount = amountOpt.get();

        // Check this account missing
        Account account = GemsEconomy.inst().getAccountManager().getAccount(sender);
        if (account == null) {
            sender.sendMessage(F.accountMissing());
            return;
        }

        // Check self pay
        if (target.getUuid().equals(account.getUuid())) {
            sender.sendMessage(F.payYourself());
            return;
        }

        // Check target receivable
        if (!target.canReceiveCurrency()) {
            sender.sendMessage(F.cannotReceive().replace("{player}", target.getDisplayName()));
            return;
        }

        // Check insufficient funds
        if (!account.hasEnough(currency, amount)) {
            sender.sendMessage(F.insufficientFunds().replace("{currencycolor}", "" + currency.getColor()).replace("{currency}", currency.getPlural()));
            return;
        }

        // Check target balance overflow
        if (target.isOverflow(currency, amount)) {
            sender.sendMessage(F.currencyOverflow()
                    .replace("{player}", target.getDisplayName())
                    .replace("{currencycolor}", "" + currency.getColor())
                    .replace("{currency}", currency.getPlural()));
            return;
        }

        GemsPayEvent event = new GemsPayEvent(currency, account, target, amount);
        SchedulerUtils.run(() -> Bukkit.getPluginManager().callEvent(event));
        if (event.isCancelled()) return;

        double accBal = account.getBalance(currency) - amount;
        double tarBal = target.getBalance(currency) + amount;

        // cap the amount
        double cappedAccBal = Math.min(accBal, currency.getMaxBalance());
        double cappedTarBal = Math.min(tarBal, currency.getMaxBalance());

        account.modifyBalance(currency, cappedAccBal, true);
        target.modifyBalance(currency, cappedTarBal, true);
        GemsEconomy.inst().getEconomyLogger().log("[PAYMENT] " + account.getDisplayName() + " (New bal: " + currency.format(cappedAccBal) + ") -> paid " + target.getDisplayName() + " (New bal: " + currency.format(cappedTarBal) + ") - An amount of " + currency.format(amount));

        UtilMessage.sendMessageToAccount(target.getUuid(), F.paidMessage()
                .replace("{currencycolor}", currency.getColor() + "")
                .replace("{amount}", currency.format(amount))
                .replace("{player}", sender.getName()));
        sender.sendMessage(F.payerMessage()
                .replace("{currencycolor}", currency.getColor() + "")
                .replace("{amount}", currency.format(amount))
                .replace("{player}", target.getDisplayName()));
    }

    private Optional<Double> parseAmount(@NotNull CommandSender sender, @NotNull String amount, @NotNull Currency currency) {
        double amountTmp;
        if (currency.isDecimalSupported()) {
            try {
                amountTmp = Double.parseDouble(amount);
                if (amountTmp <= 0.0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                sender.sendMessage(F.invalidAmount());
                return Optional.empty();
            }
        } else {
            try {
                amountTmp = Integer.parseInt(amount);
                if (amountTmp <= 0.0) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException ex) {
                sender.sendMessage(F.invalidAmount());
                return Optional.empty();
            }
        }
        return Optional.of(amountTmp);
    }
}

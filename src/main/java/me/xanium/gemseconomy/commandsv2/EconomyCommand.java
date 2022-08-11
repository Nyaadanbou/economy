package me.xanium.gemseconomy.commandsv2;

import dev.jorel.commandapi.CommandAPICommand;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.commandsv2.argument.BaseArguments;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.file.F;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import me.xanium.gemseconomy.utils.UtilMessage;
import me.xanium.gemseconomy.utils.UtilServer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Optional;

public class EconomyCommand {

    private static final String NAME = "economy";
    private static final String[] ALIASES = new String[]{"eco"};
    private static final String PERM_ECONOMY = "gemseconomy.command.economy";

    public EconomyCommand() {
        new CommandAPICommand(NAME)
                .withAliases(ALIASES)
                .withPermission(PERM_ECONOMY)
                .withSubcommand(new CommandAPICommand("give")
                        .withArguments(BaseArguments.ACCOUNT)
                        .withArguments(BaseArguments.AMOUNT)
                        .withArguments(BaseArguments.CURRENCY)
                        .executes((sender, args) -> {
                            changeBalance(sender, args[0].toString(), args[1].toString(), args[2].toString(), false);
                        }))
                .withSubcommand(new CommandAPICommand("giveMany")
                        .withArguments(BaseArguments.PLAYER)
                        .withArguments(BaseArguments.AMOUNT)
                        .withArguments(BaseArguments.CURRENCY)
                        .executesConsole((sender, args) -> {
                            //noinspection unchecked
                            for (Player p : ((Collection<Player>) args[0])) {
                                changeBalance(sender, p.getName(), args[1].toString(), args[2].toString(), false);
                            }
                        }))
                .withSubcommand(new CommandAPICommand("take")
                        .withArguments(BaseArguments.ACCOUNT)
                        .withArguments(BaseArguments.AMOUNT)
                        .withArguments(BaseArguments.CURRENCY)
                        .executes((sender, args) -> {
                            changeBalance(sender, args[0].toString(), args[1].toString(), args[2].toString(), true);
                        }))
                .withSubcommand(new CommandAPICommand("takeMany")
                        .withArguments(BaseArguments.PLAYER)
                        .withArguments(BaseArguments.AMOUNT)
                        .withArguments(BaseArguments.CURRENCY)
                        .executesConsole((sender, args) -> {
                            //noinspection unchecked
                            for (Player p : ((Collection<Player>) args[0])) {
                                changeBalance(sender, p.getName(), args[1].toString(), args[2].toString(), true);
                            }
                        }))
                .withSubcommand(new CommandAPICommand("set")
                        .withArguments(BaseArguments.ACCOUNT)
                        .withArguments(BaseArguments.AMOUNT)
                        .withArguments(BaseArguments.CURRENCY)
                        .executes((sender, args) -> {
                            setBalance(sender, args[0].toString(), args[1].toString(), args[2].toString());
                        }))
                .withSubcommand(new CommandAPICommand("setMany")
                        .withArguments(BaseArguments.PLAYER)
                        .withArguments(BaseArguments.AMOUNT)
                        .withArguments(BaseArguments.CURRENCY)
                        .executesConsole((sender, args) -> {
                            //noinspection unchecked
                            for (Player p : ((Collection<Player>) args[0])) {
                                setBalance(sender, p.getName(), args[1].toString(), args[2].toString());
                            }
                        }))
                .withSubcommand(new CommandAPICommand("cache")
                        .executesConsole((sender, args) -> {
                            SchedulerUtils.runAsync(() -> {
                                for (Account a : GemsEconomy.inst().getAccountManager().getAccounts()) {
                                    UtilServer.consoleLog("Account: " + a.getNickname() + " cached");
                                }
                            });
                        }))
                .register();
    }

    private void changeBalance(@NotNull CommandSender sender, @NotNull String rawAccount, @NotNull String rawAmount, @Nullable String rawCurrency, boolean withdraw) {
        SchedulerUtils.runAsync(() -> {

            // Check account
            Account target = GemsEconomy.inst().getAccountManager().getAccount(rawAccount);
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

            // Check amount
            Optional<Double> amountOpt = parseAmount(sender, rawAmount, currency);
            if (amountOpt.isEmpty()) {
                return;
            }
            double amount = amountOpt.get();

            // Arguments all good, deposit/withdraw the balance
            if (withdraw) {
                if (target.withdraw(currency, amount)) {
                    sender.sendMessage(F.takeMessage()
                            .replace("{player}", target.getNickname())
                            .replace("{currencycolor}", currency.getColor() + "")
                            .replace("{amount}", currency.format(amount)));
                } else {
                    sender.sendMessage(F.targetInsufficientFunds()
                            .replace("{currencycolor}", currency.getColor() + "")
                            .replace("{currency}", currency.getPlural())
                            .replace("{target}", target.getDisplayName()));
                }
            } else {
                if (target.deposit(currency, amount)) {
                    sender.sendMessage(F.addMessage()
                            .replace("{player}", target.getNickname())
                            .replace("{currencycolor}", currency.getColor() + "")
                            .replace("{amount}", currency.format(amount)));
                    UtilMessage.sendMessageToAccount(target.getUuid(), F.paidMessage()
                            .replace("{currencycolor}", currency.getColor() + "")
                            .replace("{amount}", currency.format(amount))
                            .replace("{player}", F.consoleName()));
                }
            }
        });
    }

    private void setBalance(@NotNull CommandSender sender, @NotNull String rawAccount, @NotNull String rawAmount, @Nullable String rawCurrency) {
        SchedulerUtils.runAsync(() -> {

            // Check account
            Account target = GemsEconomy.inst().getAccountManager().getAccount(rawAccount);
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

            // Check amount
            Optional<Double> amountOpt = parseAmount(sender, rawAmount, currency);
            if (amountOpt.isEmpty()) {
                return;
            }
            double amount = amountOpt.get();

            // Arguments all good, set the balance
            target.setBalance(currency, amount);
            sender.sendMessage(F.setMessage()
                    .replace("{player}", target.getNickname())
                    .replace("{currencycolor}", currency.getColor() + "")
                    .replace("{amount}", currency.format(amount)));
        });
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

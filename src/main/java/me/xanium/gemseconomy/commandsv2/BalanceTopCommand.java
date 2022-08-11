package me.xanium.gemseconomy.commandsv2;

import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.IntegerArgument;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.commandsv2.argument.BaseArguments;
import me.xanium.gemseconomy.currency.CachedTopListEntry;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.file.F;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class BalanceTopCommand {

    private static final int ACCOUNTS_PER_PAGE = 10;

    private static final String NAME = "balancetop";
    private static final String[] ALIASES = new String[]{"baltop"};
    private static final String PERM_BALANCE_TOP = "gemseconomy.command.baltop";
    private static final Predicate<CommandSender> REQUIREMENT = sender -> {
        if (!GemsEconomy.inst().getDataStore().isTopSupported()) {
            sender.sendMessage(F.balanceTopNoSupport().replace("{storage}", GemsEconomy.inst().getDataStore().getStorageType().name()));
            return false;
        } else return true;
    };

    public BalanceTopCommand() {
        // Arguments
        Argument<Integer> pageArgument = new IntegerArgument("页码", 1, 100).replaceSuggestions(ArgumentSuggestions.strings("1"));

        // Commands
        new CommandAPICommand(NAME)
                .withAliases(ALIASES)
                .withRequirement(REQUIREMENT)
                .withPermission(PERM_BALANCE_TOP)
                .executes((sender, args) -> {
                    sendBalanceTop(sender, null, 0, 1);
                })
                .register();
        new CommandAPICommand(NAME)
                .withAliases(ALIASES)
                .withRequirement(REQUIREMENT)
                .withPermission(PERM_BALANCE_TOP)
                .withArguments(BaseArguments.CURRENCY)
                .executes((sender, args) -> {
                    sendBalanceTop(sender, (String) args[0], 0, 1);
                })
                .register();
        new CommandAPICommand(NAME)
                .withAliases(ALIASES)
                .withRequirement(REQUIREMENT)
                .withPermission(PERM_BALANCE_TOP)
                .withArguments(BaseArguments.CURRENCY)
                .withArguments(pageArgument)
                .executes((sender, args) -> {
                    int page = Math.max((int) args[1], 1);
                    int offset = 10 * (page - 1);
                    sendBalanceTop(sender, (String) args[0], offset, page);
                })
                .register();
    }

    private void sendBalanceTop(@NotNull CommandSender sender, @Nullable String rawCurrency, int offset, int pageNum) {

        // Check currency
        Currency currency = rawCurrency == null
                ? GemsEconomy.inst().getCurrencyManager().getDefaultCurrency()
                : GemsEconomy.inst().getCurrencyManager().getCurrency(rawCurrency);
        if (currency == null) {
            sender.sendMessage(F.unknownCurrency());
            return;
        }

        GemsEconomy.inst().getDataStore().getTopList(currency, offset, ACCOUNTS_PER_PAGE, cachedTopListEntries -> {
            sender.sendMessage(F.balanceTopHeader()
                    .replace("{currencycolor}", "" + currency.getColor())
                    .replace("{currencyplural}", currency.getPlural())
                    .replace("{page}", String.valueOf(pageNum)));

            int num = (10 * (pageNum - 1)) + 1;
            for (CachedTopListEntry entry : cachedTopListEntries) {
                double balance = entry.getAmount();
                sender.sendMessage(F.balanceTop().replace("{number}", String.valueOf(num)).replace("{currencycolor}", "" + currency.getColor())
                        .replace("{player}", entry.getName()).replace("{balance}", currency.format(balance)));
                num++;
            }
            if (cachedTopListEntries.isEmpty()) {
                sender.sendMessage(F.balanceTopEmpty());
            } else {
                sender.sendMessage(F.balanceTopNext().replace("{currencycolor}", "" + currency.getColor()).replace("{currencyplural}", currency.getPlural()).replace("{page}", String.valueOf((pageNum + 1))));
            }
        });
    }
}

package me.xanium.gemseconomy.commandsv2;

import dev.jorel.commandapi.CommandAPICommand;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.commandsv2.argument.BaseArguments;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.file.F;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.function.Predicate;

public class ChequeCommand {

    private static final String NAME = "cheque";
    private static final String PERM_CHEQUE = "gemseconomy.command.cheque";
    private static final Predicate<CommandSender> REQUIREMENT = sender -> GemsEconomy.inst().isChequesEnabled();

    public ChequeCommand() {
        // Commands
        new CommandAPICommand(NAME)
                .withRequirement(REQUIREMENT)
                .withPermission(PERM_CHEQUE)
                .withSubcommand(new CommandAPICommand("redeem")
                        .executesPlayer(((player, args) -> {
                            ItemStack item = player.getInventory().getItemInMainHand();
                            if (item.getType().equals(Material.valueOf(GemsEconomy.inst().getConfig().getString("cheque.material")))) {
                                if (GemsEconomy.inst().getChequeManager().isValid(item)) {
                                    double value = GemsEconomy.inst().getChequeManager().getValue(item);
                                    if (item.getAmount() > 1) {
                                        player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                                    } else {
                                        player.getInventory().remove(item);
                                    }
                                    Account user = GemsEconomy.inst().getAccountManager().getAccount(player);
                                    Currency currency = GemsEconomy.inst().getChequeManager().getCurrency(item);
                                    user.deposit(currency, value);
                                    player.sendMessage(F.chequeRedeemed());
                                } else {
                                    player.sendMessage(F.chequeInvalid());
                                }
                            } else {
                                player.sendMessage(F.chequeInvalid());
                            }
                        })))
                .withSubcommand(new CommandAPICommand("write")
                        .withArguments(BaseArguments.AMOUNT)
                        .executesPlayer((player, args) -> {
                            Currency defaultCurrency = GemsEconomy.inst().getCurrencyManager().getDefaultCurrency();
                            Account user = GemsEconomy.inst().getAccountManager().getAccount(player);
                            makeCheque(player, user, (int) args[0], defaultCurrency);
                        }))
                .withSubcommand(new CommandAPICommand("write")
                        .withArguments(BaseArguments.AMOUNT)
                        .withArguments(BaseArguments.CURRENCY)
                        .executesPlayer((player, args) -> {
                            Account user = GemsEconomy.inst().getAccountManager().getAccount(player);
                            Currency currency = GemsEconomy.inst().getCurrencyManager().getCurrency((String) args[1]);
                            makeCheque(player, user, (int) args[0], currency);
                        }))
                .register();
    }

    private void makeCheque(Player player, Account user, double amount, Currency currency) {
        if (currency != null) {
            if (user.hasEnough(currency, amount)) {
                user.withdraw(currency, amount);
                player.getInventory().addItem(GemsEconomy.inst().getChequeManager().write(player.getName(), currency, amount));
                player.sendMessage(F.chequeSuccess());
            } else {
                player.sendMessage(F.insufficientFunds().replace("{currencycolor}", currency.getColor() + "").replace("{currency}", currency.getSingular()));
            }
        } else {
            player.sendMessage(F.unknownCurrency());
        }
    }
}

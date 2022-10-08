package me.xanium.gemseconomy.commandsv3.command;

import cloud.commandframework.Command;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.GemsMessages;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.commandsv3.GemsCommand;
import me.xanium.gemseconomy.commandsv3.GemsCommands;
import me.xanium.gemseconomy.commandsv3.argument.AmountArgument;
import me.xanium.gemseconomy.commandsv3.argument.CurrencyArgument;
import me.xanium.gemseconomy.currency.Currency;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ChequeCommand extends GemsCommand {
    public ChequeCommand(GemsEconomy plugin, GemsCommands manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command.Builder<CommandSender> builder = manager
                .commandBuilder("cheque")
                .permission("gemseconomy.command.cheque");

        Command<CommandSender> redeem = builder
                .literal("redeem")
                .senderType(Player.class)
                .handler(context -> {
                    Player player = (Player) context.getSender();
                    if (!GemsEconomy.inst().isChequesEnabled()) {
                        GemsEconomy.lang().sendComponent(player, "err_cheque_no_support");
                        return;
                    }

                    ItemStack item = player.getInventory().getItemInMainHand();
                    if (item.getType().equals(Material.matchMaterial(GemsEconomy.inst().getConfig().getString("cheque.material", "paper")))) {
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
                            GemsEconomy.lang().sendComponent(player, "msg_cheque_redeemed");
                        } else {
                            GemsEconomy.lang().sendComponent(player, "err_cheque_invalid");
                        }
                    } else {
                        GemsEconomy.lang().sendComponent(player, "err_cheque_invalid");
                    }
                })
                .build();

        Command<CommandSender> write = builder
                .literal("write")
                .argument(AmountArgument.of("amount"))
                .argument(CurrencyArgument.optional("currency"))
                .senderType(Player.class)
                .handler(context -> {
                    Player player = (Player) context.getSender();
                    if (!GemsEconomy.inst().isChequesEnabled()) {
                        GemsEconomy.lang().sendComponent(player, "err_cheque_no_support");
                        return;
                    }

                    int amount = context.get("amount");
                    Currency currency = context.getOrDefault("currency", GemsEconomy.inst().getCurrencyManager().getDefaultCurrency());
                    if (currency == null) {
                        GemsEconomy.lang().sendComponent(player, "err_no_default_currency");
                        return;
                    }

                    Account account = GemsEconomy.inst().getAccountManager().getAccount(player);
                    if (account == null) {
                        GemsEconomy.lang().sendComponent(player, "err_account_missing");
                        return;
                    }

                    makeCheque(player, account, amount, currency);
                })
                .build();

        manager.register(List.of(
                redeem, write
        ));
    }

    @NonnullByDefault
    private void makeCheque(Player player, Account user, double amount, Currency currency) {
        if (user.hasEnough(currency, amount)) {
            user.withdraw(currency, amount);
            player.getInventory().addItem(GemsEconomy.inst().getChequeManager().write(player.getName(), currency, amount));
            GemsEconomy.lang().sendComponent(player, "msg_cheque_written");
        } else {
            GemsEconomy.lang().sendComponent(player, GemsEconomy.lang()
                    .component(player, "err_insufficient_funds")
                    .replaceText(GemsMessages.CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
            );
        }
    }

}

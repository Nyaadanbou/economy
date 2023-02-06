package me.xanium.gemseconomy.command.command;

import cloud.commandframework.Command;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.command.GemsCommand;
import me.xanium.gemseconomy.command.GemsCommands;
import me.xanium.gemseconomy.command.argument.AmountArgument;
import me.xanium.gemseconomy.command.argument.CurrencyArgument;
import me.xanium.gemseconomy.currency.Currency;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

import static java.util.Objects.requireNonNull;
import static me.xanium.gemseconomy.GemsMessages.CURRENCY_REPLACEMENT;

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
                if (!GemsEconomy.getInstance().isChequesEnabled()) {
                    GemsEconomy.lang().sendComponent(player, "err_cheque_no_support");
                    return;
                }

                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType().equals(Material.matchMaterial(GemsEconomy.getInstance().getConfig().getString("cheque.material", "paper")))) {
                    if (GemsEconomy.getInstance().getChequeManager().isValid(item)) {
                        if (item.getAmount() > 1) {
                            player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
                        } else {
                            player.getInventory().remove(item);
                        }
                        Account account = requireNonNull(GemsEconomy.getInstance().getAccountManager().fetchAccount(player));
                        Currency currency = GemsEconomy.getInstance().getChequeManager().getCurrency(item);
                        if (currency == null) {
                            GemsEconomy.lang().sendComponent(player, "err_cheque_invalid");
                        } else {
                            account.deposit(currency, GemsEconomy.getInstance().getChequeManager().getValue(item));
                            GemsEconomy.lang().sendComponent(player, "msg_cheque_redeemed");
                        }
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
                if (!GemsEconomy.getInstance().isChequesEnabled()) {
                    GemsEconomy.lang().sendComponent(player, "err_cheque_no_support");
                    return;
                }

                double amount = context.get("amount");
                Account account = GemsEconomy.getInstance().getAccountManager().fetchAccount(player);
                Currency currency = context.getOrDefault("currency", GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency());
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
            ItemStack cheque = GemsEconomy.getInstance().getChequeManager().write(player.getName(), currency, amount);
            if (cheque != null) {
                user.withdraw(currency, amount);
                player.getInventory().addItem(cheque);
                GemsEconomy.lang().sendComponent(player, "msg_cheque_written");
            } else {
                GemsEconomy.lang().sendComponent(player, "err_cheque_written");
            }
        } else {
            GemsEconomy.lang().sendComponent(player, GemsEconomy.lang()
                .component(player, "err_insufficient_funds")
                .replaceText(CURRENCY_REPLACEMENT.apply(currency.getDisplayName()))
            );
        }
    }

}

package cc.mewcraft.economy.vault;

import cc.mewcraft.economy.EconomyPlugin;
import cc.mewcraft.economy.api.Account;
import cc.mewcraft.economy.api.Currency;
import cc.mewcraft.economy.utils.UtilServer;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import com.google.common.base.Preconditions;

import java.util.ArrayList;
import java.util.List;

import org.checkerframework.checker.nullness.qual.NonNull;

public class VaultHook extends AbstractEconomy {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "Economy";
    }

    @Override
    public String format(double amount) {
        return EconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency().simpleFormat(amount);
    }

    @Override
    public String currencyNameSingular() {
        return EconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency().getName();
    }

    @Override
    public String currencyNamePlural() {
        return currencyNameSingular();
    }

    @Override
    public boolean has(String playerName, double amount) {
        Account account = EconomyPlugin.getInstance().getAccountManager().fetchAccount(playerName);
        return account != null && account.hasEnough(amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return has(player.getName(), amount);
    }

    @Override
    public boolean hasAccount(String playerName) {
        return EconomyPlugin.getInstance().getAccountManager().hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return EconomyPlugin.getInstance().getAccountManager().hasAccount(player);
    }

    @SuppressWarnings("DuplicatedCode")
    private @NonNull Account getAccountOrCreate(@NonNull OfflinePlayer offlinePlayer) {
        Preconditions.checkNotNull(offlinePlayer, "player");
        return EconomyPlugin.getInstance().getAccountManager().createAccount(offlinePlayer);
    }

    @SuppressWarnings("DuplicatedCode")
    private @NonNull Account getAccountOrCreate(@NonNull String playerName) {
        Preconditions.checkNotNull(playerName, "playerName");
        return EconomyPlugin.getInstance().getAccountManager().createAccount(playerName);
    }

    @Override
    public double getBalance(String playerName) {
        UtilServer.consoleLog("Lookup name: " + playerName);
        Account account = getAccountOrCreate(playerName);
        Currency currency = EconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency();
        return account.getBalance(currency);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        UtilServer.consoleLog("Lookup name: " + player.getName() + " (" + player.getUniqueId() + ")");
        Account account = getAccountOrCreate(player);
        Currency currency = EconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency();
        return account.getBalance(currency);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        UtilServer.consoleLog("Lookup name: " + player.getName() + " (" + player.getUniqueId() + ")");

        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
        }

        Account account = getAccountOrCreate(player);
        Currency currency = EconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency();

        if (account.withdraw(currency, amount)) {
            return new EconomyResponse(
                    amount,
                    account.getBalance(currency),
                    EconomyResponse.ResponseType.SUCCESS,
                    null
            );
        } else {
            return new EconomyResponse(
                    amount,
                    account.getBalance(currency),
                    EconomyResponse.ResponseType.FAILURE,
                    "Could not withdraw " + amount + " from " + player.getName() + " because they don't have enough funds"
            );
        }
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        UtilServer.consoleLog("Lookup name: " + player.getName() + " (" + player.getUniqueId() + ")");

        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds");
        }

        Account account = getAccountOrCreate(player);
        Currency currency = EconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency();

        if (account.deposit(currency, amount)) {
            return new EconomyResponse(
                    amount,
                    account.getBalance(currency),
                    EconomyResponse.ResponseType.SUCCESS,
                    null
            );
        } else {
            return new EconomyResponse(
                    amount,
                    account.getBalance(currency),
                    EconomyResponse.ResponseType.FAILURE,
                    "Could not deposit " + amount + " to " + player.getName() + " because they are not allowed to receive currency."
            );
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        UtilServer.consoleLog("Lookup name: " + playerName);

        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
        }

        Account account = getAccountOrCreate(playerName);
        Currency currency = EconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency();

        if (account.withdraw(currency, amount)) {
            return new EconomyResponse(
                    amount,
                    account.getBalance(currency),
                    EconomyResponse.ResponseType.SUCCESS,
                    null
            );
        } else {
            return new EconomyResponse(
                    amount,
                    account.getBalance(currency),
                    EconomyResponse.ResponseType.FAILURE,
                    "Could not withdraw " + amount + " from " + playerName + " because they don't have enough funds"
            );
        }
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        UtilServer.consoleLog("Lookup name: " + playerName);

        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds");
        }

        Account account = getAccountOrCreate(playerName);
        Currency currency = EconomyPlugin.getInstance().getCurrencyManager().getDefaultCurrency();

        if (account.deposit(currency, amount)) {
            return new EconomyResponse(
                    amount,
                    account.getBalance(currency),
                    EconomyResponse.ResponseType.SUCCESS,
                    null
            );
        } else {
            return new EconomyResponse(
                    amount,
                    account.getBalance(currency),
                    EconomyResponse.ResponseType.FAILURE,
                    "Could not deposit " + amount + " to " + playerName + " because they are not allowed to receive currency."
            );
        }
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        EconomyPlugin.getInstance().getAccountManager().createAccount(playerName);
        return EconomyPlugin.getInstance().getAccountManager().hasAccount(playerName);
    }

    //<editor-fold desc="Expanded Methods">
    @Override
    public EconomyResponse withdrawPlayer(String playerName, String world, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String world, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public boolean has(String playerName, String world, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean hasAccount(String playerName, String world) {
        return hasAccount(playerName);
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String world, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String world, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String world) {
        return hasAccount(player);
    }

    @Override
    public boolean has(OfflinePlayer player, String world, double amount) {
        return has(player, amount);
    }

    @Override
    public boolean createPlayerAccount(String playerName, String world) {
        return createPlayerAccount(playerName);
    }
    //</editor-fold>

    //<editor-fold desc="Unsupported Methods">
    @Override
    public EconomyResponse createBank(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Economy does not support bank accounts!");
    }

    @Override
    public List<String> getBanks() {
        return new ArrayList<>();
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return -1;
    }
    //</editor-fold>

}

package me.xanium.gemseconomy.vault;

import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.utils.UtilServer;
import net.milkbowl.vault.economy.AbstractEconomy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class GEVaultHook extends AbstractEconomy {

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "GemsEconomy";
    }

    @Override
    public String format(double amount) {
        return GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency().simpleFormat(amount);
    }

    @Override
    public String currencyNameSingular() {
        return GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency().getName();
    }

    @Override
    public String currencyNamePlural() {
        return currencyNameSingular();
    }

    @Override
    public boolean has(String playerName, double amount) {
        Account account = GemsEconomy.getInstance().getAccountManager().fetchAccount(playerName);
        return account != null && account.hasEnough(amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return has(player.getName(), amount);
    }

    @Override
    public boolean hasAccount(String playerName) {
        return GemsEconomy.getInstance().getAccountManager().hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return GemsEconomy.getInstance().getAccountManager().hasAccount(player);
    }

    @SuppressWarnings("DuplicatedCode")
    private @NonNull Account getAccountOrCreate(@NonNull OfflinePlayer offlinePlayer) {
        Objects.requireNonNull(offlinePlayer, "player");
        return Objects.requireNonNull(GemsEconomy.getInstance().getAccountManager().createAccount(offlinePlayer));
    }

    @SuppressWarnings("DuplicatedCode")
    private @NonNull Account getAccountOrCreate(@NonNull String playerName) {
        Objects.requireNonNull(playerName, "playerName");
        return Objects.requireNonNull(GemsEconomy.getInstance().getAccountManager().createAccount(playerName));
    }

    @Override
    public double getBalance(String playerName) {
        UtilServer.consoleLog("Lookup name: " + playerName);
        Account account = getAccountOrCreate(playerName);
        Currency currency = GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency();
        return account.getBalance(currency);
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        UtilServer.consoleLog("Lookup name: " + player.getName() + " (" + player.getUniqueId() + ")");
        Account account = getAccountOrCreate(player);
        Currency currency = GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency();
        return account.getBalance(currency);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {
        UtilServer.consoleLog("Lookup name: " + player.getName() + " (" + player.getUniqueId() + ")");

        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
        }

        Account account = getAccountOrCreate(player);
        Currency currency = GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency();

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
        Currency currency = GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency();

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
        Currency currency = GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency();

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
        Currency currency = GemsEconomy.getInstance().getCurrencyManager().getDefaultCurrency();

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
        GemsEconomy.getInstance().getAccountManager().createAccount(playerName);
        return GemsEconomy.getInstance().getAccountManager().hasAccount(playerName);
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
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GemsEconomy does not support bank accounts!");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GemsEconomy does not support bank accounts!");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GemsEconomy does not support bank accounts!");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GemsEconomy does not support bank accounts!");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GemsEconomy does not support bank accounts!");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GemsEconomy does not support bank accounts!");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GemsEconomy does not support bank accounts!");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "GemsEconomy does not support bank accounts!");
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

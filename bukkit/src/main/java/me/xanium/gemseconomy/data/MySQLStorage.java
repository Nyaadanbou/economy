/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package me.xanium.gemseconomy.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.lucko.helper.promise.Promise;
import me.xanium.gemseconomy.GemsEconomyPlugin;
import me.xanium.gemseconomy.account.PlayerAccount;
import me.xanium.gemseconomy.api.Account;
import me.xanium.gemseconomy.api.Currency;
import me.xanium.gemseconomy.currency.ServerCurrency;
import me.xanium.gemseconomy.utils.UtilServer;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public final class MySQLStorage extends DataStorage {
    // --- Table Names ---
    private final String currencyTable = getTablePrefix() + "_currencies";
    private final String accountsTable = getTablePrefix() + "_accounts";

    // --- SQL Statements ---
    private final String SAVE_ACCOUNT = "INSERT INTO `" + getTablePrefix() + "_accounts` (`nickname`, `uuid`, `payable`, `balance_data`, `balance_acc`) VALUES(?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `nickname` = VALUES(`nickname`), `uuid` = VALUES(`uuid`), `payable` = VALUES(`payable`), `balance_data` = VALUES(`balance_data`), `balance_acc` = VALUES(`balance_acc`)";
    private final String SAVE_CURRENCY = "INSERT INTO `" + getTablePrefix() + "_currencies` (`uuid`, `name`, `default_balance`, `max_balance`, `symbol`, `decimals_supported`, `is_default`, `payable`, `color`, `exchange_rate`) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE `uuid` = VALUES(`uuid`), `name` = VALUES(`name`), `default_balance` = VALUES(`default_balance`), `max_balance` = VALUES(`max_balance`), `symbol` = VALUES(`symbol`), `decimals_supported` = VALUES(`decimals_supported`), `is_default` = VALUES(`is_default`), `payable` = VALUES(`payable`), `color` = VALUES(`color`), `exchange_rate` = VALUES(`exchange_rate`)";

    // --- Hikari ---
    private @Nullable HikariDataSource hikari;
    private final @NonNull HikariConfig hikariConfig;
    private final @NonNull String database;

    public MySQLStorage(@NonNull String host, int port, @NonNull String database, @NonNull String username, @NonNull String password) {
        super(StorageType.MYSQL, true);

        this.database = database;

        this.hikariConfig = new HikariConfig();
        this.hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database + "?allowPublicKeyRetrieval=true&useSSL=false");
        this.hikariConfig.setPassword(password);
        this.hikariConfig.setUsername(username);
        this.hikariConfig.setMaxLifetime(1500000);
        this.hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        this.hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        this.hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        this.hikariConfig.addDataSourceProperty("userServerPrepStmts", "true");
    }

    public @NonNull HikariDataSource getHikari() {
        if (hikari == null) {
            throw new IllegalStateException();
        }
        return hikari;
    }

    private String getTablePrefix() {
        return requireNonNull(GemsEconomyPlugin.getInstance().getConfig().getString("mysql.prefix"));
    }

    private void setupTables(Connection conn) throws SQLException {
        try (
            PreparedStatement stmt1 = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.currencyTable + " (uuid VARCHAR(255) NOT NULL PRIMARY KEY, name VARCHAR(255), default_balance DECIMAL, max_balance DECIMAL, symbol VARCHAR(255), decimals_supported TINYINT, is_default TINYINT, payable TINYINT, color VARCHAR(255), exchange_rate DECIMAL);");
            PreparedStatement stmt2 = conn.prepareStatement("CREATE TABLE IF NOT EXISTS " + this.accountsTable + " (nickname VARCHAR(255), uuid VARCHAR(255) NOT NULL PRIMARY KEY, payable TINYINT, balance_data LONGTEXT NULL);")
        ) {
            stmt1.execute();
            stmt2.execute();
        }
    }

    @Override
    public void initialize() {
        hikari = new HikariDataSource(hikariConfig);

        try (
            Connection conn = hikari.getConnection()
        ) {
            setupTables(conn);

            Map<String, List<String>> structure = new HashMap<>();
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet tableResultSet = metaData.getTables(database, "public", null, new String[]{"TABLE"})) {
                while (tableResultSet.next()) {
                    String tableCat = tableResultSet.getString("TABLE_CAT");
                    String tableName = tableResultSet.getString("TABLE_NAME");
                    UtilServer.consoleLog("Table catalog: %s, Table name: %s".formatted(tableCat, tableName));

                    if (tableName.startsWith(getTablePrefix())) {
                        structure.put(tableName, new ArrayList<>());

                        UtilServer.consoleLog("Added table: " + tableName);
                    }
                }
            }

            for (String table : structure.keySet()) {
                try (ResultSet columnResultSet = metaData.getColumns(database, "public", table, null)) {
                    while (columnResultSet.next()) {
                        String columnName = columnResultSet.getString("COLUMN_NAME");
                        structure.get(table).add(columnName);

                        UtilServer.consoleLog("Added column: " + columnName + " (" + table + ")");
                    }
                }
            }

            // region Update old SQL tables
            List<String> currencyTableColumns = structure.get(currencyTable);
            if (currencyTableColumns != null && !currencyTableColumns.isEmpty()) {
                if (!currencyTableColumns.contains("exchange_rate")) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                        "ALTER TABLE " + currencyTable + " ADD exchange_rate DECIMAL NULL DEFAULT NULL AFTER `color`;"
                    )) {
                        stmt.execute();
                        UtilServer.consoleLog("Altered table " + currencyTable + " to support the new exchange_rate variable.");
                    }
                }
                if (!currencyTableColumns.contains("max_balance")) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                        "ALTER TABLE " + currencyTable + " ADD max_balance DECIMAL NULL DEFAULT NULL AFTER `default_balance`;"
                    )) {
                        stmt.execute();
                        UtilServer.consoleLog("Altered table " + currencyTable + " to support the new max_balance variable.");
                    }
                }
                if (currencyTableColumns.contains("name_singular")) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                        "ALTER TABLE " + currencyTable + " RENAME COLUMN `name_singular` TO `name`"
                    )) {
                        stmt.execute();
                        UtilServer.consoleLog("Altered table " + currencyTable + " to rename 'name_singular' to just 'name'.");
                    }
                }
                if (currencyTableColumns.contains("name_plural")) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                        "ALTER TABLE " + currencyTable + " DROP COLUMN `name_plural`"
                    )) {
                        stmt.execute();
                        UtilServer.consoleLog("Altered table " + currencyTable + " to remove plural name of currencies.");
                    }
                }
            }

            List<String> accountTableColumns = structure.get(accountsTable);
            if (accountTableColumns != null && !accountTableColumns.isEmpty()) {
                if (!accountTableColumns.contains("balance_data")) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                        "ALTER TABLE " + accountsTable + " ADD balance_data LONGTEXT NULL DEFAULT '{}' AFTER `payable`;"
                    )) {
                        stmt.execute();
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(
                        "ALTER TABLE " + accountsTable + " DROP COLUMN `id`"
                    )) {
                        stmt.execute();
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(
                        "TRUNCATE TABLE " + accountsTable
                    )) {
                        stmt.execute();
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(
                        "ALTER TABLE " + accountsTable + " ADD PRIMARY KEY (uuid)"
                    )) {
                        stmt.execute();
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(
                        "ALTER TABLE " + currencyTable + " DROP COLUMN `id`"
                    )) {
                        stmt.execute();
                    }

                    try (PreparedStatement stmt = conn.prepareStatement(
                        "ALTER TABLE " + currencyTable + " ADD PRIMARY KEY (uuid)"
                    )) {
                        stmt.execute();
                    }

                    UtilServer.consoleLog("Altered tables " + accountsTable + " to support the new balance data saving");
                }
                if (!accountTableColumns.contains("balance_acc")) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                        "ALTER TABLE " + accountsTable + " ADD balance_acc LONGTEXT NULL DEFAULT '{}' AFTER `balance_data`;"
                    )) {
                        stmt.execute();
                        UtilServer.consoleLog("Altered table " + accountsTable + " to support the records of accumulated balance");
                    }
                }
            }
            // endregion
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {
        if (hikari != null) {
            hikari.close();
        }
    }

    @Override
    public List<Currency> loadCurrencies() {
        requireNonNull(hikari, "hikari");
        List<Currency> currencies = new ArrayList<>();
        try (
            Connection conn = getHikari().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + currencyTable);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                Currency currency = loadCurrencyFromDatabase(rs);
                currencies.add(currency);
                UtilServer.consoleLog("Loaded currency: %s (default_balance: %s, max_balance: %s, default_currency: %s, payable: %s)".formatted(
                    currency.getName(), currency.getDefaultBalance(), currency.getMaximumBalance(), currency.isDefaultCurrency(), currency.isPayable()
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return currencies;
    }

    @Override
    public @Nullable Currency loadCurrency(final @NonNull UUID uuid) {
        try (
            Connection conn = getHikari().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + currencyTable + " WHERE uuid = ? LIMIT 1;")
        ) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return loadCurrencyFromDatabase(rs);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void saveCurrency(final @NonNull Currency currency) {
        try (
            Connection conn = getHikari().getConnection();
            PreparedStatement stmt = conn.prepareStatement(SAVE_CURRENCY)
        ) {
            stmt.setString(1, currency.getUuid().toString());
            stmt.setString(2, currency.getName());
            stmt.setDouble(3, currency.getDefaultBalance());
            stmt.setDouble(4, currency.getMaximumBalance());
            stmt.setString(5, currency.getSymbolNullable());
            stmt.setInt(6, currency.isDecimalSupported() ? 1 : 0);
            stmt.setInt(7, currency.isDefaultCurrency() ? 1 : 0);
            stmt.setInt(8, currency.isPayable() ? 1 : 0);
            stmt.setString(9, currency.getColor().asHexString());
            stmt.setDouble(10, currency.getExchangeRate());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteCurrency(final @NonNull Currency currency) {
        try (
            Connection conn = getHikari().getConnection();
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + currencyTable + " WHERE uuid = ?")
        ) {
            stmt.setString(1, currency.getUuid().toString());
            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override public @Nullable Account updateAccount(final @Nullable Account account) {
        if (account == null) {
            return null;
        }
        try (
            Connection conn = getHikari().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + accountsTable + " WHERE uuid = ? LIMIT 1")
        ) {
            stmt.setString(1, account.getUuid().toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return updateAccountFromDatabase(rs, account);
                }
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        return account;
    }

    @Override
    public @Nullable Account loadAccount(final @NonNull String name) {
        try (
            Connection conn = getHikari().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + accountsTable + " WHERE nickname = ? LIMIT 1")
        ) {
            // Note: string comparisons are case-insensitive by default in the configuration of SQL server database
            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return loadAccountFromDatabase(rs);
                }
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public @Nullable Account loadAccount(final @NonNull UUID uuid) {
        try (
            Connection conn = getHikari().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + accountsTable + " WHERE uuid = ? LIMIT 1")
        ) {
            stmt.setString(1, uuid.toString());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return loadAccountFromDatabase(rs);
                }
            }
        } catch (SQLException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void createAccount(final @NonNull Account account) {
        saveAccountToDatabase(account);
        UtilServer.consoleLog("Account created and saved: " + account.getNickname() + " - " + account.getUuid());
    }

    @Override
    public void saveAccount(final @NonNull Account account) {
        saveAccountToDatabase(account);
        UtilServer.consoleLog("Account saved: " + account.getNickname() + " - " + account.getUuid());
    }

    @Override
    public void deleteAccount(final @NonNull Account account) {
        deleteAccount(account.getUuid());
    }

    @Override
    public void deleteAccount(final @NonNull UUID uuid) {
        try (
            Connection conn = getHikari().getConnection();
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + accountsTable + " WHERE uuid = ? LIMIT 1")
        ) {
            stmt.setString(1, uuid.toString());
            stmt.execute();
            UtilServer.consoleLog("Account deleted: " + uuid);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteAccount(final @NonNull String name) {
        try (
            Connection conn = getHikari().getConnection();
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + accountsTable + " WHERE nickname = ? LIMIT 1")
        ) {
            stmt.setString(1, name);
            stmt.execute();
            UtilServer.consoleLog("Account deleted: " + name);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public @NonNull List<Account> getOfflineAccounts() {
        List<Account> accounts = new ArrayList<>();

        try (
            Connection conn = getHikari().getConnection();
            PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + accountsTable);
            ResultSet rs = stmt.executeQuery()
        ) {
            while (rs.next()) {
                accounts.add(loadAccountFromDatabase(rs));
            }
        } catch (SQLException | ParseException ex) {
            ex.printStackTrace();
        }

        return accounts;
    }

    @Override
    public @NonNull Promise<List<TransientBalance>> getTransientBalances(final @NonNull Currency currency) {
        return Promise.supplyingAsync(() -> {
            List<TransientBalance> balances = new ArrayList<>();
            try (
                Connection conn = getHikari().getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM " + accountsTable);
                ResultSet rs = stmt.executeQuery()
            ) {
                while (rs.next()) {
                    String json = rs.getString("balance_data");
                    JSONObject data = (JSONObject) new JSONParser().parse(json);
                    Number bal = (Number) data.get(currency.getUuid().toString());
                    if (bal == null)
                        continue; // Should rarely happen, but anyway
                    balances.add(new TransientBalance(rs.getString("nickname"), bal.doubleValue()));
                }
            } catch (SQLException | ParseException ex) {
                ex.printStackTrace();
            }
            return balances;
        });
    }

    /**
     * Common logics of loading an Account from database.
     */
    private Account loadAccountFromDatabase(final ResultSet rs) throws SQLException, ParseException {
        Account account = new PlayerAccount(
            UUID.fromString(rs.getString("uuid")),
            rs.getString("nickname")
        );
        return updateAccountFromDatabase(rs, account);
    }

    /**
     * Common logics of updating an Account from database.
     */
    private Account updateAccountFromDatabase(final ResultSet resultSet, final Account account) throws SQLException, ParseException {
        account.setCanReceiveCurrency(resultSet.getInt("payable") == 1);

        // These two columns may contain null values
        String balanceDataRaw = Optional.ofNullable(resultSet.getString("balance_data")).orElse("{}");
        String balanceAccRaw = Optional.ofNullable(resultSet.getString("balance_acc")).orElse("{}");

        JSONParser parser = new JSONParser();
        JSONObject balanceDataJson = (JSONObject) parser.parse(balanceDataRaw);
        JSONObject balanceAccJson = (JSONObject) parser.parse(balanceAccRaw);

        for (Currency currency : plugin.getCurrencyManager().getLoadedCurrencies()) {
            // Read balance data
            Number balance = (Number) balanceDataJson.get(currency.getUuid().toString());
            if (balance != null) {
                account.getBalances().put(currency, balance.doubleValue());
            } else {
                account.getBalances().put(currency, currency.getDefaultBalance());
            }

            // Read accumulated balance data
            Number accBalance = (Number) balanceAccJson.get(currency.getUuid().toString());
            if (accBalance != null) {
                account.getHeapBalances().put(currency, accBalance.doubleValue());
            }
        }

        return account;
    }

    /**
     * Common logics of saving an Account to database.
     */
    @SuppressWarnings("unchecked")
    private void saveAccountToDatabase(final @NonNull Account account) {
        try (
            Connection conn = getHikari().getConnection();
            PreparedStatement stmt = conn.prepareStatement(SAVE_ACCOUNT)
        ) {
            stmt.setString(1, account.getDisplayName()); // write nickname
            stmt.setString(2, account.getUuid().toString()); // write uuid
            stmt.setInt(3, account.canReceiveCurrency() ? 1 : 0); // write payable

            final JSONObject balJsonObj = new JSONObject();
            account.getBalances().forEach((currency, balance) -> balJsonObj.put(currency.getUuid().toString(), balance));
            stmt.setString(4, balJsonObj.toJSONString()); // write balance

            final JSONObject accBalJsonObj = new JSONObject();
            account.getHeapBalances().forEach((currency, balance) -> accBalJsonObj.put(currency.getUuid().toString(), balance));
            stmt.setString(5, accBalJsonObj.toJSONString()); // write accumulated balance

            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Common logics of loading a Currency from database.
     */
    private Currency loadCurrencyFromDatabase(ResultSet rs) throws SQLException {
        UUID uuid = UUID.fromString(rs.getString("uuid"));
        String name = rs.getString("name");
        double defaultBalance = rs.getDouble("default_balance");
        double maximumBalance = rs.getDouble("max_balance");
        String symbol = rs.getString("symbol");
        boolean supportDecimals = rs.getInt("decimals_supported") == 1;
        boolean defaultCurrency = rs.getInt("is_default") == 1;
        boolean payable = rs.getInt("payable") == 1;
        TextColor color = Optional.ofNullable(rs.getString("color")).map(TextColor::fromHexString).orElse(NamedTextColor.WHITE);
        double exchangeRate = rs.getDouble("exchange_rate");

        Currency currency = new ServerCurrency(uuid, name);
        currency.setDefaultBalance(defaultBalance);
        currency.setMaximumBalance(maximumBalance);
        currency.setSymbol(symbol);
        currency.setDecimalSupported(supportDecimals);
        currency.setDefaultCurrency(defaultCurrency);
        currency.setPayable(payable);
        currency.setColor(color);
        currency.setExchangeRate(exchangeRate);

        return currency;
    }
}

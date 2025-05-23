/*
 * Copyright Xanium Development (c) 2013-2018. All Rights Reserved.
 * Any code contained within this document, and any associated APIs with similar branding
 * are the sole property of Xanium Development. Distribution, reproduction, taking snippets or claiming
 * any contents as your own will break the terms of the license, and void any agreements with you, the third party.
 * Thank you.
 */

package cc.mewcraft.economy.account;

import cc.mewcraft.economy.EconomyPlugin;
import cc.mewcraft.economy.api.Account;
import cc.mewcraft.economy.data.DataStorage;
import cc.mewcraft.economy.message.Action;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import me.lucko.helper.profiles.OfflineModeProfiles;
import me.lucko.helper.scheduler.HelperExecutors;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public class AccountManager {

    private final @NonNull EconomyPlugin plugin;
    private final @NonNull LoadingCache<UUID, Optional<Account>> cache; // accounts loaded in memory

    public AccountManager(@NonNull EconomyPlugin plugin) {
        this.plugin = plugin;
        this.cache = CacheBuilder.newBuilder()
                .expireAfterAccess(Duration.of(10, ChronoUnit.MINUTES))
                .build(CacheLoader.asyncReloading(new CacheLoader<>() {
                    @Override public @NonNull Optional<Account> load(final @NonNull UUID key) {
                        return Optional.ofNullable(plugin.getDataStore().loadAccount(key));
                    }

                    @Override public @NonNull ListenableFuture<Optional<Account>> reload(final @NonNull UUID key, final @NonNull Optional<Account> oldValue) {
                        return oldValue
                                .map(account -> plugin.getDataStore().updateAccount(account) /* Note that it doesn't change reference */)
                                .map(value -> Futures.immediateFuture(Optional.of(value)))
                                .orElseGet(() -> Futures.immediateFuture(oldValue));
                    }
                }, HelperExecutors.asyncHelper()));
    }

    /**
     * Creates an account and returns it.
     * <p>
     * If the account with specific UUID is already loaded in cache or exists in database, this method will just return
     * the existing object. Otherwise, this method will create a new account and return it.
     *
     * @param uuid the uuid of the new account
     * @return a newly created account if there wasn't one, or the existing one
     */
    public @NonNull Account createAccount(@NonNull UUID uuid) {
        return createAccount(uuid, null);
    }

    /**
     * Creates an account and returns it.
     * <p>
     * If the account with specific UUID is already loaded in cache or exists in database, this method will just return
     * the existing object. Otherwise, this method will create a new account and return it.
     *
     * @param uuid the uuid of the new account
     * @return a newly created account if there wasn't one, or the existing one
     */
    public @NonNull Account createAccount(@NonNull UUID uuid, @Nullable String nickname) {
        Account test = fetchAccount(uuid);
        if (test != null) {
            return test;
        }

        Account account = new PlayerAccount(uuid, nickname);

        // Set default balances
        plugin.getCurrencyManager().getLoadedCurrencies().forEach(currency ->
                account.setBalance(currency, currency.getDefaultBalance())
        );

        cacheAccount(account);
        plugin.getDataStore().createAccount(account);
        plugin.getMessenger().sendMessage(Action.UPDATE_ACCOUNT, account.getUuid());

        return account;
    }

    /**
     * Creates an account and returns it.
     * <p>
     * If the account with specific player is already loaded in cache or exists in database, this method will just
     * return the existing object. Otherwise, this method will create a new account and return it.
     *
     * @param player the player who owns the new account
     * @return a newly created account if there wasn't one, or the existing one
     */
    public @NonNull Account createAccount(@NonNull OfflinePlayer player) {
        return createAccount(player.getUniqueId());
    }

    /**
     * Creates an account and returns it.
     * <p>
     * If the account with specific nickname is already loaded in cache or exists in database, this method will just
     * return the existing object. Otherwise, this method will create a new account and return it.
     * <p>
     * This method will try the best to create a new account with an uuid being generated by the <b>Mojang offline
     * method</b>. That is, the uuid of the created account <b>WILL NOT</b> be the Mojang online version even if the
     * nickname does map to an online Minecraft account. The existence of this method is for the compatibility with
     * other plugins which need to create economy Accounts with the plain string name being the account identifier.
     *
     * @param nickname the nickname of the new account
     * @return a newly created account if there wasn't one, or the existing one
     * @see OfflineModeProfiles
     */
    public @NonNull Account createAccount(@NonNull String nickname) {
        Account test = fetchAccount(nickname);
        if (test != null) {
            return test;
        }

        Account account = new PlayerAccount(
                // Get the UUID of the name by using the Mojang offline player method
                // so that we can ensure same nicknames always point to the same UUID.
                OfflineModeProfiles.getUniqueId(nickname),
                // The nickname must be stored for this account
                // because it is the identifier of the account!
                nickname
        );

        // Set default balances
        plugin.getCurrencyManager().getLoadedCurrencies().forEach(currency ->
                account.setBalance(currency, currency.getDefaultBalance())
        );

        cacheAccount(account);
        plugin.getDataStore().createAccount(account);
        plugin.getMessenger().sendMessage(Action.UPDATE_ACCOUNT, account.getUuid());

        return account;
    }

    /**
     * Deletes specific account from both cache and database.
     *
     * @param uuid the uuid of specific account
     */
    public void deleteAccount(@NonNull UUID uuid) {
        cache.invalidate(uuid); // Delete from memory
        plugin.getDataStore().deleteAccount(uuid); // Delete from database
    }

    /**
     * Deletes specific account from both cache and database.
     *
     * @param player the owner of specific account
     */
    public void deleteAccount(@NonNull OfflinePlayer player) {
        deleteAccount(player.getUniqueId());
    }

    /**
     * @see #hasAccount(UUID)
     */
    public boolean hasAccount(@NonNull OfflinePlayer player) {
        return hasAccount(player.getUniqueId());
    }

    /**
     * Checks whether the account with given uuid exists.
     * <p>
     * This method is equivalent to simply call:
     *
     * <pre>{@code fetchAccount(uuid) != null}</pre>
     *
     * @param uuid the uuid of the account
     * @return true if the account with given uuid exists; otherwise false
     */
    public boolean hasAccount(@NonNull UUID uuid) {
        return fetchAccount(uuid) != null;
    }

    /**
     * Checks whether the account with given name exists.
     * <p>
     * This method is equivalent to the call:
     *
     * <pre>{@code fetchAccount(name) != null}</pre>
     *
     * @param name the name of the account
     * @return true if the account with given name exists; otherwise false
     */
    public boolean hasAccount(@NonNull String name) {
        return fetchAccount(name) != null;
    }

    /**
     * @see #fetchAccount(UUID)
     */
    public @Nullable Account fetchAccount(@NonNull OfflinePlayer player) {
        return fetchAccount(player.getUniqueId());
    }

    /**
     * Fetch an account with specific uuid.
     * <p>
     * This will first get the account from cache, followed by database. If neither is found, it will return null.
     *
     * @param uuid the uuid of the account to fetch for
     * @return an account with given uuid
     */
    public @Nullable Account fetchAccount(@NonNull UUID uuid) {
        return cache.getUnchecked(uuid).orElse(null);
    }

    /**
     * Fetch an account with specific name.
     * <p>
     * This will first get the account from cache, followed by database. If neither is found, it will return null.
     *
     * @param name the name of the account to fetch for
     * @return an account with given name
     */
    public @Nullable Account fetchAccount(@NonNull String name) {
        for (final Optional<Account> account : cache.asMap().values()) {
            if (account.isPresent() && name.equalsIgnoreCase(account.get().getNickname())) {
                return account.get();
            }
        }
        @Nullable Account account = plugin.getDataStore().loadAccount(name);
        if (account == null) {
            return null;
        } else {
            cacheAccount(account);
            return account;
        }
    }

    /**
     * Caches an account.
     * <p>
     * If the account is already cached (regardless whether it's empty optional or not), this method will override the
     * original object.
     *
     * @param account the account to be loaded into memory
     */
    public void cacheAccount(@NonNull Account account) {
        cache.put(account.getUuid(), Optional.of(account));
    }

    /**
     * Checks if specific account is currently cached.
     *
     * @param uuid the uuid of specific account
     * @return true if the account is cached; false otherwise
     */
    @SuppressWarnings("OptionalAssignedToNull")
    public boolean cached(@NonNull UUID uuid) {
        return cache.getIfPresent(uuid) != null;
    }

    /**
     * Refreshes specific account from database.
     *
     * @param uuid the uuid of the account
     */
    public void refreshAccount(@NonNull UUID uuid) {
        // TODO According to javadoc,
        //  if the account is being read by another thread this method will basically do nothing.
        //  This would be an issue because the account may not sync with the database.
        //  Link: https://github.com/google/guava/wiki/cacheExplained
        cache.refresh(uuid);
    }

    /**
     * Discards specific account object from memory.
     *
     * @param uuid the uuid of the account
     */
    public void flushAccount(@NonNull UUID uuid) {
        cache.invalidate(uuid);
    }

    /**
     * Discards all account objects from memory.
     */
    public void flushAccounts() {
        cache.invalidateAll();
    }

    /**
     * Returns a view of all the accounts that are currently loaded in memory.
     *
     * @return a view of all the accounts loaded in memory
     */
    public @NonNull Collection<Account> getCachedAccounts() {
        return cache.asMap().values().stream().filter(Optional::isPresent).map(Optional::get).toList();
    }

    /**
     * It's simply a wrapper of {@link DataStorage#getOfflineAccounts()}.
     */
    public @NonNull Collection<Account> getOfflineAccounts() {
        return plugin.getDataStore().getOfflineAccounts();
    }

    @Deprecated
    public @Nullable Account getAccount(@NonNull Player player) {
        return fetchAccount(player.getUniqueId());
    }

    @Deprecated
    public @Nullable Account getAccount(@NonNull OfflinePlayer player) {
        return fetchAccount(player.getUniqueId());
    }

    @Deprecated
    public @Nullable Account getAccount(@NonNull UUID uuid) {
        return fetchAccount(uuid);
    }

    @Deprecated
    public @Nullable Account getAccount(@NonNull String name) {
        return fetchAccount(name);
    }

}


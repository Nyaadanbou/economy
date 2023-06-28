package me.xanium.gemseconomy.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class GemsEconomyProvider {
    private static GemsEconomy instance = null;

    /**
     * Provides static access to the {@link GemsEconomy} API.
     *
     * <p>Ideally, the ServiceManager for the platform should be used to obtain an
     * instance, however, this provider can be used if this is not viable.</p>
     *
     * @return an instance of the GemsEconomy API
     * @throws IllegalStateException if the API is not loaded yet
     */
    public static @NotNull GemsEconomy get() {
        GemsEconomy instance = me.xanium.gemseconomy.api.GemsEconomyProvider.instance;
        if (instance == null) {
            throw new IllegalStateException("Instance is not loaded yet.");
        }
        return instance;
    }

    @ApiStatus.Internal
    public static void register(GemsEconomy instance) {
        me.xanium.gemseconomy.api.GemsEconomyProvider.instance = instance;
    }

    @ApiStatus.Internal
    public static void unregister() {
        me.xanium.gemseconomy.api.GemsEconomyProvider.instance = null;
    }

    @ApiStatus.Internal
    private GemsEconomyProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }
}

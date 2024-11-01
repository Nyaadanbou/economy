package cc.mewcraft.economy.api;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

public class EconomyProvider {
    private static Economy instance = null;

    /**
     * Provides static access to the {@link Economy} API.
     *
     * <p>Ideally, the ServiceManager for the platform should be used to obtain an
     * instance, however, this provider can be used if this is not viable.</p>
     *
     * @return an instance of the Economy API
     * @throws IllegalStateException if the API is not loaded yet
     */
    public static @NotNull Economy get() {
        Economy instance = EconomyProvider.instance;
        if (instance == null) {
            throw new IllegalStateException("Instance is not loaded yet.");
        }
        return instance;
    }

    @ApiStatus.Internal
    public static void register(Economy instance) {
        EconomyProvider.instance = instance;
    }

    @ApiStatus.Internal
    public static void unregister() {
        EconomyProvider.instance = null;
    }

    @ApiStatus.Internal
    private EconomyProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }
}

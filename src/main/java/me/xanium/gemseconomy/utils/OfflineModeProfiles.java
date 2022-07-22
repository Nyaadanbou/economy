package me.xanium.gemseconomy.utils;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Utilities for handling profiles on offline-mode servers.
 */
public final class OfflineModeProfiles {

    /**
     * Gets the {@link UUID} given to a player with the
     * given username on an offline mode server.
     *
     * @param username the name of the player
     * @return the uuid
     */
    public static UUID getUniqueId(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
    }

    private OfflineModeProfiles() {
        throw new UnsupportedOperationException("This class cannot be instantiated");
    }
}

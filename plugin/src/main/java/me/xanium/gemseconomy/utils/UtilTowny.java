package me.xanium.gemseconomy.utils;

import org.checkerframework.checker.nullness.qual.Nullable;

public class UtilTowny {

    public static boolean isTownyAccount(@Nullable String name) {
        return name != null && (name.startsWith("town-") || name.startsWith("nation-") || name.startsWith("towny-"));
    }

}

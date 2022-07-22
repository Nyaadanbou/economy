package me.xanium.gemseconomy.utils;

public class UtilTowny {

    public static boolean isTownyAccount(String name) {
        return name.startsWith("town-") || name.startsWith("nation-") || name.startsWith("towny-");
    }

}

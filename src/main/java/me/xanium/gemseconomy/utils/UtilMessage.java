package me.xanium.gemseconomy.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class UtilMessage {

    public static void sendMessageToAccount(UUID account, String message) {
        Player player = Bukkit.getPlayer(account);
        if (player != null) player.sendMessage(message);
    }

}

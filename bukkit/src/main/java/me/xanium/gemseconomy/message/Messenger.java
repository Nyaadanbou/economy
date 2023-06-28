package me.xanium.gemseconomy.message;

import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import me.lucko.helper.terminable.Terminable;
import me.xanium.gemseconomy.GemsEconomyPlugin;
import me.xanium.gemseconomy.message.impl.EmptyMessenger;
import me.xanium.gemseconomy.message.impl.RedisMessenger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public interface Messenger extends Terminable {

    static Messenger get() {
        Plugin connector = Bukkit.getServer().getPluginManager().getPlugin("ConnectorPlugin");
        if (connector == null) {
            return new EmptyMessenger();
        }
        return new RedisMessenger(GemsEconomyPlugin.getInstance(), (BukkitConnectorPlugin) connector);
    }

    void sendMessage(String type, UUID uuid);

}

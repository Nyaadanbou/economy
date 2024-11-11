package cc.mewcraft.economy.message;

import cc.mewcraft.economy.EconomyPlugin;
import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import me.lucko.helper.terminable.Terminable;
import cc.mewcraft.economy.message.impl.EmptyMessenger;
import cc.mewcraft.economy.message.impl.RedisMessenger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public interface Messenger extends Terminable {

    static Messenger get() {
        Plugin connector = Bukkit.getServer().getPluginManager().getPlugin("ConnectorPlugin");
        if (connector == null) {
            return new EmptyMessenger();
        }
        return new RedisMessenger(EconomyPlugin.getInstance(), (BukkitConnectorPlugin) connector);
    }

    void sendMessage(String type, UUID uuid);

}

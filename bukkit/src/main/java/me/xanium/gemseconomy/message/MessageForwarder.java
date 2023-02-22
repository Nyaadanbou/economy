package me.xanium.gemseconomy.message;

import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import me.lucko.helper.terminable.Terminable;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.message.impl.DummyMessageForwarder;
import me.xanium.gemseconomy.message.impl.RedisMessageForwarder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public interface MessageForwarder extends Terminable {

    static MessageForwarder get() {
        Plugin connector = Bukkit.getServer().getPluginManager().getPlugin("ConnectorPlugin");
        if (connector == null) {
            return new DummyMessageForwarder();
        }
        return new RedisMessageForwarder(GemsEconomy.getInstance(), (BukkitConnectorPlugin) connector);
    }

    void sendMessage(String type, UUID uuid);

}

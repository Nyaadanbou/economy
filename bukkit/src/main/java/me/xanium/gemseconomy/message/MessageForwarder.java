package me.xanium.gemseconomy.message;

import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.message.impl.DummyMessageForwarder;
import me.xanium.gemseconomy.message.impl.RedisMessageForwarder;

import java.util.UUID;

public interface MessageForwarder {

    static MessageForwarder get() {
        BukkitConnectorPlugin connector = GemsEconomy.getInstance().getPlugin("ConnectorPlugin", BukkitConnectorPlugin.class);
        if (connector == null) {
            return new DummyMessageForwarder();
        }
        return new RedisMessageForwarder(GemsEconomy.getInstance(), connector);
    }

    void sendMessage(Action type, UUID uuid);

}

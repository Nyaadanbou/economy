package cc.mewcraft.economy.message.impl;

import cc.mewcraft.economy.EconomyPlugin;
import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import de.themoep.connectorplugin.connector.ConnectingPlugin;
import de.themoep.connectorplugin.connector.Message;
import de.themoep.connectorplugin.connector.MessageTarget;
import me.lucko.helper.Schedulers;
import cc.mewcraft.economy.message.Action;
import cc.mewcraft.economy.message.Messenger;
import cc.mewcraft.economy.utils.UtilServer;
import org.bukkit.entity.Player;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import java.util.UUID;
import java.util.function.BiConsumer;

public class RedisMessenger implements Messenger {

    private final EconomyPlugin plugin;
    private final BukkitConnectorPlugin connectorPlugin;
    private final ConnectingPlugin connectingPlugin = () -> "Economy";

    public RedisMessenger(EconomyPlugin plugin, BukkitConnectorPlugin connectorPlugin) {
        this.plugin = plugin;
        this.connectorPlugin = connectorPlugin;

        // Must register it after "Done!"
        Schedulers.bukkit().runTask(plugin, this::registerHandlers);
    }

    /**
     * Handles incoming messages.
     */
    private void registerHandlers() {
        registerHandler(Action.CREATE_ACCOUNT, (player, message) -> {
            // Don't need to "sync" account creation
        });
        registerHandler(Action.UPDATE_ACCOUNT, (player, message) -> {
            UUID uuid = readUUID(message.getData());
            if (plugin.getAccountManager().cached(uuid)) {
                plugin.getAccountManager().refreshAccount(uuid);
                UtilServer.consoleLog("Received (source: %s) - Account updated: %s".formatted(message.getSendingServer(), uuid));
            }
        });
        registerHandler(Action.DELETE_ACCOUNT, (player, message) -> {
            UUID uuid = readUUID(message.getData());
            plugin.getAccountManager().flushAccount(uuid); // It's already deleted from database by sending server
            UtilServer.consoleLog("Received (source: %s) - Account deleted: %s".formatted(message.getSendingServer(), uuid));
        });
        registerHandler(Action.CREATE_CURRENCY, (player, message) -> {
            UUID uuid = readUUID(message.getData());
            plugin.getCurrencyManager().updateCurrency(uuid, true);
            UtilServer.consoleLog("Received (source: %s) - Currency created: %s".formatted(message.getSendingServer(), uuid));
        });
        registerHandler(Action.UPDATE_CURRENCY, (player, message) -> {
            UUID uuid = readUUID(message.getData());
            plugin.getCurrencyManager().updateCurrency(uuid, false);
            UtilServer.consoleLog("Received (source: %s) - Currency updated: %s".formatted(message.getSendingServer(), uuid));
        });
        registerHandler(Action.DELETE_CURRENCY, (player, message) -> {
            UUID uuid = readUUID(message.getData());
            plugin.getCurrencyManager().removeCurrency(uuid);
            UtilServer.consoleLog("Received (source: %s) - Currency deleted: %s".formatted(message.getSendingServer(), uuid));
        });
    }

    @Override
    public void sendMessage(final String action, final UUID uuid) {
        Schedulers.async().run(() -> {
            sendData(action, writeUUID(uuid));
            switch (action) {
                case Action.CREATE_ACCOUNT -> UtilServer.consoleLog("Sent - Account created: " + uuid);
                case Action.UPDATE_ACCOUNT -> UtilServer.consoleLog("Sent - Account updated: " + uuid);
                case Action.DELETE_ACCOUNT -> UtilServer.consoleLog("Sent - Account deleted: " + uuid);
                case Action.CREATE_CURRENCY -> UtilServer.consoleLog("Sent - Currency created: " + uuid);
                case Action.UPDATE_CURRENCY -> UtilServer.consoleLog("Sent - Currency updated: " + uuid);
                case Action.DELETE_CURRENCY -> UtilServer.consoleLog("Sent - Currency deleted: " + uuid);
            }
        });
    }

    private void sendData(String action, byte[] data) {
        connectorPlugin.getConnector().sendData(connectingPlugin, action, MessageTarget.OTHERS_QUEUE, data);
    }

    private void registerHandler(String action, BiConsumer<Player, Message> handler) {
        connectorPlugin.getConnector().registerMessageHandler(connectingPlugin, action, handler);
    }

    private UUID readUUID(byte[] data) {
        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        return new UUID(in.readLong(), in.readLong());
    }

    private byte[] writeUUID(UUID uuid) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeLong(uuid.getMostSignificantBits());
        out.writeLong(uuid.getLeastSignificantBits());
        return out.toByteArray();
    }

    @Override public void close() {
        connectorPlugin.getConnector().unregisterMessageHandlers(this.connectingPlugin);
    }

}

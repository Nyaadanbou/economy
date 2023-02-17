package me.xanium.gemseconomy.message.impl;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.themoep.connectorplugin.bukkit.BukkitConnectorPlugin;
import de.themoep.connectorplugin.bukkit.connector.RedisConnector;
import de.themoep.connectorplugin.connector.Message;
import de.themoep.connectorplugin.connector.MessageTarget;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.message.Action;
import me.xanium.gemseconomy.message.MessageForwarder;
import me.xanium.gemseconomy.utils.UtilServer;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.function.BiConsumer;

// TODO use async where possible

@SuppressWarnings("UnstableApiUsage")
public class RedisMessageForwarder implements MessageForwarder {

    private final GemsEconomy plugin;
    private final BukkitConnectorPlugin connectorPlugin;
    private final RedisConnector connector;

    public RedisMessageForwarder(GemsEconomy plugin, BukkitConnectorPlugin connectorPlugin) {
        this.plugin = plugin;
        this.connectorPlugin = connectorPlugin;
        this.connector = (RedisConnector) connectorPlugin.getConnector();

        // --- Handle incoming messages ---

        registerHandler(Action.CREATE_ACCOUNT, (player, message) -> {
            // Don't need to "sync" account creation
        });
        registerHandler(Action.UPDATE_ACCOUNT, (player, message) -> {
            UUID uuid = readUUID(message.getData());
            plugin.getAccountManager().refreshAccount(uuid);
            UtilServer.consoleLog("Received (source: %s) - Account updated: %s".formatted(message.getSendingServer(), uuid));
        });
        registerHandler(Action.DELETE_ACCOUNT, (player, message) -> {
            UUID uuid = readUUID(message.getData());
            plugin.getAccountManager().flushAccount(uuid); // It's already deleted from database by sending server
            UtilServer.consoleLog("Received (source: %s) - Account deleted: %s".formatted(message.getSendingServer(), uuid));
        });
        registerHandler(Action.CREATE_CURRENCY, (player, message) -> {
            UUID uuid = readUUID(message.getData());
            plugin.getCurrencyManager().loadCurrencyOverride(uuid);
            UtilServer.consoleLog("Received (source: %s) - Currency created: %s".formatted(message.getSendingServer(), uuid));
        });
        registerHandler(Action.UPDATE_CURRENCY, (player, message) -> {
            UUID uuid = readUUID(message.getData());
            plugin.getCurrencyManager().updateCurrency(uuid);
            UtilServer.consoleLog("Received (source: %s) - Currency updated: %s".formatted(message.getSendingServer(), uuid));
        });
        registerHandler(Action.DELETE_CURRENCY, (player, message) -> {
            UUID uuid = readUUID(message.getData());
            plugin.getCurrencyManager().removeCurrency(uuid);
            UtilServer.consoleLog("Received (source: %s) - Currency deleted: %s".formatted(message.getSendingServer(), uuid));
        });
    }

    @Override public void sendMessage(final Action action, final UUID uuid) {
        sendData(action, MessageTarget.OTHERS_QUEUE, writeUUID(uuid));
        switch (action) {
            case CREATE_ACCOUNT -> UtilServer.consoleLog("Sent - Account created: " + uuid);
            case UPDATE_ACCOUNT -> UtilServer.consoleLog("Sent - Account updated: " + uuid);
            case DELETE_ACCOUNT -> UtilServer.consoleLog("Sent - Account deleted: " + uuid);
            case CREATE_CURRENCY -> UtilServer.consoleLog("Sent - Currency created: " + uuid);
            case UPDATE_CURRENCY -> UtilServer.consoleLog("Sent - Currency updated: " + uuid);
            case DELETE_CURRENCY -> UtilServer.consoleLog("Sent - Currency deleted: " + uuid);
        }
    }

    private void sendData(Action action, MessageTarget target, byte[] data) {
        this.connector.sendData(this.connectorPlugin, action.name(), target, data);
    }

    private void sendData(Action action, MessageTarget target, Player player, byte[] data) {
        this.connector.sendData(this.connectorPlugin, action.name(), target, player, data);
    }

    private void sendData(Action action, MessageTarget target, String server, byte[] data) {
        this.connector.sendData(this.connectorPlugin, action.name(), target, server, data);
    }

    private void registerHandler(Action action, BiConsumer<Player, Message> handler) {
        this.connector.registerMessageHandler(this.connectorPlugin, action.name(), handler);
    }

    private UUID readUUID(byte[] data) {
        ByteArrayDataInput in = ByteStreams.newDataInput(data);
        return new UUID(in.readLong(), in.readLong());
    }

    private byte[] writeUUID(UUID uuid) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeLong(uuid.getLeastSignificantBits());
        out.writeLong(uuid.getMostSignificantBits());
        return out.toByteArray();
    }

}

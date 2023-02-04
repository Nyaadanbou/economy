package me.xanium.gemseconomy.bungee;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.utils.SchedulerUtils;
import me.xanium.gemseconomy.utils.UtilServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class UpdateForwarder implements PluginMessageListener {

    private static final String CHANNEL_NAME = "GemsEconomy Data Channel";

    /**
     * GemsEconomy Bungee-Spigot Messaging Listener
     * <p>
     * This listener is used to update currencies and balance for players on different servers. This is important to
     * sustain synced balances and currencies on all the servers.
     */
    private final GemsEconomy plugin;

    public UpdateForwarder(GemsEconomy plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player notInUse, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subChannel = in.readUTF();
        if (subChannel.equals(CHANNEL_NAME)) {
            String[] info = in.readUTF().split(",");
            String type = info[0];
            String name = info[1];
            if (plugin.isDebug()) {
                UtilServer.consoleLog(CHANNEL_NAME + " - Received: " + type + " = " + name);
            }

            if (type.equals("currency")) {
                UUID uuid = UUID.fromString(name);
                Currency currency = plugin.getCurrencyManager().getCurrency(uuid);
                if (currency != null) {
                    plugin.getDataStore().updateCurrencyLocally(currency);
                    if (GemsEconomy.getInstance().isDebug()) {
                        UtilServer.consoleLog(CHANNEL_NAME + " - Currency " + name + " updated.");
                    }
                }
            } else if (type.equals("account")) {
                UUID uuid = UUID.fromString(name);
                plugin.getAccountManager().flushAccount(uuid);
                SchedulerUtils.runAsync(() -> plugin.getDataStore().loadAccount(uuid));
                if (plugin.isDebug()) {
                    UtilServer.consoleLog(CHANNEL_NAME + " - Account " + name + " updated.");
                }
            }
        }
    }


    public void sendUpdateMessage(String type, String name) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ONLINE");
        out.writeUTF(CHANNEL_NAME);

        ByteArrayOutputStream msgbytes = new ByteArrayOutputStream();
        DataOutputStream msgout = new DataOutputStream(msgbytes);
        try {
            msgout.writeUTF(type + "," + name);
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        out.write(msgbytes.toByteArray());

        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player == null) {
            if (GemsEconomy.getInstance().isDebug()) {
                UtilServer.consoleLog(CHANNEL_NAME + " - No players online. Don't send update message.");
            }
            return;
        }
        player.sendPluginMessage(GemsEconomy.getInstance(), "BungeeCord", out.toByteArray());
    }

}

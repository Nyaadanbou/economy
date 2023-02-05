package me.xanium.gemseconomy.bungee;

import com.google.common.collect.Iterables;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.lucko.helper.Schedulers;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.utils.UtilServer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class UpdateForwarder implements PluginMessageListener {

    // TODO replace it with redis as it doesn't work well if servers are empty

    private static final String CHANNEL_NAME = "GemsEconomy";

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
        if (!channel.equals("BungeeCord"))
            return;

        ByteArrayDataInput in = ByteStreams.newDataInput(message);

        if (!in.readUTF().equals(CHANNEL_NAME))
            return; // Sub-channel is not ours

        UpdateType type = UpdateType.valueOf(in.readUTF());
        String name = in.readUTF();

        if (plugin.isDebug())
            UtilServer.consoleLog(CHANNEL_NAME + " - Received: " + type + " = " + name);

        switch (type) {
            case CURRENCY -> {
                UUID uuid = UUID.fromString(name);
                Currency currency = plugin.getCurrencyManager().getCurrency(uuid);
                if (currency != null) {
                    plugin.getDataStore().updateCurrencyLocally(currency);
                    if (GemsEconomy.getInstance().isDebug())
                        UtilServer.consoleLog(CHANNEL_NAME + " - Currency " + name + " updated.");
                }
            }
            case ACCOUNT -> {
                UUID uuid = UUID.fromString(name);
                Schedulers.async().run(() -> plugin.getAccountManager().refreshAccount(uuid));
                if (plugin.isDebug()) {
                    UtilServer.consoleLog(CHANNEL_NAME + " - Account " + name + " updated.");
                }
            }
        }
    }

    public void sendUpdateMessage(UpdateType type, String name) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Forward");
        out.writeUTF("ONLINE");

        out.writeUTF(CHANNEL_NAME);
        out.writeUTF(type.name());
        out.writeUTF(name);

        Player player = Iterables.getFirst(Bukkit.getOnlinePlayers(), null);
        if (player == null) {
            if (GemsEconomy.getInstance().isDebug())
                UtilServer.consoleLog(CHANNEL_NAME + " - No players online. Don't send update message.");
            return;
        }

        player.sendPluginMessage(GemsEconomy.getInstance(), "BungeeCord", out.toByteArray());
    }

}
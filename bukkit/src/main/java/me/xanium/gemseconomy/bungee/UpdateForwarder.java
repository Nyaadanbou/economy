package me.xanium.gemseconomy.bungee;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.lucko.helper.Schedulers;
import me.lucko.helper.Services;
import me.lucko.helper.messaging.bungee.BungeeCord;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.utils.UtilServer;

import java.util.UUID;

public class UpdateForwarder {

    // TODO replace it with redis since it doesn't sync with "empty servers" at all.
    //  "empty servers" = servers without online players.
    //  Useful library: https://github.com/lucko/helper/wiki/helper:-Messenger

    private static final String CHANNEL_NAME = "GemsEconomy";

    /**
     * GemsEconomy Bungee-Spigot Messaging Listener
     * <p>
     * This listener is used to update currencies and balance for players on different servers. This is important to
     * sustain synced balances and currencies on all the servers.
     */
    private final GemsEconomy plugin;
    private final BungeeCord bungee;

    public UpdateForwarder(GemsEconomy plugin) {
        this.plugin = plugin;
        this.bungee = Services.load(BungeeCord.class);

        bungee.registerForwardCallback(CHANNEL_NAME, this::handleIncomingMessage); // Handle received messages
    }

    public void sendUpdateMessage(UpdateType type, UUID uuid) {
        @SuppressWarnings("UnstableApiUsage")
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF(type.name());
        out.writeUTF(uuid.toString());

        if (GemsEconomy.getInstance().isDebug()) {
            UtilServer.consoleLog(CHANNEL_NAME + " - Sending update message: " + type + " - " + uuid);
        }

        bungee.forward(BungeeCord.ALL_SERVERS, CHANNEL_NAME, out);
    }

    private boolean handleIncomingMessage(ByteArrayDataInput in) {
        UpdateType type = UpdateType.valueOf(in.readUTF());
        UUID uuid = UUID.fromString(in.readUTF());

        if (plugin.isDebug())
            UtilServer.consoleLog(CHANNEL_NAME + " - Received: " + type + " - " + uuid);

        switch (type) {
            case CURRENCY -> {
                Currency currency = plugin.getCurrencyManager().getCurrency(uuid);
                if (currency != null) {
                    plugin.getDataStore().updateCurrencyLocally(currency); // TODO support sync addition & deletion
                    if (GemsEconomy.getInstance().isDebug()) {
                        UtilServer.consoleLog(CHANNEL_NAME + " - Currency " + uuid + " updated.");
                    }
                }
            }
            case ACCOUNT -> {
                Schedulers.async().run(() -> plugin.getAccountManager().refreshAccount(uuid));
                if (plugin.isDebug()) {
                    UtilServer.consoleLog(CHANNEL_NAME + " - Account " + uuid + " updated.");
                }
            }
        }

        return false;
    }
}
package cc.mewcraft.economy.message.impl;

import cc.mewcraft.economy.message.Messenger;

import java.util.UUID;

public class EmptyMessenger implements Messenger {

    @Override public void sendMessage(final String type, final UUID uuid) {}

    @Override public void close() {}

}

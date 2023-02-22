package me.xanium.gemseconomy.message.impl;

import me.xanium.gemseconomy.message.MessageForwarder;

import java.util.UUID;

public class DummyMessageForwarder implements MessageForwarder {

    @Override public void sendMessage(final String type, final UUID uuid) {}

    @Override public void close() {}

}

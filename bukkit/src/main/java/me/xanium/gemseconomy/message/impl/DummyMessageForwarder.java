package me.xanium.gemseconomy.message.impl;

import me.xanium.gemseconomy.message.Action;
import me.xanium.gemseconomy.message.MessageForwarder;

import java.util.UUID;

public class DummyMessageForwarder implements MessageForwarder {

    @Override public void sendMessage(final Action type, final UUID uuid) {}

}

package me.xanium.gemseconomy.command;

import me.xanium.gemseconomy.GemsEconomy;

public abstract class AbstractCommand {

    protected final GemsEconomy plugin;
    protected final CommandManager manager;

    public AbstractCommand(GemsEconomy plugin, CommandManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    abstract public void register();

}

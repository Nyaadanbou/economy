package me.xanium.gemseconomy.command;

import me.xanium.gemseconomy.GemsEconomyPlugin;

public abstract class AbstractCommand {

    protected final GemsEconomyPlugin plugin;
    protected final CommandManager manager;

    public AbstractCommand(GemsEconomyPlugin plugin, CommandManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public abstract void register();

}

package me.xanium.gemseconomy.command;

import me.xanium.gemseconomy.GemsEconomy;

public abstract class GemsCommand {

    protected final GemsEconomy plugin;
    protected final GemsCommands manager;

    public GemsCommand(GemsEconomy plugin, GemsCommands manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    abstract public void register();
    
}

package cc.mewcraft.economy.command;

import cc.mewcraft.economy.EconomyPlugin;

public abstract class AbstractCommand {

    protected final EconomyPlugin plugin;
    protected final CommandManager manager;

    public AbstractCommand(EconomyPlugin plugin, CommandManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public abstract void register();

}

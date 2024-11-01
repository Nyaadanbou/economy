package cc.mewcraft.economy.logging;

import cc.mewcraft.economy.EconomyPlugin;

public class EconomyLogger extends AbstractLogger {

    private final EconomyPlugin plugin;

    public EconomyLogger(EconomyPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

}

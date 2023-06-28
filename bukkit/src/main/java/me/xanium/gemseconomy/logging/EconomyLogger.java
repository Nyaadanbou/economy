package me.xanium.gemseconomy.logging;

import me.xanium.gemseconomy.GemsEconomyPlugin;

public class EconomyLogger extends AbstractLogger {

    private final GemsEconomyPlugin plugin;

    public EconomyLogger(GemsEconomyPlugin plugin) {
        super(plugin);
        this.plugin = plugin;
    }

}

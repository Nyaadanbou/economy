package me.xanium.gemseconomy.commandsv2;

import dev.jorel.commandapi.CommandAPICommand;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.file.F;

public class DebugCommand {

    private static final String NAME = "ecodebug";
    private static final String PERM_DEBUG = "gemseconomy.command.debug";

    public DebugCommand() {
        new CommandAPICommand(NAME)
                .withPermission(PERM_DEBUG)
                .executes((sender, args) -> {
                    GemsEconomy.inst().setDebug(!GemsEconomy.inst().isDebug());
                    sender.sendMessage(F.debugStatus().replace("{status}", String.valueOf(GemsEconomy.inst().isDebug())));
                })
                .register();
    }
}

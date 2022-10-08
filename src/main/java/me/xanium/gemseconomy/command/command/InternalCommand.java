package me.xanium.gemseconomy.command.command;

import cloud.commandframework.Command;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.command.GemsCommand;
import me.xanium.gemseconomy.command.GemsCommands;
import org.bukkit.command.CommandSender;

import java.util.List;

public class InternalCommand extends GemsCommand {

    public InternalCommand(GemsEconomy plugin, GemsCommands manager) {
        super(plugin, manager);
    }

    @Override
    public void register() {
        Command.Builder<CommandSender> builder = manager
                .commandBuilder("gemseconomy");

        Command<CommandSender> reloadLanguages = builder
                .literal("reload")
                .literal("lang", "languages")
                .permission("gemseconomy.command.reload")
                .handler(context -> {
                    CommandSender sender = context.getSender();
                    GemsEconomy.inst().reloadLanguages();
                    GemsEconomy.lang().sendComponent(sender, "msg_reloaded_lang",
                            "plugin", GemsEconomy.inst().getDescription().getName(),
                            "version", GemsEconomy.inst().getDescription().getVersion());
                })
                .build();

        manager.register(List.of(reloadLanguages));
    }

}

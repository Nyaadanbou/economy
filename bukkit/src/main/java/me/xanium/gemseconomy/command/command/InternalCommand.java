package me.xanium.gemseconomy.command.command;

import cloud.commandframework.Command;
import me.xanium.gemseconomy.GemsEconomyPlugin;
import me.xanium.gemseconomy.command.AbstractCommand;
import me.xanium.gemseconomy.command.CommandManager;
import org.bukkit.command.CommandSender;

import java.util.List;

public class InternalCommand extends AbstractCommand {

    public InternalCommand(GemsEconomyPlugin plugin, CommandManager manager) {
        super(plugin, manager);
    }

    @SuppressWarnings("deprecation")
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
                    GemsEconomyPlugin.getInstance().reloadLanguages();
                    GemsEconomyPlugin.lang().sendComponent(sender, "msg_reloaded_lang",
                            "plugin", GemsEconomyPlugin.getInstance().getDescription().getName(),
                            "version", GemsEconomyPlugin.getInstance().getDescription().getVersion()
                    );
                })
                .build();

        manager.register(List.of(reloadLanguages));
    }

}

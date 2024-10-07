package me.xanium.gemseconomy.command.command;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.xanium.gemseconomy.GemsEconomyPlugin;
import me.xanium.gemseconomy.command.AbstractCommand;
import me.xanium.gemseconomy.command.CommandManager;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class InternalCommand extends AbstractCommand {

    public InternalCommand(GemsEconomyPlugin plugin, CommandManager manager) {
        super(plugin, manager);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void register() {
        Command.Builder<CommandSourceStack> builder = manager.getCommandManager()
                .commandBuilder("gemseconomy");

        Command<CommandSourceStack> reloadLanguages = builder
                .literal("reload")
                .literal("lang", "languages")
                .permission("gemseconomy.command.reload")
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
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

package cc.mewcraft.economy.command.command;

import cc.mewcraft.economy.EconomyPlugin;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import cc.mewcraft.economy.command.AbstractCommand;
import cc.mewcraft.economy.command.CommandManager;
import org.bukkit.command.CommandSender;
import org.incendo.cloud.Command;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
public class InternalCommand extends AbstractCommand {

    public InternalCommand(EconomyPlugin plugin, CommandManager manager) {
        super(plugin, manager);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void register() {
        Command.Builder<CommandSourceStack> builder = manager.getCommandManager()
                .commandBuilder("economy");

        Command<CommandSourceStack> reloadLanguages = builder
                .literal("reload")
                .literal("lang", "languages")
                .permission("economy.command.reload")
                .handler(context -> {
                    CommandSender sender = context.sender().getSender();
                    EconomyPlugin.getInstance().reloadLanguages();
                    EconomyPlugin.lang().sendComponent(sender, "msg_reloaded_lang",
                            "plugin", EconomyPlugin.getInstance().getDescription().getName(),
                            "version", EconomyPlugin.getInstance().getDescription().getVersion()
                    );
                })
                .build();

        manager.register(List.of(reloadLanguages));
    }

}

package me.xanium.gemseconomy.command;

import io.leangen.geantyref.TypeToken;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import me.lucko.helper.scheduler.HelperExecutors;
import me.xanium.gemseconomy.GemsEconomyPlugin;
import me.xanium.gemseconomy.command.command.BalanceAccCommand;
import me.xanium.gemseconomy.command.command.BalanceCommand;
import me.xanium.gemseconomy.command.command.BalanceTopCommand;
import me.xanium.gemseconomy.command.command.CurrencyCommand;
import me.xanium.gemseconomy.command.command.EconomyCommand;
import me.xanium.gemseconomy.command.command.InternalCommand;
import me.xanium.gemseconomy.command.command.PayCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.ComponentMessageThrowable;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.incendo.cloud.Command;
import org.incendo.cloud.exception.ArgumentParseException;
import org.incendo.cloud.exception.InvalidCommandSenderException;
import org.incendo.cloud.exception.InvalidSyntaxException;
import org.incendo.cloud.exception.NoPermissionException;
import org.incendo.cloud.execution.ExecutionCoordinator;
import org.incendo.cloud.key.CloudKey;
import org.incendo.cloud.minecraft.extras.MinecraftExceptionHandler;
import org.incendo.cloud.paper.PaperCommandManager;
import org.incendo.cloud.parser.flag.CommandFlag;

@SuppressWarnings("UnstableApiUsage")
public class CommandManager {

    public static final CloudKey<GemsEconomyPlugin> PLUGIN = CloudKey.of("gemseconomy:plugin", TypeToken.get(GemsEconomyPlugin.class));
    private static final Component NULL = Component.text("null");
    private static final Pattern SYNTAX_HIGHLIGHT_PATTERN = Pattern.compile("[^\\s\\w\\-]");
    private final PaperCommandManager<CommandSourceStack> manager;
    private final Map<String, CommandFlag.Builder<?, ?>> flagRegistry = new HashMap<>();

    public CommandManager(GemsEconomyPlugin plugin) throws Exception {
        manager = PaperCommandManager.builder()
                .executionCoordinator(
                        ExecutionCoordinator.<CommandSourceStack>builder()
                                .executor(HelperExecutors.asyncHelper())
                                .build()
                )
                .buildOnEnable(plugin);

        // ---- Inject instances into the command context ----
        manager.registerCommandPreProcessor(ctx -> ctx.commandContext().store(PLUGIN, plugin));

        // ---- Change default exception messages ----
        MinecraftExceptionHandler.create(CommandSourceStack::getSender)
                .handler(InvalidSyntaxException.class, (formatter, ctx) -> {
                    final InvalidSyntaxException exception = ctx.exception();
                    final Component correctSyntaxMessage = Component
                            .text("/%s".formatted(exception.correctSyntax()))
                            .color(NamedTextColor.GRAY)
                            .replaceText(config -> {
                                config.match(SYNTAX_HIGHLIGHT_PATTERN);
                                config.replacement(builder -> builder.color(NamedTextColor.WHITE));
                            });
                    return GemsEconomyPlugin.lang()
                            .component("err_invalid_syntax")
                            .replaceText(config -> {
                                config.matchLiteral("{syntax}");
                                config.replacement(correctSyntaxMessage);
                            });
                })
                .handler(InvalidCommandSenderException.class, (formatter, ctx) -> {
                    final InvalidCommandSenderException exception = ctx.exception();
                    final Component correctSenderType = Component
                            .text(exception.requiredSenderTypes().stream().map(Type::getTypeName).reduce((a, b) -> a + " or " + b).orElse("null"))
                            .color(NamedTextColor.GRAY);
                    return GemsEconomyPlugin.lang()
                            .component("err_invalid_sender")
                            .replaceText(config -> {
                                config.matchLiteral("{type}");
                                config.replacement(correctSenderType);
                            });
                })
                .handler(NoPermissionException.class, (formatter, ctx) -> {
                    final NoPermissionException exception = ctx.exception();
                    return GemsEconomyPlugin.lang()
                            .component("err_no_permission")
                            .replaceText(config -> {
                                config.matchLiteral("{permission}");
                                Component permission = Component
                                        .text(exception.missingPermission().permissionString())
                                        .color(NamedTextColor.YELLOW);
                                config.replacement(permission);
                            });
                })
                .handler(ArgumentParseException.class, (formatter, ctx) -> {
                    final ArgumentParseException exception = ctx.exception();
                    return GemsEconomyPlugin.lang()
                            .component("err_argument_parsing")
                            .replaceText(config -> {
                                config.matchLiteral("{args}");
                                config.replacement(getMessage(exception.getCause()).colorIfAbsent(NamedTextColor.GRAY));
                            });
                })
                .defaultCommandExecutionHandler()
                .registerTo(manager);

        // ---- Register all commands ----
        Stream.of(
                new InternalCommand(plugin, this),
                new BalanceCommand(plugin, this),
                new BalanceAccCommand(plugin, this),
                new BalanceTopCommand(plugin, this),
                new CurrencyCommand(plugin, this),
                new EconomyCommand(plugin, this),
                new PayCommand(plugin, this)
        ).forEach(AbstractCommand::register);
    }

    public CommandFlag.Builder<?, ?> getFlag(final String name) {
        return this.flagRegistry.get(name);
    }

    public void registerFlag(final String name, final CommandFlag.Builder<?, ?> flagBuilder) {
        this.flagRegistry.put(name, flagBuilder);
    }

    public void register(final List<Command<CommandSourceStack>> commands) {
        commands.forEach(manager::command);
    }

    public PaperCommandManager<CommandSourceStack> getCommandManager() {
        return manager;
    }

    private static Component getMessage(final Throwable throwable) {
        final Component msg = ComponentMessageThrowable.getOrConvertMessage(throwable);
        return msg == null ? NULL : msg;
    }

}

package me.xanium.gemseconomy.command;

import cloud.commandframework.Command;
import cloud.commandframework.arguments.flags.CommandFlag;
import cloud.commandframework.brigadier.CloudBrigadierManager;
import cloud.commandframework.bukkit.CloudBukkitCapabilities;
import cloud.commandframework.exceptions.ArgumentParseException;
import cloud.commandframework.exceptions.InvalidCommandSenderException;
import cloud.commandframework.exceptions.InvalidSyntaxException;
import cloud.commandframework.exceptions.NoPermissionException;
import cloud.commandframework.execution.AsynchronousCommandExecutionCoordinator;
import cloud.commandframework.keys.CloudKey;
import cloud.commandframework.keys.SimpleCloudKey;
import cloud.commandframework.minecraft.extras.AudienceProvider;
import cloud.commandframework.minecraft.extras.MinecraftExceptionHandler;
import cloud.commandframework.paper.PaperCommandManager;
import io.leangen.geantyref.TypeToken;
import me.lucko.helper.scheduler.HelperExecutors;
import me.xanium.gemseconomy.GemsEconomyPlugin;
import me.xanium.gemseconomy.command.command.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.util.ComponentMessageThrowable;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CommandManager extends PaperCommandManager<CommandSender> {

    public static final CloudKey<GemsEconomyPlugin> PLUGIN = SimpleCloudKey.of("gemseconomy:plugin", TypeToken.get(GemsEconomyPlugin.class));
    private static final Component NULL = Component.text("null");
    private static final Pattern SYNTAX_HIGHLIGHT_PATTERN = Pattern.compile("[^\\s\\w\\-]");
    private final Map<String, CommandFlag.Builder<?>> flagRegistry = new HashMap<>();

    public CommandManager(GemsEconomyPlugin plugin) throws Exception {
        super(
            plugin,
            AsynchronousCommandExecutionCoordinator.<CommandSender>builder()
                .withAsynchronousParsing()
                .withExecutor(HelperExecutors.asyncHelper()) // Reuse helper's executor
                .build(),
            Function.identity(),
            Function.identity()
        );

        // ---- Register Brigadier ----
        if (hasCapability(CloudBukkitCapabilities.NATIVE_BRIGADIER)) {
            registerBrigadier();
            final @Nullable CloudBrigadierManager<CommandSender, ?> brigManager = brigadierManager();
            if (brigManager != null) {
                brigManager.setNativeNumberSuggestions(false);
            }
            plugin.getLogger().info("Successfully registered Mojang Brigadier support for commands.");
        }

        // ---- Register Asynchronous Completion Listener ----
        if (hasCapability(CloudBukkitCapabilities.ASYNCHRONOUS_COMPLETION)) {
            registerAsynchronousCompletions();
            plugin.getLogger().info("Successfully registered asynchronous command completion listener.");
        }

        // ---- Inject instances into the command context ----
        this.registerCommandPreProcessor(ctx -> ctx.getCommandContext().store(PLUGIN, plugin));

        // ---- Change default exception messages ----
        new MinecraftExceptionHandler<CommandSender>()
            .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SYNTAX, e -> {
                final InvalidSyntaxException exception = (InvalidSyntaxException) e;
                final Component correctSyntaxMessage = Component
                    .text("/%s".formatted(exception.getCorrectSyntax()))
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
            .withHandler(MinecraftExceptionHandler.ExceptionType.INVALID_SENDER, e -> {
                final InvalidCommandSenderException exception = (InvalidCommandSenderException) e;
                final Component correctSenderType = Component
                    .text(exception.getRequiredSender().getSimpleName())
                    .color(NamedTextColor.GRAY);
                return GemsEconomyPlugin.lang()
                    .component("err_invalid_sender")
                    .replaceText(config -> {
                        config.matchLiteral("{type}");
                        config.replacement(correctSenderType);
                    });
            })
            .withHandler(MinecraftExceptionHandler.ExceptionType.NO_PERMISSION, e -> {
                final NoPermissionException exception = (NoPermissionException) e;
                return GemsEconomyPlugin.lang()
                    .component("err_no_permission")
                    .replaceText(config -> {
                        config.matchLiteral("{permission}");
                        Component permission = Component
                            .text(exception.getMissingPermission())
                            .color(NamedTextColor.YELLOW);
                        config.replacement(permission);
                    });
            })
            .withHandler(MinecraftExceptionHandler.ExceptionType.ARGUMENT_PARSING, e -> {
                final ArgumentParseException exception = (ArgumentParseException) e;
                return GemsEconomyPlugin.lang()
                    .component("err_argument_parsing")
                    .replaceText(config -> {
                        config.matchLiteral("{args}");
                        config.replacement(getMessage(exception.getCause()).colorIfAbsent(NamedTextColor.GRAY));
                    });
            })
            .withCommandExecutionHandler()
            .apply(this, AudienceProvider.nativeAudience());

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

    public CommandFlag.Builder<?> getFlag(final String name) {
        return this.flagRegistry.get(name);
    }

    public void registerFlag(final String name, final CommandFlag.Builder<?> flagBuilder) {
        this.flagRegistry.put(name, flagBuilder);
    }

    public void register(final List<Command<CommandSender>> commands) {
        commands.forEach(this::command);
    }

    private static Component getMessage(final Throwable throwable) {
        final Component msg = ComponentMessageThrowable.getOrConvertMessage(throwable);
        return msg == null ? NULL : msg;
    }

}

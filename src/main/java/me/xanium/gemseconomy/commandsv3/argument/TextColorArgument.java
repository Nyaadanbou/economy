package me.xanium.gemseconomy.commandsv3.argument;

import cloud.commandframework.ArgumentDescription;
import cloud.commandframework.arguments.CommandArgument;
import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.arguments.parser.ArgumentParser;
import cloud.commandframework.context.CommandContext;
import cloud.commandframework.exceptions.parsing.NoInputProvidedException;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.BiFunction;

@SuppressWarnings("unused")
@NonnullByDefault
public class TextColorArgument extends CommandArgument<CommandSender, TextColor> {

    public TextColorArgument(
            boolean required,
            String name,
            String defaultValue,
            @Nullable BiFunction<CommandContext<CommandSender>, String, List<String>> suggestionsProvider,
            ArgumentDescription defaultDescription) {
        super(required, name, new TextColorArgument.Parser(), defaultValue, TextColor.class, suggestionsProvider, defaultDescription);
    }

    public static TextColorArgument of(final String name) {
        return builder(name).build();
    }

    public static TextColorArgument optional(final String name) {
        return builder(name).asOptional().build();
    }

    public static TextColorArgument.Builder builder(final String name) {
        return new TextColorArgument.Builder(name);
    }

    public static final class Parser implements ArgumentParser<CommandSender, TextColor> {

        @Override
        public ArgumentParseResult<TextColor> parse(
                final CommandContext<CommandSender> commandContext,
                final Queue<String> inputQueue
        ) {
            String input = inputQueue.peek();
            if (input == null) {
                return ArgumentParseResult.failure(new NoInputProvidedException(TextColorArgument.Parser.class, commandContext));
            }
            if (NamedTextColor.NAMES.value(input) != null) { // Input is a NamedTextColor
                inputQueue.remove();
                return ArgumentParseResult.success(Objects.requireNonNull(NamedTextColor.NAMES.value(input)));
            } else if (input.length() == 6) { // Input is 6-digit hex value
                try {
                    int hex = HexFormat.fromHexDigits(input);
                    TextColor color = TextColor.color(hex);
                    inputQueue.remove();
                    return ArgumentParseResult.success(color);
                } catch (IllegalArgumentException e) {
                    return ArgumentParseResult.failure(new IllegalArgumentException("Your input 6-digit hex value is not in correct format"));
                }
            }
            return ArgumentParseResult.failure(new IllegalArgumentException("Your input must either be a 6-digit hex value or a NamedTextColor from TAB auto completions"));
        }

        @Override
        public List<String> suggestions(
                final CommandContext<CommandSender> commandContext,
                final String input
        ) {
            return NamedTextColor.NAMES.keys().stream().toList();
        }
    }

    public static final class Builder extends TypedBuilder<CommandSender, TextColor, TextColorArgument.Builder> {
        private Builder(final String name) {
            super(TextColor.class, name);
        }

        @Override
        public TextColorArgument build() {
            return new TextColorArgument(
                    this.isRequired(),
                    this.getName(),
                    this.getDefaultValue(),
                    this.getSuggestionsProvider(),
                    this.getDefaultDescription()
            );
        }
    }

}



package me.xanium.gemseconomy.command.argument;

import cloud.commandframework.arguments.parser.ArgumentParseResult;
import cloud.commandframework.context.CommandContext;
import me.lucko.helper.utils.annotation.NonnullByDefault;
import org.bukkit.command.CommandSender;

import java.util.Queue;
import java.util.function.BiFunction;

@NonnullByDefault
public final class PreprocessorUtil {

    public static BiFunction<
            CommandContext<CommandSender>, Queue<String>,
            ArgumentParseResult<Boolean>> currencyReferrerOf(String referrer) {
        return (context, queue) -> {
            context.set("currencyReferrer", referrer);
            return ArgumentParseResult.success(true);
        };
    }

}

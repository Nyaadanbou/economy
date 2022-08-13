package me.xanium.gemseconomy.commandsv2.argument;

import dev.jorel.commandapi.arguments.*;
import org.bukkit.entity.Player;

public final class BaseArguments {

    public static final Argument<String> CURRENCY = new StringArgument("货币").replaceSuggestions(BaseSuggestions.CURRENCY);

    public static final Argument<String> ACCOUNT = new StringArgument("账户").replaceSuggestions(BaseSuggestions.PLAYER);

    public static final Argument<Player> PLAYER = new EntitySelectorArgument<>("玩家", EntitySelector.MANY_PLAYERS);

    public static final Argument<String> AMOUNT = new StringArgument("数额").replaceSuggestions(ArgumentSuggestions.strings("1"));

    private BaseArguments() {
        throw new UnsupportedOperationException();
    }

}

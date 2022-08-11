package me.xanium.gemseconomy.commandsv2.argument;

import dev.jorel.commandapi.arguments.*;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.currency.Currency;
import me.xanium.gemseconomy.currency.CurrencyManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

public final class BaseArguments {

    public static final Argument<String> CURRENCY = new StringArgument("货币").replaceSuggestions(ArgumentSuggestions.strings(info -> {
        CurrencyManager currencyManager = GemsEconomy.inst().getCurrencyManager();
        List<String> singulars = currencyManager.getCurrencies().stream().map(Currency::getSingular).toList();
        List<String> completions = new ArrayList<>();
        return StringUtil.copyPartialMatches(info.currentArg(), singulars, completions).toArray(String[]::new);
    }));

    public static final Argument<String> ACCOUNT = new StringArgument("账户").replaceSuggestions(ArgumentSuggestions.strings(info -> {
        List<String> playerNames = Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList();
        List<String> completions = new ArrayList<>();
        return StringUtil.copyPartialMatches(info.currentArg(), playerNames, completions).toArray(String[]::new);
    }));

    public static final Argument<Player> PLAYER = new EntitySelectorArgument<>("玩家", EntitySelector.MANY_PLAYERS);

    public static final Argument<String> AMOUNT = new StringArgument("数额").replaceSuggestions(ArgumentSuggestions.strings("1"));

    private BaseArguments() {
        throw new UnsupportedOperationException();
    }

}

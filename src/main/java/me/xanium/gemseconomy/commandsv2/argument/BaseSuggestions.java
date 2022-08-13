package me.xanium.gemseconomy.commandsv2.argument;

import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import me.xanium.gemseconomy.GemsEconomy;
import me.xanium.gemseconomy.currency.Currency;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;

public class BaseSuggestions {

    public static final ArgumentSuggestions CURRENCY = ArgumentSuggestions.stringsAsync(info -> CompletableFuture.supplyAsync(() -> StringUtil.copyPartialMatches(
            info.currentArg(), GemsEconomy.inst().getCurrencyManager().getCurrencies().stream().map(Currency::getSingular).toList(), new ArrayList<>()).toArray(String[]::new))
    );

    public static final ArgumentSuggestions PLAYER = ArgumentSuggestions.stringsAsync(info -> CompletableFuture.supplyAsync(() -> StringUtil.copyPartialMatches(
            info.currentArg(), Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).toList(), new ArrayList<>()).toArray(String[]::new))
    );

}

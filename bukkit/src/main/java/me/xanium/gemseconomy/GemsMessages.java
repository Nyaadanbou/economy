package me.xanium.gemseconomy;

import de.themoep.utils.lang.bukkit.LanguageManager;
import me.xanium.gemseconomy.account.Account;
import me.xanium.gemseconomy.currency.Currency;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class GemsMessages {

    public static final Function<Boolean, Consumer<TextReplacementConfig.Builder>> STATUS_REPLACEMENT = bool -> config -> {
        config.matchLiteral("{status}");
        config.replacement(bool
            ? GemsEconomy.lang().component("msg_enabled")
            : GemsEconomy.lang().component("msg_disabled")
        );
    };
    public static final Function<Account, Consumer<TextReplacementConfig.Builder>> ACCOUNT_REPLACEMENT = account -> config -> {
        config.matchLiteral("{account}");
        config.replacement(account.getNickname());
    };
    public static final Function<Currency, Consumer<TextReplacementConfig.Builder>> CURRENCY_REPLACEMENT = currency -> config -> {
        config.matchLiteral("{currency}");
        config.replacement(MiniMessage.miniMessage().deserialize(currency.getDisplayName()).color(currency.getColor()));
    };
    public static final BiFunction<Currency, Double, Consumer<TextReplacementConfig.Builder>> AMOUNT_REPLACEMENT = (currency, amount) -> config -> {
        config.matchLiteral("{amount}");
        config.replacement(MiniMessage.miniMessage().deserialize(currency.fancyFormat(amount)).color(currency.getColor()));
    };

    private final LanguageManager lang;

    public GemsMessages(JavaPlugin plugin) {
        this.lang = new LanguageManager(plugin, "languages", "zh");
        this.lang.setPlaceholderPrefix("{");
        this.lang.setPlaceholderSuffix("}");
    }

    public String raw(CommandSender sender, String key, String... subst) {
        if (subst.length == 0) {
            return this.lang.getConfig(sender).get(key);
        } else {
            return this.lang.getConfig(sender).get(key, subst);
        }
    }

    public String raw(String key, String... subst) {
        return raw(null, key, subst);
    }

    public String legacy(CommandSender sender, String key, String... subst) {
        return LegacyComponentSerializer.legacySection().serialize(component(sender, key, subst));
    }

    public String legacy(String key, String... subst) {
        return legacy(null, key, subst);
    }

    public Component component(CommandSender sender, String key, String... subst) {
        return MiniMessage.miniMessage().deserialize(raw(sender, key, subst));
    }

    public Component component(String key, String... subst) {
        return component(null, key, subst);
    }

    public void sendComponent(CommandSender sender, String key, String... subst) {
        sender.sendMessage(component(sender, key, subst));
    }

    public void sendComponent(CommandSender sender, Component component) {
        sender.sendMessage(component);
    }

}

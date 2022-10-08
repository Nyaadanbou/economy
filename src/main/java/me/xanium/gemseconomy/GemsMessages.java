package me.xanium.gemseconomy;

import de.themoep.utils.lang.bukkit.LanguageManager;
import me.xanium.gemseconomy.currency.Currency;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

@SuppressWarnings("unused")
public class GemsMessages {

    public static final Function<Boolean, Consumer<TextReplacementConfig.Builder>> STATUS_REPLACEMENT = (bool) -> config -> {
        config.matchLiteral("{status}");
        config.replacement(bool ? GemsEconomy.lang().component("msg_enabled") : GemsEconomy.lang().component("msg_disabled"));
    };
    public static final Function<String, Consumer<TextReplacementConfig.Builder>> ACCOUNT_REPLACEMENT = (name) -> config -> {
        config.matchLiteral("{account}");
        config.replacement(name);
    };
    public static final Function<Component, Consumer<TextReplacementConfig.Builder>> CURRENCY_REPLACEMENT = (format) -> config -> {
        config.matchLiteral("{currency}");
        config.replacement(format);
    };
    public static final BiFunction<Currency, Double, Consumer<TextReplacementConfig.Builder>> AMOUNT_REPLACEMENT = (currency, amount) -> config -> {
        config.matchLiteral("{amount}");
        config.replacement(currency.componentFormat(amount));
    };

    private final LanguageManager lang;

    public GemsMessages(JavaPlugin plugin) {
        this.lang = new LanguageManager(plugin, "languages", "zh");
        this.lang.setPlaceholderPrefix("{");
        this.lang.setPlaceholderSuffix("}");
    }

    public LanguageManager internal() {
        return lang;
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
        return ChatColor.translateAlternateColorCodes('&', raw(sender, key, subst));
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

    public String toLegacy(CommandSender sender, String key, String... subst) {
        return LegacyComponentSerializer.legacyAmpersand().serialize(component(sender, key, subst));
    }

    public String toLegacy(String key, String... subst) {
        return toLegacy(null, key, subst);
    }

    public void sendComponent(CommandSender sender, String key, String... subst) {
        Audience audience = GemsEconomy.inst().getAdventure().sender(sender);
        Component component = component(sender, key, subst);
        audience.sendMessage(component);
    }

    public void sendComponent(CommandSender sender, Component component) {
        Audience audience = GemsEconomy.inst().getAdventure().sender(sender);
        audience.sendMessage(component);
    }

    public void sendActionBar(CommandSender sender, String key, String... subst) {
        Audience audience = GemsEconomy.inst().getAdventure().sender(sender);
        Component component = component(sender, key, subst);
        audience.sendActionBar(component);
    }

    public void sendActionBar(CommandSender sender, Component component) {
        Audience audience = GemsEconomy.inst().getAdventure().sender(sender);
        audience.sendActionBar(component);
    }

    public void sendLegacy(CommandSender sender, String key, String... subst) {
        String legacy = legacy(sender, key, subst);
        sender.sendMessage(legacy);
    }

    public void sendRaw(CommandSender sender, String key, String... subst) {
        String raw = raw(sender, key, subst);
        sender.sendMessage(raw);
    }

}

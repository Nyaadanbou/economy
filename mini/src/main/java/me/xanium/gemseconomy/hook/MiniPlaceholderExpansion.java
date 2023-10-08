package me.xanium.gemseconomy.hook;

import io.github.miniplaceholders.api.Expansion;
import me.lucko.helper.terminable.Terminable;
import me.xanium.gemseconomy.api.Account;
import me.xanium.gemseconomy.api.Currency;
import me.xanium.gemseconomy.api.GemsEconomy;
import me.xanium.gemseconomy.api.GemsEconomyProvider;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

import org.checkerframework.checker.nullness.qual.Nullable;

public class MiniPlaceholderExpansion implements Terminable {
    private final Expansion expansion;
    private final GemsEconomy economy;

    public MiniPlaceholderExpansion() {
        economy = GemsEconomyProvider.get();
        expansion = Expansion.builder("econ")
                .audiencePlaceholder("balance", (audience, queue, ctx) -> {
                    // <econ_balance:r>
                    // <econ_balance:c>
                    // <econ_balance:c:acc>
                    return parse(audience, queue, ctx, (currency, number) -> Tag.preProcessParsed(String.valueOf(number)));
                })
                .audiencePlaceholder("simple_balance", (audience, queue, ctx) -> {
                    // <econ_simple_balance:r>
                    // <econ_simple_balance:c>
                    // <econ_simple_balance:c:acc>
                    return parse(audience, queue, ctx, (currency, number) -> Tag.preProcessParsed(currency.simpleFormat(number)));
                })
                .audiencePlaceholder("fancy_balance", (audience, queue, ctx) -> {
                    // <econ_fancy_balance:r>
                    // <econ_fancy_balance:c>
                    // <econ_fancy_balance:c:acc>
                    return parse(audience, queue, ctx, (currency, number) -> Tag.selfClosingInserting(MiniMessage.miniMessage().deserialize(currency.fancyFormat(number))));
                })
                .build();
    }

    private @Nullable Tag parse(
            Audience audience,
            ArgumentQueue queue,
            Context ctx,
            BiFunction<Currency, Double, Tag> balanceStringFunc
    ) {
        Optional<UUID> optional = audience.get(Identity.UUID);
        if (optional.isEmpty()) {
            return null;
        }

        UUID uuid = optional.get();
        Account account = economy.getAccount(uuid);
        if (account == null) {
            return null;
        }

        Currency currency = economy.getCurrency(queue.popOr("missing argument: currency").value());
        if (currency == null) {
            return null;
        }

        Tag.Argument peek = queue.peek();
        if (peek == null) {
            return balanceStringFunc.apply(currency, account.getBalance(currency));
        } else if (peek.value().equals("heap")) {
            return balanceStringFunc.apply(currency, account.getHeapBalance(currency));
        } else {
            throw ctx.newException("unrecognized argument: %s".formatted(peek.value()), queue);
        }
    }

    public void register() {
        expansion.register();
    }

    @Override public void close() {
        expansion.unregister();
    }
}

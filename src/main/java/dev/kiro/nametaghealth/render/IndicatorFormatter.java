package dev.kiro.nametaghealth.render;

import dev.kiro.nametaghealth.config.EffectStyle;
import dev.kiro.nametaghealth.config.NametagHealthConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;

import java.util.Locale;

/**
 * Builds the indicator text from a token template.
 *
 * <p>The template is split on whitespace and each word rendered independently. A word containing
 * only tokens that resolve to nothing is dropped entirely, which is what keeps optional pieces like
 * {@code {abs}} or {@code {ping}} from leaving double spaces behind when they are absent. Words are
 * then rejoined with single spaces.
 */
@Environment(EnvType.CLIENT)
public final class IndicatorFormatter {
    private static final String[] KNOWN_TOKENS =
            {"sym", "hp", "max", "frac", "pct", "bar", "abs", "armor", "fx", "ping", "delta"};

    private IndicatorFormatter() {
    }

    /** @return the indicator, or null when the template produced nothing at all. */
    public static MutableText build(NametagHealthConfig config, MinecraftClient client, Entity entity,
                                    LivingEntity living, HealthSamples.Sample sample, long now) {
        float max = living.getMaxHealth();
        float shown = Math.max(0.0F, Math.min(sample.displayHealth(), max));
        float ratio = max > 0.0F ? shown / max : 0.0F;

        int color = ColorPalettes.resolve(config, ratio, now);
        Style accent = styled(Style.EMPTY.withColor(TextColor.fromRgb(color)), config);
        Style value = config.colorSymbolOnly
                ? styled(Style.EMPTY.withColor(Formatting.WHITE), config)
                : accent;
        Style muted = styled(Style.EMPTY.withColor(Formatting.GRAY), config);

        String template = config.formatTemplate.isBlank() ? defaultTemplate(config) : config.formatTemplate;

        Context context = new Context(config, accent, value, muted, shown, max, ratio,
                Math.max(0.0F, living.getAbsorptionAmount()),
                living.getArmor(),
                template.contains("{fx}") ? effects(config, living) : null,
                template.contains("{ping}") ? ping(client, entity) : null,
                HealthSamples.recentDelta(sample, config, now));

        return assemble(template, context);
    }

    /**
     * Same renderer, driven by plain numbers instead of an entity, for the settings screen preview.
     * Status effects and ping have no meaning without an entity, so those tokens resolve to nothing.
     */
    public static MutableText preview(NametagHealthConfig config, float shown, float max,
                                      float absorption, int armor, float delta, long now) {
        float ratio = max > 0.0F ? shown / max : 0.0F;
        int color = ColorPalettes.resolve(config, ratio, now);
        Style accent = styled(Style.EMPTY.withColor(TextColor.fromRgb(color)), config);
        Style value = config.colorSymbolOnly
                ? styled(Style.EMPTY.withColor(Formatting.WHITE), config)
                : accent;
        Style muted = styled(Style.EMPTY.withColor(Formatting.GRAY), config);

        String template = config.formatTemplate.isBlank() ? defaultTemplate(config) : config.formatTemplate;
        Context context = new Context(config, accent, value, muted, shown, max, ratio,
                absorption, armor, null, null, delta);
        return assemble(template, context);
    }

    private static MutableText assemble(String template, Context context) {
        NametagHealthConfig config = context.config();
        Style muted = context.muted();

        MutableText out = Text.empty();
        boolean any = false;
        for (String word : template.trim().split("\\s+")) {
            MutableText rendered = renderWord(word, context);
            if (rendered == null) {
                continue;
            }
            if (any) {
                out.append(Text.literal(" "));
            }
            out.append(rendered);
            any = true;
        }
        if (!any) {
            return null;
        }

        if (!config.wrapInBrackets) {
            return out;
        }
        return Text.empty()
                .append(Text.literal("[").setStyle(muted))
                .append(out)
                .append(Text.literal("]").setStyle(muted));
    }

    private static String defaultTemplate(NametagHealthConfig config) {
        StringBuilder template = new StringBuilder(switch (config.style) {
            case HEARTS -> "{sym} {hp}";
            case NUMBER -> "{hp}";
            case FRACTION -> "{hp}/{max}";
            case PERCENT -> "{pct}";
            case BAR -> "{bar}";
        });
        if (config.showAbsorption) template.append(" {abs}");
        if (config.showArmor) template.append(" {armor}");
        if (config.showEffects) template.append(" {fx}");
        if (config.showPing) template.append(" {ping}");
        if (config.showDelta) template.append(" {delta}");
        return template.toString();
    }

    /** @return null when every token in the word resolved to nothing. */
    private static MutableText renderWord(String word, Context context) {
        MutableText out = Text.empty();
        StringBuilder literal = new StringBuilder();
        int tokens = 0;
        int resolved = 0;

        int index = 0;
        while (index < word.length()) {
            char current = word.charAt(index);
            int close = current == '{' ? word.indexOf('}', index) : -1;
            String name = close > index ? word.substring(index + 1, close).toLowerCase(Locale.ROOT) : null;

            if (name != null && known(name)) {
                tokens++;
                MutableText piece = resolve(name, context);
                if (piece != null) {
                    resolved++;
                    flush(literal, out, context.muted());
                    out.append(piece);
                }
                index = close + 1;
                continue;
            }

            // Unknown tokens are left visible rather than silently swallowed, so typos are obvious.
            literal.append(current);
            index++;
        }
        flush(literal, out, context.muted());

        return tokens > 0 && resolved == 0 ? null : out;
    }

    private static void flush(StringBuilder literal, MutableText out, Style style) {
        if (literal.length() > 0) {
            out.append(Text.literal(literal.toString()).setStyle(style));
            literal.setLength(0);
        }
    }

    private static boolean known(String name) {
        for (String token : KNOWN_TOKENS) {
            if (token.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static MutableText resolve(String name, Context context) {
        NametagHealthConfig config = context.config();
        return switch (name) {
            case "sym" -> config.symbol.isEmpty()
                    ? null
                    : Text.literal(config.symbol).setStyle(context.accent());
            case "hp" -> Text.literal(number(context.shown(), config)).setStyle(context.value());
            case "max" -> Text.literal(number(context.max(), config)).setStyle(context.muted());
            case "frac" -> Text.empty()
                    .append(Text.literal(number(context.shown(), config)).setStyle(context.value()))
                    .append(Text.literal("/" + number(context.max(), config)).setStyle(context.muted()));
            case "pct" -> Text.literal(Math.round(context.ratio() * 100.0F) + "%").setStyle(context.value());
            case "bar" -> bar(context);
            case "abs" -> context.absorption() <= 0.0F ? null
                    : Text.literal("+" + number(context.absorption(), config))
                    .setStyle(styled(Style.EMPTY.withColor(TextColor.fromRgb(config.absorptionColor)), config));
            case "armor" -> context.armor() <= 0 ? null
                    : Text.literal(config.armorSymbol + context.armor())
                    .setStyle(styled(Style.EMPTY.withColor(TextColor.fromRgb(0xC6C6C6)), config));
            case "fx" -> context.effects();
            case "ping" -> context.ping();
            case "delta" -> delta(context);
            default -> null;
        };
    }

    private static MutableText bar(Context context) {
        NametagHealthConfig config = context.config();
        int segments = config.barSegments;
        int filled = Math.round(context.ratio() * segments);
        // Anything still alive keeps at least one visible segment.
        if (filled == 0 && context.ratio() > 0.0F) {
            filled = 1;
        }
        filled = Math.max(0, Math.min(segments, filled));

        MutableText out = Text.empty();
        if (filled > 0) {
            out.append(Text.literal(config.barFilled.repeat(filled)).setStyle(context.accent()));
        }
        if (filled < segments) {
            out.append(Text.literal(config.barEmpty.repeat(segments - filled)).setStyle(context.muted()));
        }
        return out;
    }

    private static MutableText delta(Context context) {
        float change = context.delta();
        if (change == 0.0F) {
            return null;
        }
        boolean healed = change > 0.0F;
        String text = (healed ? "+" : "-") + number(Math.abs(change), context.config());
        int color = healed ? 0x55FF55 : 0xFF5555;
        return Text.literal(text).setStyle(styled(Style.EMPTY.withColor(TextColor.fromRgb(color)), context.config()));
    }

    /**
     * Driven by the synced swirl particles rather than {@code getStatusEffects()}, which is always
     * empty for a remote entity. That means a count, not names — see {@link EffectMarkers}.
     */
    private static MutableText effects(NametagHealthConfig config, LivingEntity living) {
        int count = EffectMarkers.count(living);
        if (count == 0) {
            return null;
        }

        Style style = styled(Style.EMPTY.withColor(TextColor.fromRgb(config.effectColor)), config);
        if (config.effectStyle == EffectStyle.COUNT) {
            return Text.literal(config.effectSymbol + count).setStyle(style);
        }
        return Text.literal(config.effectSymbol.repeat(Math.min(count, config.maxEffectsShown)))
                .setStyle(style);
    }

    private static MutableText ping(MinecraftClient client, Entity entity) {
        if (!(entity instanceof PlayerEntity player)) {
            return null;
        }
        ClientPlayNetworkHandler handler = client.getNetworkHandler();
        if (handler == null) {
            return null;
        }
        PlayerListEntry listEntry = handler.getPlayerListEntry(player.getUuid());
        if (listEntry == null) {
            return null;
        }
        int latency = listEntry.getLatency();
        int color = latency <= 100 ? 0x55FF55 : latency <= 250 ? 0xFFFF55 : 0xFF5555;
        return Text.literal(latency + "ms").setStyle(Style.EMPTY.withColor(TextColor.fromRgb(color)));
    }

    private static String number(float raw, NametagHealthConfig config) {
        if (config.abbreviateLargeNumbers && raw >= config.abbreviateAbove) {
            if (raw >= 1_000_000.0F) {
                return compact(raw / 1_000_000.0F) + "M";
            }
            return compact(raw / 1_000.0F) + "k";
        }
        if (config.decimals <= 0) {
            return Integer.toString(Math.round(raw));
        }
        return String.format(Locale.ROOT, "%." + config.decimals + "f", raw);
    }

    /** One decimal place, with a trailing {@code .0} trimmed: 1.2k but 5k rather than 5.0k. */
    private static String compact(float value) {
        String text = String.format(Locale.ROOT, "%.1f", value);
        return text.endsWith(".0") ? text.substring(0, text.length() - 2) : text;
    }

    private static Style styled(Style base, NametagHealthConfig config) {
        Style style = base;
        if (config.bold) {
            style = style.withBold(true);
        }
        if (config.italic) {
            style = style.withItalic(true);
        }
        if (config.underline) {
            style = style.withUnderline(true);
        }
        return style;
    }

    /** Everything a token needs, resolved once per rebuild. */
    private record Context(NametagHealthConfig config, Style accent, Style value, Style muted,
                           float shown, float max, float ratio, float absorption, int armor,
                           MutableText effects, MutableText ping, float delta) {
    }
}

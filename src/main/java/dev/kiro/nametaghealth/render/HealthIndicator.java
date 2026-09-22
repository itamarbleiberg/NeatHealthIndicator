package dev.kiro.nametaghealth.render;

import dev.kiro.nametaghealth.NametagHealth;
import dev.kiro.nametaghealth.config.ConfigManager;
import dev.kiro.nametaghealth.config.NametagHealthConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
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
 * Builds the health suffix that gets appended to an entity's in-world name label.
 *
 * <p>This is deliberately pure text work: no matrices, no vertex consumers, no render pipeline. The
 * vanilla nametag renderer draws whatever {@code Text} it is handed, so styling it here keeps the mod
 * compatible with resource packs, other nametag mods, and future rendering changes.
 */
@Environment(EnvType.CLIENT)
public final class HealthIndicator {
    private static final int GREEN = 0x55FF55;
    private static final int YELLOW = 0xFFFF55;
    private static final int RED = 0xFF5555;

    private static boolean reportedFailure = false;

    private HealthIndicator() {
    }

    /**
     * @return the original label with a health indicator appended, or the label untouched when the
     *         indicator is disabled or does not apply to this entity.
     */
    public static Text decorate(Entity entity, Text label) {
        // This runs inside the entity render loop, so never let a mistake here take down rendering.
        try {
            return decorateChecked(entity, label);
        } catch (Throwable t) {
            if (!reportedFailure) {
                reportedFailure = true;
                NametagHealth.LOGGER.error("Failed to build a health indicator; leaving nametags alone.", t);
            }
            return label;
        }
    }

    private static Text decorateChecked(Entity entity, Text label) {
        NametagHealthConfig config = ConfigManager.get();
        if (!config.enabled || label == null) {
            return label;
        }
        if (!(entity instanceof LivingEntity living)) {
            return label;
        }
        if (entity instanceof PlayerEntity ? !config.showOnPlayers : !config.showOnMobs) {
            return label;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity self = client.player;
        if (self == null || entity == self || entity == client.getCameraEntity()) {
            return label;
        }

        double limit = (double) config.maxDistance * config.maxDistance;
        if (entity.squaredDistanceTo(self) > limit) {
            return label;
        }

        float max = living.getMaxHealth();
        if (max <= 0.0F) {
            return label;
        }
        float health = Math.max(0.0F, Math.min(living.getHealth(), max));
        float absorption = config.showAbsorption ? Math.max(0.0F, living.getAbsorptionAmount()) : 0.0F;

        if (config.hideWhenFull && health >= max && absorption <= 0.0F) {
            return label;
        }

        return Text.empty()
                .append(label)
                .append(Text.literal(" "))
                .append(build(config, health, max, absorption));
    }

    /** The indicator on its own, without the name. Also used to render the config screen preview. */
    public static MutableText build(NametagHealthConfig config, float health, float max, float absorption) {
        float ratio = max > 0.0F ? health / max : 0.0F;
        int color = resolveColor(config, ratio);
        Style accent = Style.EMPTY.withColor(TextColor.fromRgb(color));
        Style value = config.colorSymbolOnly ? Style.EMPTY.withColor(Formatting.WHITE) : accent;
        Style muted = Style.EMPTY.withColor(Formatting.GRAY);

        MutableText out = Text.empty();
        if (config.wrapInBrackets) {
            out.append(Text.literal("[").setStyle(muted));
        }

        switch (config.style) {
            case HEARTS -> {
                if (!config.symbol.isEmpty()) {
                    out.append(Text.literal(config.symbol).setStyle(accent));
                    out.append(Text.literal(" "));
                }
                out.append(Text.literal(format(health, config.decimals)).setStyle(value));
            }
            case NUMBER -> out.append(Text.literal(format(health, config.decimals)).setStyle(value));
            case FRACTION -> {
                out.append(Text.literal(format(health, config.decimals)).setStyle(value));
                // The maximum never has a fractional part worth showing, so keep it whole.
                out.append(Text.literal("/" + format(max, 0)).setStyle(muted));
            }
            case PERCENT -> out.append(Text.literal(Math.round(ratio * 100.0F) + "%").setStyle(value));
            case BAR -> out.append(bar(config, ratio, accent, muted));
        }

        if (absorption > 0.0F) {
            out.append(Text.literal(" +" + format(absorption, config.decimals))
                    .setStyle(Style.EMPTY.withColor(TextColor.fromRgb(config.absorptionColor))));
        }

        if (config.wrapInBrackets) {
            out.append(Text.literal("]").setStyle(muted));
        }
        return out;
    }

    private static MutableText bar(NametagHealthConfig config, float ratio, Style accent, Style muted) {
        int segments = config.barSegments;
        int filled = Math.round(ratio * segments);
        // Anything still alive should keep at least one visible segment.
        if (filled == 0 && ratio > 0.0F) {
            filled = 1;
        }
        filled = Math.max(0, Math.min(segments, filled));

        MutableText out = Text.empty();
        if (filled > 0) {
            out.append(Text.literal(config.barFilled.repeat(filled)).setStyle(accent));
        }
        if (filled < segments) {
            out.append(Text.literal(config.barEmpty.repeat(segments - filled)).setStyle(muted));
        }
        return out;
    }

    private static int resolveColor(NametagHealthConfig config, float ratio) {
        return switch (config.colorMode) {
            case STATIC -> config.staticColor;
            case THRESHOLDS -> ratio > 0.6F ? GREEN : ratio > 0.3F ? YELLOW : RED;
            case GRADIENT -> ratio <= 0.5F
                    ? lerpColor(RED, YELLOW, ratio / 0.5F)
                    : lerpColor(YELLOW, GREEN, (ratio - 0.5F) / 0.5F);
        };
    }

    private static int lerpColor(int from, int to, float delta) {
        float t = Math.max(0.0F, Math.min(1.0F, delta));
        int r = Math.round((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * t);
        int g = Math.round((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * t);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return r << 16 | g << 8 | b;
    }

    private static String format(float value, int decimals) {
        if (decimals <= 0) {
            return Integer.toString(Math.round(value));
        }
        return String.format(Locale.ROOT, "%." + decimals + "f", value);
    }
}

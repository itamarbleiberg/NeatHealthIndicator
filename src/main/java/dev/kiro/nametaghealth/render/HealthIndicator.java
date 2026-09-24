package dev.kiro.nametaghealth.render;

import dev.kiro.nametaghealth.NametagHealth;
import dev.kiro.nametaghealth.config.ConfigManager;
import dev.kiro.nametaghealth.config.NametagHealthConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;

/**
 * Entry point from the mixin: decides whether to decorate a nametag, and stitches the indicator onto
 * it.
 *
 * <p>This is deliberately pure text work — no matrices, no vertex consumers, no render pipeline. The
 * vanilla nametag renderer draws whatever {@code Text} it is handed, so styling it here keeps the mod
 * compatible with resource packs, other nametag mods, and future rendering changes.
 */
@Environment(EnvType.CLIENT)
public final class HealthIndicator {
    private static boolean reportedFailure = false;

    private HealthIndicator() {
    }

    /**
     * @return the original label with a health indicator attached, or the label untouched when the
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
        if (!config.enabled || !config.toggledOn || label == null) {
            return label;
        }
        if (!(entity instanceof LivingEntity living)) {
            return label;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (!Visibility.shouldShow(config, client, entity, living)) {
            return label;
        }

        long now = System.currentTimeMillis();
        HealthSamples.Sample sample = HealthSamples.update(entity, living, config, now);

        float max = living.getMaxHealth();
        float absorption = Math.max(0.0F, living.getAbsorptionAmount());
        boolean changing = HealthSamples.deltaPending(sample, config, now);
        // Healing back to full still shows the "+n" for its hold window rather than vanishing mid-animation.
        if (config.hideWhenFull && living.getHealth() >= max && absorption <= 0.0F && !changing) {
            return label;
        }

        Text indicator = cached(config, client, entity, living, sample, now, changing);
        if (indicator == null) {
            return label;
        }

        return switch (config.placement) {
            case SUFFIX -> Text.empty().append(label).append(Text.literal(" ")).append(indicator);
            case PREFIX -> Text.empty().append(indicator).append(Text.literal(" ")).append(label);
            case REPLACE_NAME -> indicator;
        };
    }

    /**
     * Rebuilding touches the registries and allocates a small text tree, so it is throttled to
     * {@code updateIntervalMillis}. Anything mid-animation opts out of the cache, since a stale frame
     * would defeat the point.
     */
    private static Text cached(NametagHealthConfig config, MinecraftClient client, Entity entity,
                               LivingEntity living, HealthSamples.Sample sample, long now, boolean changing) {
        boolean animating = config.smoothingMillis > 0 || config.lowHealthPulse || changing;
        if (!animating && sample.cacheValid && now - sample.cachedAtMillis < config.updateIntervalMillis) {
            return sample.cachedText;
        }

        MutableText built = IndicatorFormatter.build(config, client, entity, living, sample, now);
        sample.cachedText = built;
        sample.cachedAtMillis = now;
        sample.cacheValid = true;
        return built;
    }
}

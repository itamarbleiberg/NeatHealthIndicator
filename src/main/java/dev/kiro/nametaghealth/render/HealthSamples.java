package dev.kiro.nametaghealth.render;

import dev.kiro.nametaghealth.config.NametagHealthConfig;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.text.Text;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Remembers a little history per entity, which is what lets the indicator animate and report
 * changes rather than only ever showing an instantaneous number.
 *
 * <p>Entries are weakly keyed, so an entity leaving render distance or unloading takes its sample
 * with it and nothing needs to be pruned on a timer. Only ever touched from the render thread.
 */
@Environment(EnvType.CLIENT)
public final class HealthSamples {
    /** Mutable per-entity state. Package-private fields: this is a record with a lifecycle. */
    public static final class Sample {
        private boolean seen;
        private float lastHealth;
        private float displayHealth;
        private float pendingDelta;
        private long deltaAtMillis;
        private long steppedAtMillis;

        Text cachedText;
        long cachedAtMillis;
        boolean cacheValid;

        /** Health to render, which trails the real value when smoothing is on. */
        public float displayHealth() {
            return this.displayHealth;
        }
    }

    private static final Map<Entity, Sample> SAMPLES = new WeakHashMap<>();

    private HealthSamples() {
    }

    /** Advances the entity's sample to {@code now} and returns it. */
    public static Sample update(Entity entity, LivingEntity living, NametagHealthConfig config, long now) {
        Sample sample = SAMPLES.computeIfAbsent(entity, ignored -> new Sample());

        float max = living.getMaxHealth();
        float real = Math.max(0.0F, Math.min(living.getHealth(), max));

        if (!sample.seen) {
            sample.seen = true;
            sample.lastHealth = real;
            sample.displayHealth = real;
            sample.steppedAtMillis = now;
            return sample;
        }

        if (real != sample.lastHealth) {
            float change = real - sample.lastHealth;
            boolean continuing = now - sample.deltaAtMillis <= config.deltaHoldMillis
                    && Math.signum(change) == Math.signum(sample.pendingDelta);
            // A flurry of hits reads better as one running total than as a flickering last-hit value.
            sample.pendingDelta = continuing ? sample.pendingDelta + change : change;
            sample.deltaAtMillis = now;
            sample.lastHealth = real;
            sample.cacheValid = false;
        }

        if (config.smoothingMillis <= 0) {
            sample.displayHealth = real;
        } else if (sample.displayHealth != real) {
            // smoothingMillis is the time to travel a full bar, so the rate is independent of max health.
            long elapsed = Math.max(0L, now - sample.steppedAtMillis);
            float step = max * elapsed / config.smoothingMillis;
            float gap = real - sample.displayHealth;
            sample.displayHealth = Math.abs(gap) <= step
                    ? real
                    : sample.displayHealth + Math.copySign(step, gap);
            sample.cacheValid = false;
        }
        sample.steppedAtMillis = now;

        return sample;
    }

    /** The recent health change, or 0 once the hold window has lapsed. */
    public static float recentDelta(Sample sample, NametagHealthConfig config, long now) {
        if (sample.pendingDelta == 0.0F || now - sample.deltaAtMillis > config.deltaHoldMillis) {
            return 0.0F;
        }
        return sample.pendingDelta;
    }

    /** True while a delta is still on screen, so the cache knows it has to keep refreshing. */
    public static boolean deltaPending(Sample sample, NametagHealthConfig config, long now) {
        return recentDelta(sample, config, now) != 0.0F;
    }

    /** Dropped when the config changes, so edits in the settings screen take effect at once. */
    public static void invalidateAll() {
        for (Sample sample : SAMPLES.values()) {
            sample.cacheValid = false;
        }
    }
}

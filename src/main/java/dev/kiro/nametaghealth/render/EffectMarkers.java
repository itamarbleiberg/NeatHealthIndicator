package dev.kiro.nametaghealth.render;

import dev.kiro.nametaghealth.mixin.LivingEntityAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.particle.ParticleEffect;

import java.util.List;

/**
 * Counts the status effects on an entity using the only data a client is given: the swirl particles.
 *
 * <p>This cannot name the effects. Which effects an entity has is server-side state, so a client-only
 * mod can report that an entity is affected and roughly by how many things, but not that it is
 * specifically Strength II. Effects hidden from particles — ambient beacon effects, or anything
 * applied with {@code hideParticles} — contribute nothing here, exactly as they show nothing in world.
 */
@Environment(EnvType.CLIENT)
public final class EffectMarkers {
    private EffectMarkers() {
    }

    /** @return the swirl particles currently synced for this entity, never null. */
    public static List<ParticleEffect> swirls(LivingEntity living) {
        try {
            return living.getDataTracker().get(LivingEntityAccessor.nametagHealth$potionSwirls());
        } catch (Throwable t) {
            // A mapping or accessor problem should degrade to "no markers", not break every nametag.
            return List.of();
        }
    }

    public static int count(LivingEntity living) {
        return swirls(living).size();
    }
}

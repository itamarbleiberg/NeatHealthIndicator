package dev.kiro.nametaghealth.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

/**
 * Exposes the one piece of status-effect state the server actually sends to clients.
 *
 * <p>{@code LivingEntity.activeStatusEffects} is a plain map that only ever exists server-side —
 * effect packets go to the affected player alone, never to nearby clients — so
 * {@code getStatusEffects()} is always empty for someone else's entity. {@code POTION_SWIRLS} is the
 * tracked counterpart that drives the visible swirl particles, and it is private, hence this accessor.
 */
@Mixin(LivingEntity.class)
public interface LivingEntityAccessor {
    @Accessor("POTION_SWIRLS")
    static TrackedData<List<ParticleEffect>> nametagHealth$potionSwirls() {
        throw new AssertionError("mixin accessor was not applied");
    }
}

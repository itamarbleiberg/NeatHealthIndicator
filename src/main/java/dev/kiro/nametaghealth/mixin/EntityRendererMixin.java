package dev.kiro.nametaghealth.mixin;

import dev.kiro.nametaghealth.render.HealthIndicator;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Rewrites the name label on the entity render state rather than hooking the nametag draw call.
 *
 * <p>Two reasons for that choice: the render state is built once per entity per frame with the entity
 * still in hand (so health is trivially available), and it sidesteps the submit-based render pipeline
 * introduced in 1.21.9 entirely. It also means the change is scoped to the in-world label — chat, the
 * tab list and death messages keep the unmodified name.
 */
@Environment(EnvType.CLIENT)
@Mixin(EntityRenderer.class)
public class EntityRendererMixin {
    @Inject(method = "updateRenderState", at = @At("TAIL"))
    private void nametagHealth$appendHealth(Entity entity, EntityRenderState state, float tickDelta, CallbackInfo ci) {
        if (state.nameLabel != null) {
            state.nameLabel = HealthIndicator.decorate(entity, state.nameLabel);
        }
    }
}

package dev.kiro.nametaghealth.render;

import dev.kiro.nametaghealth.config.FilterMode;
import dev.kiro.nametaghealth.config.NametagHealthConfig;
import dev.kiro.nametaghealth.config.TeamFilter;
import dev.kiro.nametaghealth.input.Keybinds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;

import java.util.List;
import java.util.Locale;

/**
 * Decides whether a given entity earns an indicator this frame.
 *
 * <p>Checks are ordered by cost: field reads and arithmetic first, registry lookups next, and the
 * line-of-sight raycast last, so the expensive test only runs for entities that passed everything
 * else.
 */
@Environment(EnvType.CLIENT)
public final class Visibility {
    private Visibility() {
    }

    public static boolean shouldShow(NametagHealthConfig config, MinecraftClient client,
                                     Entity entity, LivingEntity living) {
        PlayerEntity self = client.player;
        if (self == null) {
            return false;
        }
        // You never see your own nametag, and decorating the camera entity would be noise.
        if (entity == self || entity == client.getCameraEntity()) {
            return false;
        }
        if (entity instanceof PlayerEntity ? !config.showOnPlayers : !config.showOnMobs) {
            return false;
        }
        if (config.respectHudHidden && client.options != null && client.options.hudHidden) {
            return false;
        }
        if (config.hideForInvisible && entity.isInvisible()) {
            return false;
        }

        double limit = (double) config.maxDistance * config.maxDistance;
        if (entity.squaredDistanceTo(self) > limit) {
            return false;
        }

        float max = living.getMaxHealth();
        if (max <= 0.0F || max < config.minMaxHealth || max > config.maxMaxHealth) {
            return false;
        }

        if (config.teamFilter != TeamFilter.ALL) {
            boolean teammate = self.isTeammate(entity);
            if (config.teamFilter == TeamFilter.TEAMMATES_ONLY && !teammate) {
                return false;
            }
            if (config.teamFilter == TeamFilter.ENEMIES_ONLY && teammate) {
                return false;
            }
        }

        if (config.entityFilterMode != FilterMode.OFF && !config.entityFilter.isEmpty()) {
            boolean listed = matches(config.entityFilter, Registries.ENTITY_TYPE.getId(entity.getType()));
            if (config.entityFilterMode == FilterMode.WHITELIST && !listed) {
                return false;
            }
            if (config.entityFilterMode == FilterMode.BLACKLIST && listed) {
                return false;
            }
        }

        if (!revealed(config, client, entity)) {
            return false;
        }

        return !config.requireLineOfSight || self.canSee(entity);
    }

    private static boolean revealed(NametagHealthConfig config, MinecraftClient client, Entity entity) {
        return switch (config.revealMode) {
            case ALWAYS -> true;
            case WHILE_KEY_HELD -> Keybinds.revealHeld();
            case CROSSHAIR_TARGET -> client.crosshairTarget instanceof EntityHitResult hit
                    && hit.getEntity() == entity;
            case HOLDING_LISTED_ITEM -> holdingListedItem(config, client);
        };
    }

    private static boolean holdingListedItem(NametagHealthConfig config, MinecraftClient client) {
        PlayerEntity self = client.player;
        if (self == null || config.revealItems.isEmpty()) {
            return false;
        }
        ItemStack held = self.getMainHandStack();
        if (held.isEmpty()) {
            return false;
        }
        return matches(config.revealItems, Registries.ITEM.getId(held.getItem()));
    }

    /** Accepts either a full id ({@code minecraft:creeper}) or just the path ({@code creeper}). */
    private static boolean matches(List<String> entries, Identifier id) {
        if (id == null) {
            return false;
        }
        String full = id.toString();
        String path = id.getPath();
        for (String entry : entries) {
            if (entry == null) {
                continue;
            }
            String candidate = entry.trim().toLowerCase(Locale.ROOT);
            if (!candidate.isEmpty() && (candidate.equals(full) || candidate.equals(path))) {
                return true;
            }
        }
        return false;
    }
}

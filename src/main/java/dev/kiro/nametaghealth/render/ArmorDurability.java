package dev.kiro.nametaghealth.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * Finds the armour piece closest to breaking.
 *
 * <p>Unlike status effects, equipment genuinely is synced to nearby clients — it has to be, or nobody
 * could see what anyone is wearing — and the stacks arrive with their damage intact. So this needs no
 * server-side support.
 */
@Environment(EnvType.CLIENT)
public final class ArmorDurability {
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private ArmorDurability() {
    }

    /**
     * @param slot      which piece it was
     * @param remaining hits left before it breaks
     * @param max       the item's total durability
     */
    public record Worst(EquipmentSlot slot, int remaining, int max) {
        public float ratio() {
            return this.max <= 0 ? 0.0F : (float) this.remaining / this.max;
        }

    }

    /**
     * @return the armour piece with the lowest fraction of durability left, or null when the entity
     *         wears nothing damageable. Compared by fraction rather than absolute hits, so a nearly
     *         spent helmet outranks a chestplate that merely has more total durability.
     */
    public static Worst worst(LivingEntity living) {
        Worst worst = null;
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            ItemStack stack = living.getEquippedStack(slot);
            if (stack.isEmpty() || !stack.isDamageable()) {
                continue;
            }
            int max = stack.getMaxDamage();
            if (max <= 0) {
                continue;
            }
            Worst candidate = new Worst(slot, Math.max(0, max - stack.getDamage()), max);
            if (worst == null || candidate.ratio() < worst.ratio()) {
                worst = candidate;
            }
        }
        return worst;
    }
}

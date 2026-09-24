package dev.kiro.nametaghealth.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Locale;
import java.util.Map;

/**
 * Compact letters and colours for status effects.
 *
 * <p>Looked up by registry id path, which means modded effects work too: anything unrecognised falls
 * back to its own first letter in white rather than being dropped.
 */
@Environment(EnvType.CLIENT)
public final class StatusEffectGlyphs {
    /** Vanilla's own particle colours, so the markers match the swirls around the entity. */
    private static final Map<String, Integer> COLORS = Map.ofEntries(
            Map.entry("speed", 0x7CAFC6),
            Map.entry("slowness", 0x5A6C81),
            Map.entry("haste", 0xD9C043),
            Map.entry("mining_fatigue", 0x4A4217),
            Map.entry("strength", 0x932423),
            Map.entry("instant_health", 0xF82423),
            Map.entry("instant_damage", 0x430A09),
            Map.entry("jump_boost", 0x22FF4C),
            Map.entry("nausea", 0x551D4A),
            Map.entry("regeneration", 0xCD5CAB),
            Map.entry("resistance", 0x99453A),
            Map.entry("fire_resistance", 0xE49A3A),
            Map.entry("water_breathing", 0x2E5299),
            Map.entry("invisibility", 0xF6F6F6),
            Map.entry("blindness", 0x1F1F23),
            Map.entry("night_vision", 0x1F1FA1),
            Map.entry("hunger", 0x587653),
            Map.entry("weakness", 0x484D48),
            Map.entry("poison", 0x4E9331),
            Map.entry("wither", 0x736156),
            Map.entry("health_boost", 0xF87D23),
            Map.entry("absorption", 0x2552A5),
            Map.entry("saturation", 0xF82423),
            Map.entry("glowing", 0x94A061),
            Map.entry("levitation", 0xCEFFFF),
            Map.entry("slow_falling", 0xF3CFB9),
            Map.entry("conduit_power", 0x1DC2D1),
            Map.entry("dolphins_grace", 0x88A3BE),
            Map.entry("bad_omen", 0x0B6138),
            Map.entry("hero_of_the_village", 0x44FF44),
            Map.entry("darkness", 0x292721));

    /**
     * Overrides where the natural first letter would collide. Resistance takes D for "defence" so it
     * does not fight regeneration for R, and so on.
     */
    private static final Map<String, String> LETTERS = Map.of(
            "resistance", "D",
            "slowness", "L",
            "weakness", "K",
            "mining_fatigue", "M",
            "fire_resistance", "F",
            "water_breathing", "B",
            "night_vision", "N",
            "instant_damage", "X",
            "instant_health", "H",
            "health_boost", "+");

    private StatusEffectGlyphs() {
    }

    public static String letter(String path) {
        String override = LETTERS.get(path);
        if (override != null) {
            return override;
        }
        return path.isEmpty() ? "?" : path.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    public static int color(String path) {
        return COLORS.getOrDefault(path, 0xFFFFFF);
    }
}

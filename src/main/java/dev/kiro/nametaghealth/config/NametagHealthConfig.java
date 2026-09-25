package dev.kiro.nametaghealth.config;

import java.util.ArrayList;
import java.util.List;

/**
 * Plain data holder, serialised to {@code config/nametag_health.json} by {@link ConfigManager}.
 * Field names are the JSON keys, so renaming a field resets that option for existing users.
 */
public class NametagHealthConfig {
    // --- general ---------------------------------------------------------------------------
    public boolean enabled = true;
    public boolean showOnPlayers = true;
    public boolean showOnMobs = false;
    /** Skip the indicator entirely while the entity is at full health. */
    public boolean hideWhenFull = false;
    /** Blocks from you, beyond which the indicator is dropped. */
    public int maxDistance = 48;
    /** Runtime on/off state driven by the toggle keybind. Persisted so it survives a restart. */
    public boolean toggledOn = true;

    // --- visibility ------------------------------------------------------------------------
    public RevealMode revealMode = RevealMode.ALWAYS;
    /** Item ids that count as "held" for {@link RevealMode#HOLDING_LISTED_ITEM}. */
    public List<String> revealItems = new ArrayList<>(List.of(
            "minecraft:wooden_sword", "minecraft:stone_sword", "minecraft:iron_sword",
            "minecraft:golden_sword", "minecraft:diamond_sword", "minecraft:netherite_sword",
            "minecraft:bow", "minecraft:crossbow", "minecraft:trident", "minecraft:mace"));
    /** Hide the indicator when blocks sit between you and the entity. */
    public boolean requireLineOfSight = false;
    /** Hide alongside the rest of the HUD when the player presses F1. */
    public boolean respectHudHidden = true;
    public TeamFilter teamFilter = TeamFilter.ALL;
    public FilterMode entityFilterMode = FilterMode.OFF;
    /** Entity ids such as {@code minecraft:creeper}, used by {@link #entityFilterMode}. */
    public List<String> entityFilter = new ArrayList<>();
    /** Ignore entities whose maximum health is below this, e.g. to skip chickens. */
    public int minMaxHealth = 0;
    /** Ignore entities whose maximum health is above this, e.g. to skip bosses. */
    public int maxMaxHealth = 1024;
    public boolean hideForInvisible = true;

    // --- content ---------------------------------------------------------------------------
    public Placement placement = Placement.SUFFIX;
    /**
     * Overrides {@link #style} and the extras below when set. Whitespace-separated words; any word
     * whose tokens all resolve to nothing is dropped, so spacing never collapses into gaps.
     * Tokens: {@code {sym} {hp} {max} {frac} {pct} {bar} {abs} {armor} {fx} {ping} {delta}}
     */
    public String formatTemplate = "";
    public DisplayStyle style = DisplayStyle.HEARTS;
    /** Decimal places on the health number. 0 rounds to whole hearts. */
    public int decimals = 0;
    /** Glyph drawn before the number in {@link DisplayStyle#HEARTS}. */
    public String symbol = "\u2665";
    /** Wrap the indicator in grey brackets. */
    public boolean wrapInBrackets = false;
    /** Append a gold {@code +n} for absorption (golden hearts). */
    public boolean showAbsorption = true;
    public boolean showArmor = false;
    public String armorSymbol = "\u25C6";
    /** Show the remaining durability of whichever armour piece is closest to breaking. */
    public boolean showArmorDurability = false;
    /**
     * Text placed before the durability value. Free-form, so it takes a word, a glyph, or nothing —
     * {@code "Durability:"}, {@code "▣"} and {@code ""} are all valid. Empty by default, since the
     * slot letter already says what the number refers to.
     */
    public String armorDurabilityLabel = "";
    public DurabilityStyle armorDurabilityStyle = DurabilityStyle.REMAINING;
    /** Prefix the value with a letter naming the slot, so you know which piece is failing. */
    public boolean armorDurabilityShowSlot = true;
    /** Slot letters, configurable so the marker can read however you like. */
    public String slotLabelHead = "H";
    public String slotLabelChest = "C";
    public String slotLabelLegs = "L";
    public String slotLabelFeet = "B";
    public boolean showEffects = false;
    public EffectStyle effectStyle = EffectStyle.DOTS;
    public int maxEffectsShown = 3;
    public String effectSymbol = "\u25CF";
    public int effectColor = 0x55FFFF;
    /** Briefly show how much health just changed, as {@code -4} or {@code +2}. */
    public boolean showDelta = false;
    public int deltaHoldMillis = 1500;
    /** Show connection latency for players. */
    public boolean showPing = false;
    /** Render 1200 as {@code 1.2k}, for high-health modded bosses. */
    public boolean abbreviateLargeNumbers = true;
    public int abbreviateAbove = 1000;
    public int barSegments = 10;
    public String barFilled = "\u2588";
    public String barEmpty = "\u2591";

    // --- colour ----------------------------------------------------------------------------
    public ColorMode colorMode = ColorMode.GRADIENT;
    public Palette palette = Palette.CLASSIC;
    /** Health percentage at or above which the indicator counts as healthy. */
    public int highThresholdPercent = 60;
    /** Health percentage at or below which the indicator counts as critical. */
    public int lowThresholdPercent = 30;
    /** Used by {@link ColorMode#STATIC}. */
    public int staticColor = 0xFF5555;
    public int absorptionColor = 0xFFD700;
    /** Colour only the symbol/bar and leave the number white — a quieter look. */
    public boolean colorSymbolOnly = false;
    /** Fade the colour in and out while below {@link #pulseThresholdPercent}. */
    public boolean lowHealthPulse = false;
    public int pulseThresholdPercent = 30;
    public int pulseSpeedMillis = 700;

    // --- text style ------------------------------------------------------------------------
    public boolean bold = false;
    public boolean italic = false;
    public boolean underline = false;

    // --- advanced --------------------------------------------------------------------------
    /**
     * Milliseconds the displayed health takes to catch up to the real value. 0 snaps instantly.
     * Animation bypasses the rebuild cache, so this and {@link #lowHealthPulse} cost more frames.
     */
    public int smoothingMillis = 0;
    /** How often the indicator text is rebuilt. Higher values trade responsiveness for cheaper frames. */
    public int updateIntervalMillis = 100;

    /** Brings out-of-range values from a hand-edited file back into something renderable. */
    public NametagHealthConfig normalize() {
        if (style == null) style = DisplayStyle.HEARTS;
        if (colorMode == null) colorMode = ColorMode.GRADIENT;
        if (revealMode == null) revealMode = RevealMode.ALWAYS;
        if (teamFilter == null) teamFilter = TeamFilter.ALL;
        if (entityFilterMode == null) entityFilterMode = FilterMode.OFF;
        if (placement == null) placement = Placement.SUFFIX;
        if (palette == null) palette = Palette.CLASSIC;
        // An older config may hold the removed LETTERS value, which Gson leaves as null.
        if (effectStyle == null) effectStyle = EffectStyle.DOTS;
        if (armorDurabilityStyle == null) armorDurabilityStyle = DurabilityStyle.REMAINING;

        if (symbol == null) symbol = "";
        if (armorSymbol == null) armorSymbol = "";
        if (effectSymbol == null || effectSymbol.isEmpty()) effectSymbol = "\u25CF";
        if (armorDurabilityLabel == null) armorDurabilityLabel = "";
        if (slotLabelHead == null) slotLabelHead = "";
        if (slotLabelChest == null) slotLabelChest = "";
        if (slotLabelLegs == null) slotLabelLegs = "";
        if (slotLabelFeet == null) slotLabelFeet = "";
        if (formatTemplate == null) formatTemplate = "";
        if (barFilled == null || barFilled.isEmpty()) barFilled = "\u2588";
        if (barEmpty == null || barEmpty.isEmpty()) barEmpty = "\u2591";
        if (revealItems == null) revealItems = new ArrayList<>();
        if (entityFilter == null) entityFilter = new ArrayList<>();

        maxDistance = clamp(maxDistance, 4, 128);
        decimals = clamp(decimals, 0, 2);
        barSegments = clamp(barSegments, 4, 20);
        maxEffectsShown = clamp(maxEffectsShown, 1, 8);
        deltaHoldMillis = clamp(deltaHoldMillis, 200, 10_000);
        abbreviateAbove = clamp(abbreviateAbove, 100, 1_000_000);
        smoothingMillis = clamp(smoothingMillis, 0, 2_000);
        updateIntervalMillis = clamp(updateIntervalMillis, 0, 1_000);
        pulseSpeedMillis = clamp(pulseSpeedMillis, 100, 3_000);
        pulseThresholdPercent = clamp(pulseThresholdPercent, 1, 100);

        minMaxHealth = clamp(minMaxHealth, 0, 100_000);
        maxMaxHealth = clamp(maxMaxHealth, 1, 100_000);
        if (minMaxHealth > maxMaxHealth) {
            int swap = minMaxHealth;
            minMaxHealth = maxMaxHealth;
            maxMaxHealth = swap;
        }

        lowThresholdPercent = clamp(lowThresholdPercent, 1, 99);
        highThresholdPercent = clamp(highThresholdPercent, 1, 99);
        // A high threshold at or below the low one would make the middle band impossible.
        if (highThresholdPercent <= lowThresholdPercent) {
            highThresholdPercent = Math.min(99, lowThresholdPercent + 1);
        }

        staticColor &= 0xFFFFFF;
        absorptionColor &= 0xFFFFFF;
        effectColor &= 0xFFFFFF;

        // Long symbols push the nametag off-centre; keep them short.
        if (symbol.length() > 4) symbol = symbol.substring(0, 4);
        if (armorSymbol.length() > 4) armorSymbol = armorSymbol.substring(0, 4);
        if (effectSymbol.length() > 4) effectSymbol = effectSymbol.substring(0, 4);
        // Roomier than the glyph fields, since this one is meant to hold a word.
        if (armorDurabilityLabel.length() > 24) armorDurabilityLabel = armorDurabilityLabel.substring(0, 24);
        if (slotLabelHead.length() > 8) slotLabelHead = slotLabelHead.substring(0, 8);
        if (slotLabelChest.length() > 8) slotLabelChest = slotLabelChest.substring(0, 8);
        if (slotLabelLegs.length() > 8) slotLabelLegs = slotLabelLegs.substring(0, 8);
        if (slotLabelFeet.length() > 8) slotLabelFeet = slotLabelFeet.substring(0, 8);
        if (barFilled.length() > 2) barFilled = barFilled.substring(0, 2);
        if (barEmpty.length() > 2) barEmpty = barEmpty.substring(0, 2);
        return this;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

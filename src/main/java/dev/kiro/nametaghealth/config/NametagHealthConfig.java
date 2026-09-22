package dev.kiro.nametaghealth.config;

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

    // --- appearance ------------------------------------------------------------------------
    public DisplayStyle style = DisplayStyle.HEARTS;
    /** Decimal places on the health number. 0 rounds to whole hearts. */
    public int decimals = 0;
    /** Glyph drawn before the number in {@link DisplayStyle#HEARTS}. */
    public String symbol = "\u2665";
    /** Wrap the indicator in grey brackets. */
    public boolean wrapInBrackets = false;
    /** Append a gold {@code +n} for absorption (golden hearts). */
    public boolean showAbsorption = true;
    public int barSegments = 10;
    public String barFilled = "\u2588";
    public String barEmpty = "\u2591";

    // --- colour ----------------------------------------------------------------------------
    public ColorMode colorMode = ColorMode.GRADIENT;
    /** Used by {@link ColorMode#STATIC}. */
    public int staticColor = 0xFF5555;
    public int absorptionColor = 0xFFD700;
    /** Colour only the symbol/bar and leave the number white — a quieter look. */
    public boolean colorSymbolOnly = false;

    /** Brings out-of-range values from a hand-edited file back into something renderable. */
    public NametagHealthConfig normalize() {
        if (style == null) style = DisplayStyle.HEARTS;
        if (colorMode == null) colorMode = ColorMode.GRADIENT;
        if (symbol == null) symbol = "";
        if (barFilled == null || barFilled.isEmpty()) barFilled = "\u2588";
        if (barEmpty == null || barEmpty.isEmpty()) barEmpty = "\u2591";

        maxDistance = clamp(maxDistance, 4, 128);
        decimals = clamp(decimals, 0, 2);
        barSegments = clamp(barSegments, 4, 20);
        staticColor &= 0xFFFFFF;
        absorptionColor &= 0xFFFFFF;

        // A very long symbol would push the nametag off-centre; keep it short.
        if (symbol.length() > 4) symbol = symbol.substring(0, 4);
        if (barFilled.length() > 2) barFilled = barFilled.substring(0, 2);
        if (barEmpty.length() > 2) barEmpty = barEmpty.substring(0, 2);
        return this;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}

package dev.kiro.nametaghealth.config;

import java.util.Locale;

/**
 * The three anchor colours the health gradient runs through. {@link ColorMode} decides how they are
 * blended; this decides what they are.
 */
public enum Palette implements OptionLabel {
    /** Red → yellow → green. */
    CLASSIC(0xFF5555, 0xFFFF55, 0x55FF55),
    /**
     * Red and green are the classic confusion pair for deuteranopia and protanopia, so this runs
     * orange → light grey → blue instead, which stays distinguishable by hue and brightness.
     */
    COLORBLIND(0xE8701A, 0xE0E0E0, 0x3B8EEA),
    /** Dark grey → mid grey → white: reads by brightness alone. */
    MONOCHROME(0x5A5A5A, 0xA8A8A8, 0xFFFFFF);

    private final int low;
    private final int mid;
    private final int high;

    Palette(int low, int mid, int high) {
        this.low = low;
        this.mid = mid;
        this.high = high;
    }

    /** Colour for nearly-dead. */
    public int low() {
        return this.low;
    }

    /** Colour for roughly half health. */
    public int mid() {
        return this.mid;
    }

    /** Colour for full health. */
    public int high() {
        return this.high;
    }

    public String translationKey() {
        return "nametag_health.palette." + name().toLowerCase(Locale.ROOT);
    }
}

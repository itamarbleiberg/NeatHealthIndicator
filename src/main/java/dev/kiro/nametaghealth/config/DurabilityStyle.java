package dev.kiro.nametaghealth.config;

import java.util.Locale;

/** How the worst armour piece's remaining durability is written. */
public enum DurabilityStyle implements OptionLabel {
    /** {@code 14%} */
    PERCENT,
    /** {@code 34} — hits left before it breaks. */
    REMAINING,
    /** {@code 34/240} */
    FRACTION;

    public String translationKey() {
        return "nametag_health.durability_style." + name().toLowerCase(Locale.ROOT);
    }
}

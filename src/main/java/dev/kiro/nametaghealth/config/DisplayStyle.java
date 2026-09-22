package dev.kiro.nametaghealth.config;

/** How the health value itself is written next to the name. */
public enum DisplayStyle {
    /** {@code ♥ 14} — symbol plus the current health. */
    HEARTS,
    /** {@code 14} — just the number. */
    NUMBER,
    /** {@code 14/20} — current out of maximum. */
    FRACTION,
    /** {@code 70%} — share of maximum health. */
    PERCENT,
    /** {@code ███████░░░} — a compact text bar. */
    BAR;

    public String translationKey() {
        return "nametag_health.style." + name().toLowerCase(java.util.Locale.ROOT);
    }
}

package dev.kiro.nametaghealth.config;

/** How the indicator is coloured. */
public enum ColorMode {
    /** Smoothly fades red → yellow → green as health rises. */
    GRADIENT,
    /** Three flat steps: green, yellow, red. */
    THRESHOLDS,
    /** One fixed colour regardless of health. */
    STATIC;

    public String translationKey() {
        return "nametag_health.color_mode." + name().toLowerCase(java.util.Locale.ROOT);
    }
}

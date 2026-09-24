package dev.kiro.nametaghealth.config;

import java.util.Locale;

/** How active status effects are summarised. */
public enum EffectStyle implements OptionLabel {
    /** One letter per effect, taken from its id: {@code RPS}. */
    LETTERS,
    /** One coloured dot per effect: {@code ●●●}. */
    DOTS,
    /** Just how many are active: {@code 3}. */
    COUNT;

    public String translationKey() {
        return "nametag_health.effect_style." + name().toLowerCase(Locale.ROOT);
    }
}

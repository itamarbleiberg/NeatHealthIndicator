package dev.kiro.nametaghealth.config;

import java.util.Locale;

/**
 * How active status effects are summarised.
 *
 * <p>There is no "letters" option because naming the effects is impossible client-side: which effects
 * an entity has is server-side state. Only the swirl particle count is synced.
 */
public enum EffectStyle implements OptionLabel {
    /** One marker per effect: {@code ●●●}. */
    DOTS,
    /** The marker followed by how many are active: {@code ●3}. */
    COUNT;

    public String translationKey() {
        return "nametag_health.effect_style." + name().toLowerCase(Locale.ROOT);
    }
}

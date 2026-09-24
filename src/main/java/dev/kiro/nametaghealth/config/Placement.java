package dev.kiro.nametaghealth.config;

import java.util.Locale;

/** Where the indicator sits relative to the entity's name. */
public enum Placement implements OptionLabel {
    /** {@code Steve ♥ 14} */
    SUFFIX,
    /** {@code ♥ 14 Steve} */
    PREFIX,
    /** {@code ♥ 14} — the name is dropped entirely. */
    REPLACE_NAME;

    public String translationKey() {
        return "nametag_health.placement." + name().toLowerCase(Locale.ROOT);
    }
}

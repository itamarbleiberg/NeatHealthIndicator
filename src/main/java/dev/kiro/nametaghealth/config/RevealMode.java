package dev.kiro.nametaghealth.config;

import java.util.Locale;

/** When the indicator is allowed to appear at all. */
public enum RevealMode implements OptionLabel {
    /** Any nametag in range gets an indicator. */
    ALWAYS,
    /** Only while the "reveal" keybind is held down. */
    WHILE_KEY_HELD,
    /** Only on the entity currently under your crosshair. */
    CROSSHAIR_TARGET,
    /** Only while holding one of the items listed in the config. */
    HOLDING_LISTED_ITEM;

    public String translationKey() {
        return "nametag_health.reveal_mode." + name().toLowerCase(Locale.ROOT);
    }
}

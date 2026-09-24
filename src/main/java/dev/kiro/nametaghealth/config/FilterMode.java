package dev.kiro.nametaghealth.config;

import java.util.Locale;

/** How the entity id list is applied. */
public enum FilterMode implements OptionLabel {
    /** Ignore the list entirely. */
    OFF,
    /** Show only entity types on the list. */
    WHITELIST,
    /** Show everything except entity types on the list. */
    BLACKLIST;

    public String translationKey() {
        return "nametag_health.filter_mode." + name().toLowerCase(Locale.ROOT);
    }
}

package dev.kiro.nametaghealth.config;

import java.util.Locale;

/** Restricts the indicator by scoreboard team relationship. */
public enum TeamFilter implements OptionLabel {
    ALL,
    /** Only entities on your own scoreboard team. */
    TEAMMATES_ONLY,
    /** Only entities that are not on your team. */
    ENEMIES_ONLY;

    public String translationKey() {
        return "nametag_health.team_filter." + name().toLowerCase(Locale.ROOT);
    }
}

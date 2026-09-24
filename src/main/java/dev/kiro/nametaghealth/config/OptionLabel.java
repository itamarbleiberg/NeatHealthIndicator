package dev.kiro.nametaghealth.config;

/**
 * Implemented by every config enum so the settings screen can label any dropdown through one
 * function instead of a cast per enum type.
 */
public interface OptionLabel {
    String translationKey();
}

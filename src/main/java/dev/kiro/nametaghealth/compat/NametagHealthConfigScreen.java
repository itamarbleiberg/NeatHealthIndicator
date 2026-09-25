package dev.kiro.nametaghealth.compat;

import dev.kiro.nametaghealth.config.ColorMode;
import dev.kiro.nametaghealth.config.ConfigManager;
import dev.kiro.nametaghealth.config.DisplayStyle;
import dev.kiro.nametaghealth.config.DurabilityStyle;
import dev.kiro.nametaghealth.config.EffectStyle;
import dev.kiro.nametaghealth.config.FilterMode;
import dev.kiro.nametaghealth.config.NametagHealthConfig;
import dev.kiro.nametaghealth.config.OptionLabel;
import dev.kiro.nametaghealth.config.Palette;
import dev.kiro.nametaghealth.config.Placement;
import dev.kiro.nametaghealth.config.RevealMode;
import dev.kiro.nametaghealth.config.TeamFilter;
import dev.kiro.nametaghealth.render.HealthSamples;
import dev.kiro.nametaghealth.render.IndicatorFormatter;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

/** The Cloth Config screen reached from Mod Menu. */
@Environment(EnvType.CLIENT)
public final class NametagHealthConfigScreen {
    private static final String UNIT_BLOCKS = "nametag_health.unit.blocks";
    private static final String UNIT_MILLIS = "nametag_health.unit.millis";
    private static final String UNIT_PERCENT = "nametag_health.unit.percent";
    private static final String UNIT_HEALTH = "nametag_health.unit.health";

    private NametagHealthConfigScreen() {
    }

    public static Screen create(Screen parent) {
        NametagHealthConfig config = ConfigManager.get();
        NametagHealthConfig defaults = new NametagHealthConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("nametag_health.config.title"))
                .setSavingRunnable(() -> {
                    ConfigManager.save();
                    // Drop cached indicator text so edits show up on the very next frame.
                    HealthSamples.invalidateAll();
                });

        ConfigEntryBuilder entries = builder.entryBuilder();
        general(builder, entries, config, defaults);
        visibility(builder, entries, config, defaults);
        content(builder, entries, config, defaults);
        colour(builder, entries, config, defaults);
        textStyle(builder, entries, config, defaults);
        advanced(builder, entries, config, defaults);

        return builder.build();
    }

    // ---------------------------------------------------------------------------------------
    // categories
    // ---------------------------------------------------------------------------------------

    private static void general(ConfigBuilder builder, ConfigEntryBuilder entries,
                               NametagHealthConfig config, NametagHealthConfig defaults) {
        ConfigCategory category = category(builder, "general");

        bool(category, entries, "enabled", config.enabled, defaults.enabled, v -> config.enabled = v);
        bool(category, entries, "show_on_players", config.showOnPlayers, defaults.showOnPlayers,
                v -> config.showOnPlayers = v);
        bool(category, entries, "show_on_mobs", config.showOnMobs, defaults.showOnMobs,
                v -> config.showOnMobs = v);
        bool(category, entries, "hide_when_full", config.hideWhenFull, defaults.hideWhenFull,
                v -> config.hideWhenFull = v);
        slider(category, entries, "max_distance", config.maxDistance, defaults.maxDistance, 4, 128,
                UNIT_BLOCKS, v -> config.maxDistance = v);
        bool(category, entries, "toggled_on", config.toggledOn, defaults.toggledOn,
                v -> config.toggledOn = v);
    }

    private static void visibility(ConfigBuilder builder, ConfigEntryBuilder entries,
                                   NametagHealthConfig config, NametagHealthConfig defaults) {
        ConfigCategory category = category(builder, "visibility");

        enumOption(category, entries, "reveal_mode", RevealMode.class, config.revealMode,
                defaults.revealMode, v -> config.revealMode = v);
        stringList(category, entries, "reveal_items", config.revealItems, defaults.revealItems,
                v -> config.revealItems = v);
        bool(category, entries, "require_line_of_sight", config.requireLineOfSight,
                defaults.requireLineOfSight, v -> config.requireLineOfSight = v);
        bool(category, entries, "respect_hud_hidden", config.respectHudHidden,
                defaults.respectHudHidden, v -> config.respectHudHidden = v);
        bool(category, entries, "hide_for_invisible", config.hideForInvisible,
                defaults.hideForInvisible, v -> config.hideForInvisible = v);
        enumOption(category, entries, "team_filter", TeamFilter.class, config.teamFilter,
                defaults.teamFilter, v -> config.teamFilter = v);
        enumOption(category, entries, "entity_filter_mode", FilterMode.class, config.entityFilterMode,
                defaults.entityFilterMode, v -> config.entityFilterMode = v);
        stringList(category, entries, "entity_filter", config.entityFilter, defaults.entityFilter,
                v -> config.entityFilter = v);
        slider(category, entries, "min_max_health", config.minMaxHealth, defaults.minMaxHealth,
                0, 512, UNIT_HEALTH, v -> config.minMaxHealth = v);
        slider(category, entries, "max_max_health", config.maxMaxHealth, defaults.maxMaxHealth,
                1, 1024, UNIT_HEALTH, v -> config.maxMaxHealth = v);
    }

    private static void content(ConfigBuilder builder, ConfigEntryBuilder entries,
                                NametagHealthConfig config, NametagHealthConfig defaults) {
        ConfigCategory category = category(builder, "content");
        long now = System.currentTimeMillis();

        // Rendered from the settings as they were when this screen opened.
        category.addEntry(entries.startTextDescription(Text.translatable("nametag_health.preview.healthy",
                IndicatorFormatter.preview(config, 14.0F, 20.0F, 0.0F, 8, 0.0F, now))).build());
        category.addEntry(entries.startTextDescription(Text.translatable("nametag_health.preview.critical",
                IndicatorFormatter.preview(config, 3.0F, 20.0F, 0.0F, 8, -4.0F, now))).build());

        enumOption(category, entries, "placement", Placement.class, config.placement,
                defaults.placement, v -> config.placement = v);
        string(category, entries, "format_template", config.formatTemplate, defaults.formatTemplate,
                v -> config.formatTemplate = v);
        enumOption(category, entries, "style", DisplayStyle.class, config.style, defaults.style,
                v -> config.style = v);
        slider(category, entries, "decimals", config.decimals, defaults.decimals, 0, 2, null,
                v -> config.decimals = v);
        string(category, entries, "symbol", config.symbol, defaults.symbol, v -> config.symbol = v);
        bool(category, entries, "wrap_in_brackets", config.wrapInBrackets, defaults.wrapInBrackets,
                v -> config.wrapInBrackets = v);
        bool(category, entries, "show_absorption", config.showAbsorption, defaults.showAbsorption,
                v -> config.showAbsorption = v);
        bool(category, entries, "show_armor", config.showArmor, defaults.showArmor,
                v -> config.showArmor = v);
        string(category, entries, "armor_symbol", config.armorSymbol, defaults.armorSymbol,
                v -> config.armorSymbol = v);
        bool(category, entries, "show_armor_durability", config.showArmorDurability,
                defaults.showArmorDurability, v -> config.showArmorDurability = v);
        string(category, entries, "armor_durability_symbol", config.armorDurabilitySymbol,
                defaults.armorDurabilitySymbol, v -> config.armorDurabilitySymbol = v);
        enumOption(category, entries, "armor_durability_style", DurabilityStyle.class,
                config.armorDurabilityStyle, defaults.armorDurabilityStyle,
                v -> config.armorDurabilityStyle = v);
        bool(category, entries, "armor_durability_show_slot", config.armorDurabilityShowSlot,
                defaults.armorDurabilityShowSlot, v -> config.armorDurabilityShowSlot = v);
        bool(category, entries, "show_effects", config.showEffects, defaults.showEffects,
                v -> config.showEffects = v);
        enumOption(category, entries, "effect_style", EffectStyle.class, config.effectStyle,
                defaults.effectStyle, v -> config.effectStyle = v);
        slider(category, entries, "max_effects_shown", config.maxEffectsShown, defaults.maxEffectsShown,
                1, 8, null, v -> config.maxEffectsShown = v);
        string(category, entries, "effect_symbol", config.effectSymbol, defaults.effectSymbol,
                v -> config.effectSymbol = v);
        colour(category, entries, "effect_color", config.effectColor, defaults.effectColor,
                v -> config.effectColor = v);
        bool(category, entries, "show_delta", config.showDelta, defaults.showDelta,
                v -> config.showDelta = v);
        slider(category, entries, "delta_hold_millis", config.deltaHoldMillis, defaults.deltaHoldMillis,
                200, 10_000, UNIT_MILLIS, v -> config.deltaHoldMillis = v);
        bool(category, entries, "show_ping", config.showPing, defaults.showPing,
                v -> config.showPing = v);
        bool(category, entries, "abbreviate_large_numbers", config.abbreviateLargeNumbers,
                defaults.abbreviateLargeNumbers, v -> config.abbreviateLargeNumbers = v);
        slider(category, entries, "abbreviate_above", config.abbreviateAbove, defaults.abbreviateAbove,
                100, 100_000, UNIT_HEALTH, v -> config.abbreviateAbove = v);
        slider(category, entries, "bar_segments", config.barSegments, defaults.barSegments, 4, 20, null,
                v -> config.barSegments = v);
        string(category, entries, "bar_filled", config.barFilled, defaults.barFilled,
                v -> config.barFilled = v);
        string(category, entries, "bar_empty", config.barEmpty, defaults.barEmpty,
                v -> config.barEmpty = v);
    }

    private static void colour(ConfigBuilder builder, ConfigEntryBuilder entries,
                               NametagHealthConfig config, NametagHealthConfig defaults) {
        ConfigCategory category = category(builder, "color");

        enumOption(category, entries, "color_mode", ColorMode.class, config.colorMode,
                defaults.colorMode, v -> config.colorMode = v);
        enumOption(category, entries, "palette", Palette.class, config.palette, defaults.palette,
                v -> config.palette = v);
        slider(category, entries, "high_threshold", config.highThresholdPercent,
                defaults.highThresholdPercent, 1, 99, UNIT_PERCENT, v -> config.highThresholdPercent = v);
        slider(category, entries, "low_threshold", config.lowThresholdPercent,
                defaults.lowThresholdPercent, 1, 99, UNIT_PERCENT, v -> config.lowThresholdPercent = v);
        colour(category, entries, "static_color", config.staticColor, defaults.staticColor,
                v -> config.staticColor = v);
        colour(category, entries, "absorption_color", config.absorptionColor, defaults.absorptionColor,
                v -> config.absorptionColor = v);
        bool(category, entries, "color_symbol_only", config.colorSymbolOnly, defaults.colorSymbolOnly,
                v -> config.colorSymbolOnly = v);
        bool(category, entries, "low_health_pulse", config.lowHealthPulse, defaults.lowHealthPulse,
                v -> config.lowHealthPulse = v);
        slider(category, entries, "pulse_threshold", config.pulseThresholdPercent,
                defaults.pulseThresholdPercent, 1, 100, UNIT_PERCENT, v -> config.pulseThresholdPercent = v);
        slider(category, entries, "pulse_speed", config.pulseSpeedMillis, defaults.pulseSpeedMillis,
                100, 3_000, UNIT_MILLIS, v -> config.pulseSpeedMillis = v);
    }

    private static void textStyle(ConfigBuilder builder, ConfigEntryBuilder entries,
                                  NametagHealthConfig config, NametagHealthConfig defaults) {
        ConfigCategory category = category(builder, "text_style");

        bool(category, entries, "bold", config.bold, defaults.bold, v -> config.bold = v);
        bool(category, entries, "italic", config.italic, defaults.italic, v -> config.italic = v);
        bool(category, entries, "underline", config.underline, defaults.underline,
                v -> config.underline = v);
    }

    private static void advanced(ConfigBuilder builder, ConfigEntryBuilder entries,
                                 NametagHealthConfig config, NametagHealthConfig defaults) {
        ConfigCategory category = category(builder, "advanced");

        slider(category, entries, "smoothing_millis", config.smoothingMillis, defaults.smoothingMillis,
                0, 2_000, UNIT_MILLIS, v -> config.smoothingMillis = v);
        slider(category, entries, "update_interval_millis", config.updateIntervalMillis,
                defaults.updateIntervalMillis, 0, 1_000, UNIT_MILLIS, v -> config.updateIntervalMillis = v);
    }

    // ---------------------------------------------------------------------------------------
    // entry helpers
    // ---------------------------------------------------------------------------------------

    private static ConfigCategory category(ConfigBuilder builder, String key) {
        return builder.getOrCreateCategory(Text.translatable("nametag_health.category." + key));
    }

    private static void bool(ConfigCategory category, ConfigEntryBuilder entries, String key,
                             boolean current, boolean fallback, Consumer<Boolean> save) {
        category.addEntry(entries.startBooleanToggle(label(key), current)
                .setDefaultValue(fallback)
                .setTooltip(tooltip(key))
                .setSaveConsumer(save)
                .build());
    }

    private static void slider(ConfigCategory category, ConfigEntryBuilder entries, String key,
                               int current, int fallback, int min, int max, String unitKey,
                               Consumer<Integer> save) {
        var entry = entries.startIntSlider(label(key), current, min, max)
                .setDefaultValue(fallback)
                .setTooltip(tooltip(key))
                .setSaveConsumer(save);
        if (unitKey != null) {
            entry.setTextGetter(value -> Text.translatable(unitKey, value));
        }
        category.addEntry(entry.build());
    }

    private static void string(ConfigCategory category, ConfigEntryBuilder entries, String key,
                               String current, String fallback, Consumer<String> save) {
        category.addEntry(entries.startStrField(label(key), current)
                .setDefaultValue(fallback)
                .setTooltip(tooltip(key))
                .setSaveConsumer(save)
                .build());
    }

    private static void colour(ConfigCategory category, ConfigEntryBuilder entries, String key,
                               int current, int fallback, Consumer<Integer> save) {
        category.addEntry(entries.startColorField(label(key), current)
                .setDefaultValue(fallback)
                .setTooltip(tooltip(key))
                .setSaveConsumer(save)
                .build());
    }

    private static void stringList(ConfigCategory category, ConfigEntryBuilder entries, String key,
                                   List<String> current, List<String> fallback,
                                   Consumer<List<String>> save) {
        category.addEntry(entries.startStrList(label(key), current)
                .setDefaultValue(fallback)
                .setTooltip(tooltip(key))
                .setSaveConsumer(save)
                .build());
    }

    private static <T extends Enum<T>> void enumOption(ConfigCategory category, ConfigEntryBuilder entries,
                                                       String key, Class<T> type, T current, T fallback,
                                                       Consumer<T> save) {
        category.addEntry(entries.startEnumSelector(label(key), type, current)
                .setDefaultValue(fallback)
                .setTooltip(tooltip(key))
                .setEnumNameProvider(value -> Text.translatable(((OptionLabel) value).translationKey()))
                .setSaveConsumer(save)
                .build());
    }

    private static Text label(String key) {
        return Text.translatable("nametag_health.option." + key);
    }

    private static Text tooltip(String key) {
        return Text.translatable("nametag_health.option." + key + ".tooltip");
    }
}

package dev.kiro.nametaghealth.compat;

import dev.kiro.nametaghealth.config.ColorMode;
import dev.kiro.nametaghealth.config.ConfigManager;
import dev.kiro.nametaghealth.config.DisplayStyle;
import dev.kiro.nametaghealth.config.NametagHealthConfig;
import dev.kiro.nametaghealth.render.HealthIndicator;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

/** The Cloth Config screen reached from Mod Menu. */
@Environment(EnvType.CLIENT)
public final class NametagHealthConfigScreen {
    private NametagHealthConfigScreen() {
    }

    public static Screen create(Screen parent) {
        NametagHealthConfig config = ConfigManager.get();
        NametagHealthConfig defaults = new NametagHealthConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("nametag_health.config.title"))
                .setSavingRunnable(ConfigManager::save);

        ConfigEntryBuilder entries = builder.entryBuilder();

        general(builder, entries, config, defaults);
        appearance(builder, entries, config, defaults);
        colors(builder, entries, config, defaults);

        return builder.build();
    }

    private static void general(ConfigBuilder builder, ConfigEntryBuilder entries,
                               NametagHealthConfig config, NametagHealthConfig defaults) {
        ConfigCategory category = builder.getOrCreateCategory(Text.translatable("nametag_health.category.general"));

        category.addEntry(entries.startBooleanToggle(option("enabled"), config.enabled)
                .setDefaultValue(defaults.enabled)
                .setTooltip(tooltip("enabled"))
                .setSaveConsumer(value -> config.enabled = value)
                .build());

        category.addEntry(entries.startBooleanToggle(option("show_on_players"), config.showOnPlayers)
                .setDefaultValue(defaults.showOnPlayers)
                .setTooltip(tooltip("show_on_players"))
                .setSaveConsumer(value -> config.showOnPlayers = value)
                .build());

        category.addEntry(entries.startBooleanToggle(option("show_on_mobs"), config.showOnMobs)
                .setDefaultValue(defaults.showOnMobs)
                .setTooltip(tooltip("show_on_mobs"))
                .setSaveConsumer(value -> config.showOnMobs = value)
                .build());

        category.addEntry(entries.startBooleanToggle(option("hide_when_full"), config.hideWhenFull)
                .setDefaultValue(defaults.hideWhenFull)
                .setTooltip(tooltip("hide_when_full"))
                .setSaveConsumer(value -> config.hideWhenFull = value)
                .build());

        category.addEntry(entries.startIntSlider(option("max_distance"), config.maxDistance, 4, 128)
                .setDefaultValue(defaults.maxDistance)
                .setTooltip(tooltip("max_distance"))
                .setTextGetter(value -> Text.translatable("nametag_health.option.max_distance.value", value))
                .setSaveConsumer(value -> config.maxDistance = value)
                .build());
    }

    private static void appearance(ConfigBuilder builder, ConfigEntryBuilder entries,
                                  NametagHealthConfig config, NametagHealthConfig defaults) {
        ConfigCategory category = builder.getOrCreateCategory(Text.translatable("nametag_health.category.appearance"));

        // Rendered from the settings as they were when this screen opened.
        category.addEntry(entries.startTextDescription(
                        Text.translatable("nametag_health.preview",
                                HealthIndicator.build(config, 14.0F, 20.0F, 0.0F)))
                .build());

        category.addEntry(entries.startEnumSelector(option("style"), DisplayStyle.class, config.style)
                .setDefaultValue(defaults.style)
                .setTooltip(tooltip("style"))
                .setEnumNameProvider(value -> Text.translatable(((DisplayStyle) value).translationKey()))
                .setSaveConsumer(value -> config.style = value)
                .build());

        category.addEntry(entries.startIntSlider(option("decimals"), config.decimals, 0, 2)
                .setDefaultValue(defaults.decimals)
                .setTooltip(tooltip("decimals"))
                .setSaveConsumer(value -> config.decimals = value)
                .build());

        category.addEntry(entries.startStrField(option("symbol"), config.symbol)
                .setDefaultValue(defaults.symbol)
                .setTooltip(tooltip("symbol"))
                .setSaveConsumer(value -> config.symbol = value)
                .build());

        category.addEntry(entries.startBooleanToggle(option("wrap_in_brackets"), config.wrapInBrackets)
                .setDefaultValue(defaults.wrapInBrackets)
                .setTooltip(tooltip("wrap_in_brackets"))
                .setSaveConsumer(value -> config.wrapInBrackets = value)
                .build());

        category.addEntry(entries.startBooleanToggle(option("show_absorption"), config.showAbsorption)
                .setDefaultValue(defaults.showAbsorption)
                .setTooltip(tooltip("show_absorption"))
                .setSaveConsumer(value -> config.showAbsorption = value)
                .build());

        category.addEntry(entries.startIntSlider(option("bar_segments"), config.barSegments, 4, 20)
                .setDefaultValue(defaults.barSegments)
                .setTooltip(tooltip("bar_segments"))
                .setSaveConsumer(value -> config.barSegments = value)
                .build());

        category.addEntry(entries.startStrField(option("bar_filled"), config.barFilled)
                .setDefaultValue(defaults.barFilled)
                .setTooltip(tooltip("bar_filled"))
                .setSaveConsumer(value -> config.barFilled = value)
                .build());

        category.addEntry(entries.startStrField(option("bar_empty"), config.barEmpty)
                .setDefaultValue(defaults.barEmpty)
                .setTooltip(tooltip("bar_empty"))
                .setSaveConsumer(value -> config.barEmpty = value)
                .build());
    }

    private static void colors(ConfigBuilder builder, ConfigEntryBuilder entries,
                               NametagHealthConfig config, NametagHealthConfig defaults) {
        ConfigCategory category = builder.getOrCreateCategory(Text.translatable("nametag_health.category.color"));

        category.addEntry(entries.startEnumSelector(option("color_mode"), ColorMode.class, config.colorMode)
                .setDefaultValue(defaults.colorMode)
                .setTooltip(tooltip("color_mode"))
                .setEnumNameProvider(value -> Text.translatable(((ColorMode) value).translationKey()))
                .setSaveConsumer(value -> config.colorMode = value)
                .build());

        category.addEntry(entries.startColorField(option("static_color"), config.staticColor)
                .setDefaultValue(defaults.staticColor)
                .setTooltip(tooltip("static_color"))
                .setSaveConsumer(value -> config.staticColor = value)
                .build());

        category.addEntry(entries.startColorField(option("absorption_color"), config.absorptionColor)
                .setDefaultValue(defaults.absorptionColor)
                .setTooltip(tooltip("absorption_color"))
                .setSaveConsumer(value -> config.absorptionColor = value)
                .build());

        category.addEntry(entries.startBooleanToggle(option("color_symbol_only"), config.colorSymbolOnly)
                .setDefaultValue(defaults.colorSymbolOnly)
                .setTooltip(tooltip("color_symbol_only"))
                .setSaveConsumer(value -> config.colorSymbolOnly = value)
                .build());
    }

    private static Text option(String key) {
        return Text.translatable("nametag_health.option." + key);
    }

    private static Text tooltip(String key) {
        return Text.translatable("nametag_health.option." + key + ".tooltip");
    }
}

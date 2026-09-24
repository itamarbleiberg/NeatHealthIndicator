package dev.kiro.nametaghealth.input;

import dev.kiro.nametaghealth.config.ConfigManager;
import dev.kiro.nametaghealth.config.NametagHealthConfig;
import dev.kiro.nametaghealth.render.HealthSamples;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

/**
 * Both keys are registered unbound. Claiming a default would risk stomping on whatever the player
 * already uses, and the mod is perfectly usable without them.
 *
 * <p>Keybind categories are typed as of 1.21.9, so the mod registers its own rather than passing a
 * category string.
 */
@Environment(EnvType.CLIENT)
public final class Keybinds {
    private static final KeyBinding.Category CATEGORY =
            KeyBinding.Category.create(Identifier.of("nametag_health", "main"));

    private static KeyBinding toggle;
    private static KeyBinding reveal;

    private Keybinds() {
    }

    public static void register() {
        toggle = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.nametag_health.toggle", GLFW.GLFW_KEY_UNKNOWN, CATEGORY));
        reveal = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.nametag_health.reveal", GLFW.GLFW_KEY_UNKNOWN, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggle.wasPressed()) {
                NametagHealthConfig config = ConfigManager.get();
                config.toggledOn = !config.toggledOn;
                ConfigManager.save();
                HealthSamples.invalidateAll();

                if (client.player != null) {
                    // Above the hotbar, so it confirms the press without spamming chat history.
                    client.player.sendMessage(Text.translatable(config.toggledOn
                            ? "nametag_health.toggled.on"
                            : "nametag_health.toggled.off"), true);
                }
            }
        });
    }

    /** True while the reveal key is held, used by {@code RevealMode.WHILE_KEY_HELD}. */
    public static boolean revealHeld() {
        return reveal != null && reveal.isPressed();
    }
}

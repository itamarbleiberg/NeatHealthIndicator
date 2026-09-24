package dev.kiro.nametaghealth;

import dev.kiro.nametaghealth.config.ConfigManager;
import dev.kiro.nametaghealth.input.Keybinds;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint. The indicator itself is injected into the entity render state by
 * {@code EntityRendererMixin}, so all that happens here is loading the config and claiming keybinds.
 */
@Environment(EnvType.CLIENT)
public final class NametagHealth implements ClientModInitializer {
    public static final String MOD_ID = "nametag_health";
    public static final Logger LOGGER = LoggerFactory.getLogger("Nametag Health");

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        Keybinds.register();
        LOGGER.info("Nametag Health ready.");
    }
}

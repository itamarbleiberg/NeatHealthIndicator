package dev.kiro.nametaghealth;

import dev.kiro.nametaghealth.config.ConfigManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client entrypoint. Nothing needs to be registered at runtime — the indicator is injected into the
 * entity render state by {@code EntityRendererMixin}, so all we do here is load the config once.
 */
@Environment(EnvType.CLIENT)
public final class NametagHealth implements ClientModInitializer {
    public static final String MOD_ID = "nametag_health";
    public static final Logger LOGGER = LoggerFactory.getLogger("Nametag Health");

    @Override
    public void onInitializeClient() {
        ConfigManager.load();
        LOGGER.info("Nametag Health ready.");
    }
}

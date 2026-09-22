package dev.kiro.nametaghealth.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.kiro.nametaghealth.NametagHealth;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Loads, holds and saves the single config instance. */
public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder()
            .setPrettyPrinting()
            .disableHtmlEscaping()
            .create();

    private static final Path PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve(NametagHealth.MOD_ID + ".json");

    private static NametagHealthConfig instance = new NametagHealthConfig();

    private ConfigManager() {
    }

    /** Never null — falls back to defaults if the file is missing or unreadable. */
    public static NametagHealthConfig get() {
        return instance;
    }

    public static void load() {
        if (!Files.exists(PATH)) {
            instance = new NametagHealthConfig();
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(PATH, StandardCharsets.UTF_8)) {
            NametagHealthConfig loaded = GSON.fromJson(reader, NametagHealthConfig.class);
            instance = loaded == null ? new NametagHealthConfig() : loaded.normalize();
        } catch (Exception e) {
            NametagHealth.LOGGER.warn("Could not read {}, using defaults.", PATH, e);
            instance = new NametagHealthConfig();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            instance.normalize();
            try (Writer writer = Files.newBufferedWriter(PATH, StandardCharsets.UTF_8)) {
                GSON.toJson(instance, writer);
            }
        } catch (IOException e) {
            NametagHealth.LOGGER.error("Could not write {}", PATH, e);
        }
    }
}

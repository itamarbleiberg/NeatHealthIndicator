package dev.kiro.nametaghealth.render;

import dev.kiro.nametaghealth.config.NametagHealthConfig;
import dev.kiro.nametaghealth.config.Palette;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Turns a health ratio into a colour.
 *
 * <p>The configured thresholds shape the gradient as well as the stepped mode: below the low
 * threshold the indicator is fully "critical", above the high threshold fully "healthy", and the
 * band between them is where the blend happens. Widening the band therefore makes the fade gentler.
 */
@Environment(EnvType.CLIENT)
public final class ColorPalettes {
    private ColorPalettes() {
    }

    public static int resolve(NametagHealthConfig config, float ratio, long now) {
        int color = base(config, ratio);
        if (config.lowHealthPulse && ratio * 100.0F <= config.pulseThresholdPercent) {
            color = pulse(color, config.pulseSpeedMillis, now);
        }
        return color;
    }

    /**
     * The palette colour for any 0–1 ratio, without the health-specific pulse. Used for things that
     * are not health but still read better on the same ramp, such as armour durability.
     */
    public static int base(NametagHealthConfig config, float ratio) {
        return switch (config.colorMode) {
            case STATIC -> config.staticColor;
            case THRESHOLDS -> stepped(config, ratio);
            case GRADIENT -> gradient(config, ratio);
        };
    }

    private static int stepped(NametagHealthConfig config, float ratio) {
        Palette palette = config.palette;
        float percent = ratio * 100.0F;
        if (percent >= config.highThresholdPercent) {
            return palette.high();
        }
        return percent > config.lowThresholdPercent ? palette.mid() : palette.low();
    }

    private static int gradient(NametagHealthConfig config, float ratio) {
        Palette palette = config.palette;
        float low = config.lowThresholdPercent / 100.0F;
        float high = config.highThresholdPercent / 100.0F;

        if (ratio <= low) {
            return palette.low();
        }
        if (ratio >= high) {
            return palette.high();
        }

        float position = (ratio - low) / (high - low);
        return position < 0.5F
                ? lerp(palette.low(), palette.mid(), position * 2.0F)
                : lerp(palette.mid(), palette.high(), (position - 0.5F) * 2.0F);
    }

    /** Fades between the colour and a dimmed copy of it, so the hue stays readable while it throbs. */
    private static int pulse(int color, int speedMillis, long now) {
        double phase = (now % speedMillis) / (double) speedMillis * Math.PI * 2.0;
        float amount = (float) ((Math.sin(phase) + 1.0) / 2.0);
        return lerp(scale(color, 0.45F), color, amount);
    }

    private static int scale(int color, float factor) {
        int r = Math.round((color >> 16 & 0xFF) * factor);
        int g = Math.round((color >> 8 & 0xFF) * factor);
        int b = Math.round((color & 0xFF) * factor);
        return r << 16 | g << 8 | b;
    }

    static int lerp(int from, int to, float delta) {
        float t = Math.max(0.0F, Math.min(1.0F, delta));
        int r = Math.round((from >> 16 & 0xFF) + ((to >> 16 & 0xFF) - (from >> 16 & 0xFF)) * t);
        int g = Math.round((from >> 8 & 0xFF) + ((to >> 8 & 0xFF) - (from >> 8 & 0xFF)) * t);
        int b = Math.round((from & 0xFF) + ((to & 0xFF) - (from & 0xFF)) * t);
        return r << 16 | g << 8 | b;
    }
}

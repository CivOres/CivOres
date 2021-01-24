package uk.co.froogo.civores.generation;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import uk.co.froogo.civores.CivOres;
import uk.co.froogo.civores.config.Config;

/**
 * Settings of the frequency, threshold, and optimal Y levels for one specific ore.
 */
public class OreGenerationSettings {
    private String type;
    private @NotNull Material material;

    private final float frequency;
    private final float minimum;
    private final float optimalYMin;
    private final float optimalYMax;
    private final float optimalYPunishment;

    public OreGenerationSettings(@NotNull Material material, float frequency, float minimum, float optimalYMin, float optimalYMax, float optimalYPunishment) {
        this.material = material;
        this.frequency = frequency;
        this.minimum = minimum;
        this.optimalYMin = optimalYMin;
        this.optimalYMax = optimalYMax;
        this.optimalYPunishment = optimalYPunishment;
    }

    /**
     * Initialisation called after loading configuration.
     */
    public void init(String key) {
        Material material = Material.getMaterial(type);
        if (material == null) {
            this.material = Material.STONE;
            CivOres.getInstance().getLogger().severe("Error parsing " + Config.location + "; unknown type \"" + type + "\" in preset \"" + key + "\"");
            return;
        }

        this.material = material;
    }

    public @NotNull Material getMaterial() {
        return material;
    }

    public float getFrequency() {
        return frequency;
    }

    public float getMinimum() {
        return minimum;
    }

    public float getOptimalYMin() {
        return optimalYMin;
    }

    public float getOptimalYMax() {
        return optimalYMax;
    }

    public float getOptimalYPunishment() {
        return optimalYPunishment;
    }
}

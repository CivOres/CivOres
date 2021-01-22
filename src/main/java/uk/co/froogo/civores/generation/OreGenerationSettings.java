package uk.co.froogo.civores.generation;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

/**
 * Settings of the frequency, threshold, and optimal Y levels for one specific ore.
 */
public class OreGenerationSettings {
    private final @NotNull Material material;

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

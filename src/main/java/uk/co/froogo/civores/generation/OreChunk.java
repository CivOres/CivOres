package uk.co.froogo.civores.generation;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Block;
import uk.co.froogo.civores.noise.FastNoise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OreChunk {
    private final HashMap<Short, Material> ores;

    public OreChunk(ArrayList<OreGenerationSettings> oreGenerationSettings, Chunk chunk) {
        ores = new HashMap<>();

        for (OreGenerationSettings settings : oreGenerationSettings) {
            FastNoise noise = new FastNoise();
            noise.SetSeed(settings.getMaterial().ordinal());
            noise.SetNoiseType(FastNoise.NoiseType.OpenSimplex2S);
            noise.SetFrequency(settings.getFrequency());
            noise.SetFractalType(FastNoise.FractalType.None);

            for (int y = 0; y < 256; y++) {
                float min;
                if (y < settings.getOptimalYMin())
                    min = settings.getMinimum() + (Math.abs(y - settings.getOptimalYMin()) * settings.getOptimalYPunishment());
                else if (y > settings.getOptimalYMax())
                    min = settings.getMinimum() + (Math.abs(y - settings.getOptimalYMax()) * settings.getOptimalYPunishment());
                else
                    min = settings.getMinimum();

                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        Block block =  chunk.getBlock(x, y, z);

                        if (noise.GetNoise(block.getX(), block.getY(), block.getZ()) >= min)
                            ores.put(coordsToShort(x, y, z), settings.getMaterial());
                    }
                }
            }
        }
    }

    public void visualiseAir(Chunk chunk) {
        for (Map.Entry<Short, Material> entry : ores.entrySet())
            shortToBlock(entry.getKey(), chunk).setType(entry.getValue());
    }

    private short coordsToShort(int x, int y, int z) {
        // Bits:
        // 1-4  = x
        // 5-8  = z
        // 9-16 = y

        return (short) (
                x |
                z << 4 |
                y << 8
        );
    }

    private Block shortToBlock(short coords, Chunk chunk) {
        // Bits:
        // 1-4  = x
        // 5-8  = z
        // 9-16 = y

        return chunk.getBlock(
                coords & 0xF,
                coords >> 8,
                (coords >> 4) & 0xF
        );
    }

    public HashMap<Short, Material> getOres() {
        return ores;
    }
}

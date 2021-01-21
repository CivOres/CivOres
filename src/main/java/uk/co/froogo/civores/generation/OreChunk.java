package uk.co.froogo.civores.generation;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import uk.co.froogo.civores.noise.FastNoise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OreChunk {
    private final HashMap<Long, Material> ores;

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
                            ores.put(block.getBlockKey(), settings.getMaterial());
                    }
                }
            }
        }
    }

    public void visualiseAir(World world) {
        for (Map.Entry<Long, Material> entry : ores.entrySet())
            world.getBlockAtKey(entry.getKey()).setType(entry.getValue());
    }

    public HashMap<Long, Material> getOres() {
        return ores;
    }
}

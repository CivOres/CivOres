package uk.co.froogo.civores.generation;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import uk.co.froogo.civores.noise.FastNoise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class OreChunk {
    private final HashMap<Short, Material> ores;

    /**
     * Generate all of the ores in a chunk for a specific player given settings.
     *
     * @param oreGenerationSettings the settings for each ore generated in the OreChunk.
     * @param chunk the real chunk to generate the ores for.
     * @param player the player who owns these ores.
     */
    public OreChunk(ArrayList<OreGenerationSettings> oreGenerationSettings, Chunk chunk, Player player) {
        ores = new HashMap<>();

        for (OreGenerationSettings settings : oreGenerationSettings) {
            FastNoise noise = new FastNoise();
            noise.SetSeed(generateSeed(player, chunk.getWorld(), settings.getMaterial()));
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

    /**
     * Generate a consistent seed from a player, world, and material.
     * @param player the player for who the seed is for, used for their UUID.
     * @param world the world for where the seed is for, used for its UUID.
     * @param material the material for the seed, used for its ordinal.
     * @return a consistent seed from the provided input parameters.
     */
    private int generateSeed(Player player, World world, Material material) {
        return (int) (
                player.getUniqueId().getMostSignificantBits() +
                player.getUniqueId().getLeastSignificantBits() +
                world.getUID().getMostSignificantBits() +
                world.getUID().getLeastSignificantBits() +
                material.ordinal()
        );
    }

    /**
     * Visualise an OreChunk to a player by replacing all of the blocks in that
     * chunk with either air or ores from this OreChunk.
     *
     * @param chunk this OreChunk's real chunk.
     */
    public void visualiseAir(Chunk chunk) {
        for (int x = 0; x < 16; x++)
            for (int y = 0; y < 256; y++)
                for (int z = 0; z < 16; z++)
                    chunk.getBlock(x, y, z).setType(Material.AIR);

        for (Map.Entry<Short, Material> entry : ores.entrySet())
            shortToBlock(entry.getKey(), chunk).setType(entry.getValue());
    }

    /**
     * Represent the co-ordinates of any block in a chunk more concisely.
     *
     * Bits:
     * x = 1-4
     * z = 5-8
     * y = 9-16
     * @param x x co-ordinate.
     * @param y y co-ordinate.
     * @param z z co-ordinate.
     * @return a short containing the co-ordinates more concisely.
     */
    private short coordsToShort(int x, int y, int z) {
        return (short) (
                x |
                z << 4 |
                y << 8
        );
    }

    /**
     * Get back a block from co-ords generated using coordsToShort(x, y, z).
     *
     * @param coords co-ordinates generated using coordsToShort(x, y, z).
     * @param chunk this OreChunk's real chunk.
     * @return the block at the co-ordinates in the chunk provided.
     */
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
}

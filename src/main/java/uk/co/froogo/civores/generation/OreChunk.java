package uk.co.froogo.civores.generation;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import uk.co.froogo.civores.noise.FastNoise;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A stateless consistently generating map of ores for a given chunk.
 * As this OreChunk is stateless, it is generated asynchronously, and later utilised for its result once its state is OreChunkState.GENERATED.
 */
public class OreChunk {
    private @NotNull OreChunkState state;
    private final @NotNull HashMap<Short, Material> ores;

    public OreChunk() {
        state = OreChunkState.GENERATING;
        ores = new HashMap<>();
    }

    /**
     * Generate all of the ores in a chunk for a specific player given settings.
     *
     * @param oreGenerationSettings the settings for each ore generated in the OreChunk.
     * @param chunkBlockX the X co-ordinate of the chunk bit shifted to the left by four.
     * @param chunkBlockZ the Z co-ordinate of the chunk bit shifted to the left by four.
     * @param worldUUID the UUID of the world for which to generate these ores.
     * @param playerUUID the UUID of the player for which to generate these ores.
     */
    public void generate(@NotNull ArrayList<@NotNull OreGenerationSettings> oreGenerationSettings, int chunkBlockX, int chunkBlockZ, @NotNull UUID worldUUID, @NotNull UUID playerUUID) {
        for (OreGenerationSettings settings : oreGenerationSettings) {
            FastNoise noise = new FastNoise();
            noise.SetSeed(generateSeed(playerUUID, worldUUID, settings.getMaterial()));
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
                        if (noise.GetNoise(chunkBlockX + x, y, chunkBlockZ + z) >= min)
                            ores.put(coordsToShort(x, y, z), settings.getMaterial());
                    }
                }
            }
        }

        state = OreChunkState.GENERATED;
    }

    /**
     * Generate a consistent seed from a player, world, and material.
     * @param playerUUID the player's UUID for who the seed is for.
     * @param worldUUID the world's UUID for where the seed is for.
     * @param material the material for the seed, used for its ordinal.
     * @return a consistent seed from the provided input parameters.
     */
    private int generateSeed(@NotNull UUID playerUUID, @NotNull UUID worldUUID, @NotNull Material material) {
        return (int) (
                playerUUID.getMostSignificantBits() +
                playerUUID.getLeastSignificantBits() +
                worldUUID.getMostSignificantBits() +
                worldUUID.getLeastSignificantBits() +
                material.ordinal()
        );
    }

    /**
     * Visualise an OreChunk to a player by replacing all of the blocks in that
     * chunk with either air or ores from this OreChunk.
     *
     * This is done entirely through packets, and not actually changing the blocks.
     *
     * @param chunk this OreChunk's real chunk.
     * @param player the player to show the visualisation to.
     */
    public void visualiseAir(@NotNull Chunk chunk, @NotNull Player player) {
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 256; y++) {
                for (int z = 0; z < 16; z++) {
                    Material material = ores.get(coordsToShort(x, y, z));
                    Location location = chunk.getBlock(x, y, z).getLocation();

                    // This could possibly be optimised using multi block change events.
                    // Though this is not natively accessible using just the Paper Spigot API,
                    // and would require NMS/ProtocolLib, which I currently don't want to use.

                    if (material != null)
                        player.sendBlockChange(location, material.createBlockData());
                    else
                        player.sendBlockChange(location, Material.AIR.createBlockData());
                }
            }
        }
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
    private @NotNull Block shortToBlock(short coords, @NotNull Chunk chunk) {
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

    /**
     * Tick event, called once per in-game tick.
     *
     * @param player the player which the OreChunk belongs to.
     * @param key the key for the OreChunk stored in the PlayerOreMetadata HashMap.
     */
    public void tick(Player player, Long key) {
        if (state.equals(OreChunkState.GENERATED))
            sendOres(player, key);
    }

    /**
     * Send the ores to a player using packets.
     *
     * @param player the player which the OreChunk belongs to.
     * @param key the key for the OreChunk stored in the PlayerOreMetadata HashMap.
     */
    private void sendOres(Player player, Long key) {
        Chunk chunk = player.getWorld().getChunkAt(key);

        for (Map.Entry<Short, Material> entry : ores.entrySet()) {
            Block block = shortToBlock(entry.getKey(), chunk);

            if (block.getType().equals(Material.STONE))
                player.sendBlockChange(block.getLocation(), entry.getValue().createBlockData());
        }

        state = OreChunkState.SENT;
    }

    /**
     * @param block block inside OreChunk to get the OreChunk material of.
     * @return material for that position in the OreChunk (null if there is no material at that position).
     */
    public @Nullable Material getMaterialAtBlock(Block block) {
        return ores.get(coordsToShort(block.getX() - (block.getChunk().getX() << 4), block.getY(), block.getZ() - (block.getChunk().getZ() << 4)));
    }

    /**
     * @return the current state of generation.
     */
    public @NotNull OreChunkState getState() {
        return state;
    }
}

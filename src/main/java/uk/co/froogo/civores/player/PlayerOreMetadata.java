package uk.co.froogo.civores.player;

import org.bukkit.metadata.MetadataValueAdapter;
import org.jetbrains.annotations.NotNull;
import uk.co.froogo.civores.CivOres;
import uk.co.froogo.civores.generation.OreChunk;

import java.util.HashMap;

/**
 * Metadata on every player containing all of their cached OreChunks.
 */
public class PlayerOreMetadata extends MetadataValueAdapter {
    /**
     * Metadata key.
     */
    public static final String key = "civores-player-ore";

    /**
     * Map of cached OreChunks for a player.
     *
     * The key is a Spigot chunk key, the value is an OreChunk.
     */
    private final @NotNull HashMap<Long, OreChunk> oreChunkMap;

    /**
     * Construct new empty PlayerOreMetadata for players on connection.
     */
    public PlayerOreMetadata() {
        super(CivOres.getInstance());
        oreChunkMap = new HashMap<>();
    }

    public @NotNull HashMap<Long, OreChunk> getOreChunkMap() {
        return oreChunkMap;
    }

    @Override
    public @NotNull HashMap<Long, OreChunk> value() {
        return oreChunkMap;
    }

    @Override
    public void invalidate() {
        oreChunkMap.clear();
    }
}

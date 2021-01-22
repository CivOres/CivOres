package uk.co.froogo.civores.player;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import uk.co.froogo.civores.CivOres;
import uk.co.froogo.civores.generation.OreChunk;

import java.util.Map;

/**
 * Ticker to call OreChunk.tick on every OreChunk inside every online player's metadata.
 */
public class PlayerOreMetadataTicker {
    private final @NotNull CivOres plugin;

    public PlayerOreMetadataTicker(@NotNull CivOres plugin) {
        this.plugin = plugin;
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 1L, 1L);
    }

    /**
     * Ticker called once per tick, used to call tick OreChunk.tick on every online player's OreChunk in their metadata.
     */
    private void tick() {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            // Player must have joined the server too recently to have metadata; continue.
            if (!player.hasMetadata(PlayerOreMetadata.key))
                continue;

            PlayerOreMetadata metadata = (PlayerOreMetadata) player.getMetadata(PlayerOreMetadata.key).get(0);

            for (Map.Entry<Long, OreChunk> entry : metadata.getOreChunkMap().entrySet())
                entry.getValue().tick(player, entry.getKey());
        }
    }
}

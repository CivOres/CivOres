package uk.co.froogo.civores.player;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.froogo.civores.CivOres;
import uk.co.froogo.civores.config.Config;
import uk.co.froogo.civores.generation.OreChunk;
import uk.co.froogo.civores.generation.OreGenerationSettings;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;

/**
 * Events related to generating and sending OreChunks to players.
 */
public class PlayerEvents implements Listener {
    public PlayerEvents(CivOres plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler(
            priority = EventPriority.HIGH,
            ignoreCancelled = true
    )
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Set the player's metadata.
        event.getPlayer().setMetadata(PlayerOreMetadata.key, new PlayerOreMetadata());
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPlayerMove(PlayerMoveEvent event) {
        onMove(event.getPlayer(), event.getFrom(), event.getTo());
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        onMove(event.getPlayer(), event.getFrom(), event.getTo());
    }

    private void onMove(Player player, Location from, Location to) {
        // Skip events when a player doesn't move to a new chunk.
        if (from.getChunk().equals(to.getChunk()))
            return;

        // Player must have joined the server too recently to have metadata; continue.
        if (!player.hasMetadata(PlayerOreMetadata.key))
            return;

        PlayerOreMetadata metadata = (PlayerOreMetadata) player.getMetadata(PlayerOreMetadata.key).get(0);

        int distance = Config.getInstance().getRenderDistance();
        int width = distance * 2 + 1;
        HashSet<Long> visibleChunks = new HashSet<>(width * width);
        for (int x = 0; x < width; x++)
            for (int z = 0; z < width; z++)
                visibleChunks.add(Chunk.getChunkKey(x - distance + player.getChunk().getX(), z - distance + player.getChunk().getZ()));

        metadata.getOreChunkMap().keySet().removeIf(key -> !visibleChunks.contains(key));

        UUID playerUUID = player.getUniqueId();
        UUID worldUUID = player.getWorld().getUID();

        for (Long key : visibleChunks) {
            OreChunk oreChunk = new OreChunk();
            metadata.getOreChunkMap().put(key, oreChunk);

            Biome biome = player.getWorld().getChunkAt(key).getBlock(7, 0, 7).getBiome();
            ArrayList<OreGenerationSettings> settings = Config.getInstance().getSettings(biome);

            // Generate the OreChunk asynchronously.
            new BukkitRunnable() {
                @Override
                public void run() {
                    oreChunk.generate(settings, (int) (key << 32 >> 28), (int) (key >> 28), playerUUID, worldUUID);
                }
            }.runTaskAsynchronously(CivOres.getInstance());
        }
    }

    @EventHandler(
            priority = EventPriority.HIGH,
            ignoreCancelled = true
    )
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.hasBlock())
            return;

        if (!event.getClickedBlock().getType().equals(Material.STONE))
            return;

        // Player must have joined the server too recently to have metadata; continue.
        if (!event.getPlayer().hasMetadata(PlayerOreMetadata.key))
            return;

        PlayerOreMetadata metadata = (PlayerOreMetadata) event.getPlayer().getMetadata(PlayerOreMetadata.key).get(0);

        OreChunk oreChunk = metadata.getOreChunkMap().get(event.getPlayer().getChunk().getChunkKey());
        if (oreChunk == null)
            return;

        Material material = oreChunk.getMaterialAtBlock(event.getClickedBlock());
        if (material == null)
            return;

        event.getClickedBlock().setType(material);
    }

    @EventHandler(
            priority = EventPriority.HIGH,
            ignoreCancelled = true
    )
    public void onBlockBreak(BlockBreakEvent event) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            // Player must have joined the server too recently to have metadata; continue.
            if (!player.hasMetadata(PlayerOreMetadata.key))
                continue;

            PlayerOreMetadata metadata = (PlayerOreMetadata) player.getMetadata(PlayerOreMetadata.key).get(0);

            OreChunk oreChunk = metadata.getOreChunkMap().get(event.getBlock().getChunk().getChunkKey());
            if (oreChunk == null)
                continue;

            oreChunk.onBlockBreak(event, player);
        }
    }
}

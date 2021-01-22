package uk.co.froogo.civores.player;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.froogo.civores.CivOres;
import uk.co.froogo.civores.generation.OreChunk;
import uk.co.froogo.civores.generation.OreGenerationSettings;

import java.util.ArrayList;

public class PlayerEvents implements Listener {
    public PlayerEvents(CivOres plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Set the player's metadata.
        event.getPlayer().setMetadata(PlayerOreMetadata.key, new PlayerOreMetadata());
    }

    @EventHandler(
            priority = EventPriority.HIGHEST,
            ignoreCancelled = true
    )
    public void onPlayerMove(PlayerMoveEvent event) {
        // Skip events when a player doesn't move to a new chunk.
        if (event.getFrom().getChunk().equals(event.getTo().getChunk()))
            return;

        // Player must have joined the server too recently to have metadata; continue.
        if (!event.getPlayer().hasMetadata(PlayerOreMetadata.key))
            return;

        PlayerOreMetadata metadata = (PlayerOreMetadata) event.getPlayer().getMetadata(PlayerOreMetadata.key).get(0);

        metadata.getOreChunkMap().clear();

        OreChunk oreChunk = new OreChunk();
        metadata.getOreChunkMap().put(event.getTo().getChunk().getChunkKey(), oreChunk);

        ArrayList<OreGenerationSettings> settings = new ArrayList<>();
        settings.add(new OreGenerationSettings(Material.COAL_ORE, 0.12f, 0.8f, 20f, 60f, 0.005f));
        settings.add(new OreGenerationSettings(Material.IRON_ORE, 0.14f, 0.85f, 20f, 40f, 0.005f));
        settings.add(new OreGenerationSettings(Material.GOLD_ORE, 0.14f, 0.9f, 20f, 30f, 0.005f));
        settings.add(new OreGenerationSettings(Material.REDSTONE_ORE, 0.14f, 0.9f, 20f, 30f, 0.005f));
        settings.add(new OreGenerationSettings(Material.LAPIS_ORE, 0.1f, 0.9f, 10f, 20f, 0.005f));
        settings.add(new OreGenerationSettings(Material.EMERALD_ORE, 0.16f, 0.93f, 10f, 20f, 0.005f));
        settings.add(new OreGenerationSettings(Material.DIAMOND_ORE, 0.1f, 0.9f, 11f, 11f, 0.005f));

        // Generate the OreChunk asynchronously.
        new BukkitRunnable() {
            @Override
            public void run() {
                oreChunk.generate(settings, event.getTo().getChunk().getX() << 4, event.getTo().getChunk().getZ() << 4, event.getPlayer().getUniqueId(), event.getPlayer().getWorld().getUID());
            }
        }.runTaskAsynchronously(CivOres.getInstance());
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.LEFT_CLICK_BLOCK))
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
}

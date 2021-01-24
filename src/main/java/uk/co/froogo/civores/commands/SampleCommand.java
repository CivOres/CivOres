package uk.co.froogo.civores.commands;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import uk.co.froogo.civores.CivOres;
import uk.co.froogo.civores.config.Config;
import uk.co.froogo.civores.generation.OreChunk;
import uk.co.froogo.civores.generation.OreChunkState;
import uk.co.froogo.civores.generation.OreGenerationSettings;

import java.util.ArrayList;

/**
 * Command to show a player a sample of currently generated ores.
 */
public class SampleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to execute this command.");
            return true;
        }

        Player player = (Player) sender;

        ArrayList<OreGenerationSettings> settings;

        if (args.length != 0) {
            if (args.length < 5) {
                sender.sendMessage("Not enough arguments.");
                return false;
            }

            float frequency;
            float minimum;
            float optimalYMin;
            float optimalYMax;
            float optimalYPunishment;

            try {
                frequency = Float.parseFloat(args[0]);
                minimum = Float.parseFloat(args[1]);
                optimalYMin = Float.parseFloat(args[2]);
                optimalYMax = Float.parseFloat(args[3]);
                optimalYPunishment = Float.parseFloat(args[4]);
            } catch (NumberFormatException e) {
                player.sendMessage("Could not parse arguments.");
                return true;
            }

            settings = new ArrayList<>();
            settings.add(new OreGenerationSettings(Material.DIAMOND_ORE, frequency, minimum, optimalYMin, optimalYMax, optimalYPunishment));
        } else {
            Biome biome = player.getChunk().getBlock(7, 0, 7).getBiome();
            settings = Config.getInstance().getSettings(biome);
        }

        Chunk chunk = player.getChunk();

        OreChunk oreChunk = new OreChunk();

        // Generate the OreChunk asynchronously.
        new BukkitRunnable() {
            @Override
            public void run() {
                oreChunk.generate(settings, chunk.getX() << 4, chunk.getZ() << 4, player.getUniqueId(), chunk.getWorld().getUID());
            }
        }.runTaskAsynchronously(CivOres.getInstance());

        new BukkitRunnable() {
            @Override
            public void run() {
                // Wait until the OreChunk is generated.
                if (oreChunk.getState().equals(OreChunkState.GENERATING))
                    return;

                // Cancel this runnable.
                cancel();

                oreChunk.visualiseAir(chunk, player);
            }
        }.runTaskTimer(CivOres.getInstance(), 1L, 1L);

        return true;
    }
}

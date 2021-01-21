package uk.co.froogo.civores;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import uk.co.froogo.civores.generation.OreChunk;
import uk.co.froogo.civores.generation.OreGenerationSettings;
import uk.co.froogo.civores.noise.FastNoise;

import java.util.ArrayList;

public class SampleCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        EntityDamageEvent event;
        if (!(sender instanceof Player)) {
            sender.sendMessage("You must be a player to execute this command.");
            return true;
        }

        Player player = (Player) sender;

        ArrayList<OreGenerationSettings> settings = new ArrayList<>();

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
            int seed;

            try {
                frequency = Float.parseFloat(args[0]);
                minimum = Float.parseFloat(args[1]);
                optimalYMin = Float.parseFloat(args[2]);
                optimalYMax = Float.parseFloat(args[3]);
                optimalYPunishment = Float.parseFloat(args[4]);

                if (args.length >= 6)
                    seed = Integer.parseInt(args[5]);
                else
                    seed = (int)(System.currentTimeMillis() % Integer.MAX_VALUE);
            } catch (NumberFormatException e) {
                player.sendMessage("Could not parse arguments.");
                return true;
            }

            FastNoise noise = new FastNoise();
            noise.SetSeed(seed);
            noise.SetNoiseType(FastNoise.NoiseType.OpenSimplex2S);
            noise.SetFrequency(frequency);
            noise.SetFractalType(FastNoise.FractalType.None);

            settings.add(new OreGenerationSettings(Material.DIAMOND_ORE, frequency, minimum, optimalYMin, optimalYMax, optimalYPunishment));
        } else {
            settings.add(new OreGenerationSettings(Material.COAL_ORE, 0.12f, 0.8f, 20f, 60f, 0.005f));
            settings.add(new OreGenerationSettings(Material.IRON_ORE, 0.14f, 0.85f, 20f, 40f, 0.005f));
            settings.add(new OreGenerationSettings(Material.GOLD_ORE, 0.14f, 0.9f, 20f, 30f, 0.005f));
            settings.add(new OreGenerationSettings(Material.REDSTONE_ORE, 0.14f, 0.9f, 20f, 30f, 0.005f));
            settings.add(new OreGenerationSettings(Material.LAPIS_ORE, 0.1f, 0.9f, 10f, 20f, 0.005f));
            settings.add(new OreGenerationSettings(Material.EMERALD_ORE, 0.16f, 0.93f, 10f, 20f, 0.005f));
            settings.add(new OreGenerationSettings(Material.DIAMOND_ORE, 0.1f, 0.9f, 11f, 11f, 0.005f));
        }

        Chunk chunk = player.getChunk();

        OreChunk oreChunk = new OreChunk(settings, chunk, player);
        oreChunk.visualiseAir(player.getChunk());

        return true;
    }
}

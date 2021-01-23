package uk.co.froogo.civores;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import uk.co.froogo.civores.noise.FastNoise;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Command to calculate the probability of an spawning ore with given settings.
 */
public class ProbabilityCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 7) {
            sender.sendMessage("Not enough arguments.");
            return false;
        }

        float frequency;
        float minimum;
        float optimalYMin;
        float optimalYMax;
        float optimalYPunishment;
        float y;
        int sampleSize;

        try {
            frequency = Float.parseFloat(args[0]);
            minimum = Float.parseFloat(args[1]);
            optimalYMin = Float.parseFloat(args[2]);
            optimalYMax = Float.parseFloat(args[3]);
            optimalYPunishment = Float.parseFloat(args[4]);
            y = Float.parseFloat(args[5]);
            sampleSize = Integer.parseInt(args[6]);
        } catch (NumberFormatException e) {
            sender.sendMessage("Could not parse arguments.");
            return true;
        }

        FastNoise noise = new FastNoise();
        noise.SetSeed((int)(System.currentTimeMillis() % Integer.MAX_VALUE));
        noise.SetNoiseType(FastNoise.NoiseType.OpenSimplex2S);
        noise.SetFrequency(frequency);
        noise.SetFractalType(FastNoise.FractalType.None);

        float min;
        if (y < optimalYMin)
            min = minimum + (Math.abs(y - optimalYMin) * optimalYPunishment);
        else if (y > optimalYMax)
            min = minimum + (Math.abs(y - optimalYMax) * optimalYPunishment);
        else
            min = minimum;

        sender.sendMessage("Calculated minimum: " + minimum);
        sender.sendMessage("Started calculating probability, this may take a moment depending on the sample size.");

        int count = 0;

        int sqrtSampleSize = (int) Math.sqrt(sampleSize);
        for (int x = 0; x < sqrtSampleSize; x++)
            for (int z = 0; z < sqrtSampleSize; z++)
                if (noise.GetNoise(x, y, z) >= min)
                    count++;

        sender.sendMessage((double) count / (double) (sqrtSampleSize * sqrtSampleSize) * 100.d + "% probability");
        return true;
    }
}

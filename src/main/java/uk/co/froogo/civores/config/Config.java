package uk.co.froogo.civores.config;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.bukkit.block.Biome;
import uk.co.froogo.civores.CivOres;
import uk.co.froogo.civores.generation.OreGenerationSettings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Config {
    public static final String location = "config.json";

    /**
     * Singleton instance of this config.
     */
    private static Config instance;

    private int renderDistance;

    private HashMap<String, OreGenerationSettings> presets;

    private ArrayList<String> defaultBiome;
    private ArrayList<OreGenerationSettings> defaultBiomeLoaded;

    private HashMap<Biome, ArrayList<String>> biomes;
    private HashMap<Biome, ArrayList<OreGenerationSettings>> biomesLoaded;

    /**
     * Load the configuration.
     */
    public static void init() {
        File file = new File(CivOres.getInstance().getDataFolder(), location);

        if (!file.exists())
            CivOres.getInstance().saveResource(location, false);

        Gson gson = new Gson();

        try {
            instance = gson.fromJson(new FileReader(file), new TypeToken<Config>(){}.getType());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // Load presets.
        for (Map.Entry<String, OreGenerationSettings> entry : instance.presets.entrySet())
            entry.getValue().init(entry.getKey());

        // Load default biome.
        instance.defaultBiomeLoaded = new ArrayList<>(instance.defaultBiome.size());

        for (String key : instance.defaultBiome) {
            OreGenerationSettings preset = instance.presets.get(key);
            if (preset == null) {
                CivOres.getInstance().getLogger().severe("Error loading " + location + "; unknown preset \"" + key + "\n in default biome");
                continue;
            }

            instance.defaultBiomeLoaded.add(preset);
        }

        // Load biomes.
        instance.biomesLoaded = new HashMap<>();
        for (Map.Entry<Biome, ArrayList<String>> entry : instance.biomes.entrySet()) {
            ArrayList<OreGenerationSettings> settings = new ArrayList<>(entry.getValue().size());

            for (String key : entry.getValue()) {
                OreGenerationSettings preset = instance.presets.get(key);
                if (preset == null) {
                    CivOres.getInstance().getLogger().severe("Error loading " + location + "; unknown preset \"" + key + "\n in biome \"" + entry.getKey().name() + "\"");
                    continue;
                }

                settings.add(preset);
            }

            instance.biomesLoaded.put(entry.getKey(), settings);
        }
    }

    public static Config getInstance() {
        return instance;
    }

    /**
     * @return the render distance in chunks of ores as the radius of a square around the player's chunk.
     */
    public int getRenderDistance() {
        return renderDistance;
    }

    /**
     * Get the OreGenerationSettings for a specific biome.
     *
     * @param biome biome to get the settings of.
     * @return specific settings of that biome, or the default biome if the biome does not exist.
     */
    public ArrayList<OreGenerationSettings> getSettings(Biome biome) {
        return biomesLoaded.getOrDefault(biome, defaultBiomeLoaded);
    }
}

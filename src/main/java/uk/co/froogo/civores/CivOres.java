package uk.co.froogo.civores;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * CivOres Paper Spigot plugin entry point.
 */
public final class CivOres extends JavaPlugin {
    @Override
    public void onEnable() {
        getCommand("sample").setExecutor(new SampleCommand());
    }

    @Override
    public void onDisable() {

    }
}

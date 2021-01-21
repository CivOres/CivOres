package uk.co.froogo.civores;

import org.bukkit.plugin.java.JavaPlugin;

public final class CivOres extends JavaPlugin {
    @Override
    public void onEnable() {
        getCommand("sample").setExecutor(new SampleCommand());
    }

    @Override
    public void onDisable() {

    }
}

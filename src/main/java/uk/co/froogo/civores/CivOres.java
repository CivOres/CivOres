package uk.co.froogo.civores;

import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import uk.co.froogo.civores.player.PlayerEvents;
import uk.co.froogo.civores.player.PlayerOreMetadataTicker;

/**
 * CivOres Paper Spigot plugin entry point.
 */
public final class CivOres extends JavaPlugin {
    private static CivOres instance;

    @Override
    public void onEnable() {
        // Set singleton reference to this.
        instance = this;

        // Start tickers.
        new PlayerOreMetadataTicker(this);

        // Register events.
        new PlayerEvents(this);

        // Register commands.
        getCommand("sample").setExecutor(new SampleCommand());
    }

    @Override
    public void onDisable() {

    }

    public static @NotNull CivOres getInstance() {
        return instance;
    }
}

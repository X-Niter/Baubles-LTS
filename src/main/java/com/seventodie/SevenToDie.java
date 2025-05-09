package com.seventodie;

import com.seventodie.api.BaubleType;
import com.seventodie.api.IBauble;
import com.seventodie.commands.BaublesCommand;
import com.seventodie.listeners.InventoryListener;
import com.seventodie.listeners.PlayerListener;
import com.seventodie.manager.BaubleManager;
import com.seventodie.util.Config;
import org.bukkit.plugin.java.JavaPlugin;

public final class SevenToDie extends JavaPlugin {

    private static SevenToDie instance;
    private BaubleManager baubleManager;
    private Config config;

    @Override
    public void onEnable() {
        instance = this;
        
        // Initialize configuration
        saveDefaultConfig();
        config = new Config(this);
        
        // Initialize the bauble manager
        baubleManager = new BaubleManager(this);
        
        // Register listeners
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        
        // Register commands
        getCommand("baubles").setExecutor(new BaublesCommand(this));
        
        getLogger().info("SevenToDie plugin has been enabled!");
        getLogger().info("7 bauble slots added (AMULET, RING x2, BELT, HEAD, BODY, CHARM)");
    }

    @Override
    public void onDisable() {
        // Save player data if needed
        if (baubleManager != null) {
            baubleManager.saveAllPlayerData();
        }
        
        getLogger().info("SevenToDie plugin has been disabled!");
    }

    /**
     * Get the plugin instance
     * @return The plugin instance
     */
    public static SevenToDie getInstance() {
        return instance;
    }
    
    /**
     * Get the bauble manager
     * @return The bauble manager
     */
    public BaubleManager getBaubleManager() {
        return baubleManager;
    }
    
    /**
     * Get the plugin config handler
     * @return The config handler
     */
    public Config getConfigHandler() {
        return config;
    }
}
package com.seventodie.util;

import com.seventodie.SevenToDie;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Handles configuration settings for the SevenToDie plugin
 */
public class Config {
    private final SevenToDie plugin;
    private final FileConfiguration config;
    
    // Default configuration values
    private boolean renderBaubles = true;
    private boolean enableRightClickEquip = true;
    private boolean saveDataOnLogout = true;
    
    public Config(SevenToDie plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfig();
        
        // Create default config if it doesn't exist
        plugin.saveDefaultConfig();
        
        // Load configuration
        loadConfig();
    }
    
    /**
     * Load configuration values from config.yml
     */
    private void loadConfig() {
        // Create default config values if they don't exist
        if (!config.isSet("render_baubles")) {
            config.set("render_baubles", renderBaubles);
        }
        if (!config.isSet("enable_right_click_equip")) {
            config.set("enable_right_click_equip", enableRightClickEquip);
        }
        if (!config.isSet("save_data_on_logout")) {
            config.set("save_data_on_logout", saveDataOnLogout);
        }
        
        // Save if we added any default values
        plugin.saveConfig();
        
        // Now load the values
        renderBaubles = config.getBoolean("render_baubles", true);
        enableRightClickEquip = config.getBoolean("enable_right_click_equip", true);
        saveDataOnLogout = config.getBoolean("save_data_on_logout", true);
    }
    
    /**
     * Check if the plugin should render baubles on players
     * @return True if baubles should be rendered
     */
    public boolean shouldRenderBaubles() {
        return renderBaubles;
    }
    
    /**
     * Check if right-click equipping is enabled
     * @return True if right-click equipping is enabled
     */
    public boolean isRightClickEquipEnabled() {
        return enableRightClickEquip;
    }
    
    /**
     * Check if player data should be saved on logout
     * @return True if data should be saved on logout
     */
    public boolean shouldSaveDataOnLogout() {
        return saveDataOnLogout;
    }
}
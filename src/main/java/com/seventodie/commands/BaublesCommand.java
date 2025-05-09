package com.seventodie.commands;

import com.seventodie.SevenToDie;
import com.seventodie.api.BaubleType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Handler for the /baubles command
 */
public class BaublesCommand implements CommandExecutor {
    private final SevenToDie plugin;
    
    public BaublesCommand(SevenToDie plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Open baubles inventory
            openBaublesInventory(player);
            return true;
        }
        
        if (args.length >= 1) {
            String subCommand = args[0].toLowerCase();
            
            switch (subCommand) {
                case "help":
                    showHelp(player);
                    break;
                case "list":
                    listBaubles(player);
                    break;
                default:
                    player.sendMessage(ChatColor.RED + "Unknown sub-command. Use /baubles help for help.");
                    break;
            }
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Open the baubles inventory for a player
     * @param player The player to open the inventory for
     */
    private void openBaublesInventory(Player player) {
        // In a real implementation, this would open a custom inventory GUI
        // For now, just send a message
        player.sendMessage(ChatColor.GREEN + "Opening baubles inventory (not implemented yet)");
        
        // Here's how you might implement it in the future:
        Inventory inventory = Bukkit.createInventory(player, 9, ChatColor.DARK_PURPLE + "Baubles");
        player.openInventory(inventory);
    }
    
    /**
     * Show help for the baubles command
     * @param player The player to show help to
     */
    private void showHelp(Player player) {
        player.sendMessage(ChatColor.GOLD + "===== Baubles Help =====");
        player.sendMessage(ChatColor.YELLOW + "/baubles - Open your baubles inventory");
        player.sendMessage(ChatColor.YELLOW + "/baubles help - Show this help");
        player.sendMessage(ChatColor.YELLOW + "/baubles list - List your equipped baubles");
    }
    
    /**
     * List all equipped baubles for a player
     * @param player The player
     */
    private void listBaubles(Player player) {
        player.sendMessage(ChatColor.GOLD + "===== Your Baubles =====");
        
        ItemStack[] baubles = plugin.getBaubleManager().getBaubles(player);
        
        for (int i = 0; i < baubles.length; i++) {
            ItemStack bauble = baubles[i];
            String slotName = getSlotName(i);
            
            if (bauble == null) {
                player.sendMessage(ChatColor.GRAY + slotName + ": " + ChatColor.RED + "Empty");
            } else {
                String itemName = bauble.hasItemMeta() && bauble.getItemMeta().hasDisplayName() 
                        ? bauble.getItemMeta().getDisplayName() 
                        : bauble.getType().toString();
                
                player.sendMessage(ChatColor.GRAY + slotName + ": " + ChatColor.GREEN + itemName);
            }
        }
    }
    
    /**
     * Get the name of a bauble slot based on its index
     * @param index The slot index
     * @return The human-readable name of the slot
     */
    private String getSlotName(int index) {
        switch (index) {
            case 0:
                return "Amulet";
            case 1:
            case 2:
                return "Ring " + (index);
            case 3:
                return "Belt";
            case 4:
                return "Head";
            case 5:
                return "Body";
            case 6:
                return "Charm";
            default:
                return "Unknown";
        }
    }
}
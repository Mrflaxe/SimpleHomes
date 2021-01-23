package ru.mrflaxe.simplehomes.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.permissions.PermissionAttachmentInfo;

import ru.mrflaxe.simplehomes.database.DatabaseManager;
import ru.mrflaxe.simplehomes.database.Home;
import ru.soknight.lib.configuration.Messages;

public class GUIManager {

    private final List<String> browses;
    private final List<String> changedMode;
    
    private final DatabaseManager dbManager;
    private final Messages messages;
    
    public GUIManager(DatabaseManager dbManager, Messages messages) {
        this.browses = new ArrayList<>();
        this.changedMode = new ArrayList<>();
        
        this.dbManager = dbManager;
        this.messages = messages;
    }
    
    /**
     * Opening the home menu with homes of current player
     * @param player
     */
    public void openMenu(Player player) {
        player.openInventory(initializeMenu(player));
        browses.add(player.getName());
    }
    
    /**
     * Removes the player from "browsing" list
     * @param player
     */
    public void removeBrowsing(Player player) {
        String name = player.getName();
        
        if(!browses.contains(name)) return;
        browses.remove(name);
    }
    
    /**
     * Checks if the player browsing menu now
     * @param player
     * @return true if player browsing and false if not
     */
    public boolean isBrowsing(Player player) {
        return browses.contains(player.getName());
    }
    
    // this method fils an inventory with homes of player and other items
    
    private Inventory initializeMenu(Player player) {
        List<Home> homes = dbManager.getHomesByPlayer(player.getName());
        int size = homes.size();
        int inventorySize = ((size - 1)/9 + 2) * 9;
        
        Inventory menu = Bukkit.createInventory(player, inventorySize, messages.getColoredString("gui.title"));
        
        for(int i = 0; i < size; i++) {
            Home home = homes.get(i);
            
            Material material = Material.getMaterial(home.getMaterial());
            ItemStack item = new ItemStack(material);
            
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("\u00a7e" + home.getHomeName());
            item.setItemMeta(meta);
            
            menu.setItem(i, item);
        }
        
        for(int i = inventorySize - 1; i >= inventorySize - 9; i--) {
            ItemStack edging = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
            ItemMeta meta = edging.getItemMeta();
            meta.setDisplayName(" ");
            edging.setItemMeta(meta);
            
            menu.setItem(i, edging );
        }
        
        
        
        // creating the sign with information about how many homes a player can create yet
        ItemStack limitItem = new ItemStack(Material.WARPED_SIGN);
        ItemMeta limitMeta = limitItem.getItemMeta();
        
        int limit = getHomesLimit(player);
        
        String name;
        if(limit <= 0) name = messages.getColoredString("gui.limit.ended");
        else name = messages.getFormatted("gui.limit.free", "%count%", limit);
        
        limitMeta.setDisplayName(name);
        limitMeta.setCustomModelData(1001);
        limitItem.setItemMeta(limitMeta);
        
        menu.setItem(inventorySize - 5, limitItem);
        
        
        
        // creating button what will changing modes
        ItemStack button = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta buttonMeta = button.getItemMeta();
        
        buttonMeta.setDisplayName(messages.getColoredString("gui.button.enable"));
        button.setItemMeta(buttonMeta);
        
        menu.setItem(inventorySize - 9, button);
        
        
        
        return menu;
    }
    
    
    public int getHomesLimit(Player player) {
        int limit = player.getEffectivePermissions()
                .parallelStream()
                .map(PermissionAttachmentInfo::getPermission)
                .filter(p -> p.startsWith("simplehomes.limit.multiple."))
                .filter(p -> p.length() > 27)
                .map(p -> p.substring(27))
                .map(this::getAsInteger)
                .filter(i -> i != null)
                .mapToInt(Integer::intValue)
                .map(i -> 0 - i)
                .sum();
        
        return limit;
    }
    
    private Integer getAsInteger(String source) {
        try {
            return Integer.parseInt(source);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
    
    /**
     * Adds the player to changeMode list
     * @param player
     */
    public void setChangedMode(Player player) {
        changedMode.add(player.getName());
    }
    
    /**
     * checks if the player contains in changeMode list
     * @param player
     * @return true if contains and false if not
     */
    public boolean isChangedMode(Player player) {
        return changedMode.contains(player.getName());
    }
    
    /**
     * removes the player from changeMode list
     * @param player
     */
    public void removeChangedMode (Player player) {
        changedMode.remove(player.getName());
    }
}


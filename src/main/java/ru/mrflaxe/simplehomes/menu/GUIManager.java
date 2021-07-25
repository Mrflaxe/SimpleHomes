package ru.mrflaxe.simplehomes.menu;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.mrflaxe.simplehomes.common.HomesLimitFetcher;
import ru.mrflaxe.simplehomes.database.DatabaseManager;
import ru.mrflaxe.simplehomes.database.model.HomeModel;
import ru.soknight.lib.configuration.Messages;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GUIManager {
    
    private final Messages messages;
    private final DatabaseManager databaseManager;

    private final Set<Player> browses;
    private final Set<Player> editorMode;
    
    public GUIManager(Messages messages, DatabaseManager dbManager) {
        this.messages = messages;
        this.databaseManager = dbManager;
        
        this.browses = new HashSet<>();
        this.editorMode = new HashSet<>();
    }
    
    public void closeAll() {
        if(!browses.isEmpty())
            browses.forEach(Player::closeInventory);
        
        browses.clear();
        editorMode.clear();
    }
    
    /**
     * Opening the home menu with homes of current player
     * @param player target player
     */
    public void openMenu(Player player) {
        player.openInventory(initializeMenu(player));
        browses.add(player);
    }
    
    /**
     * Removes the player from "browsing" list
     * @param player target player
     */
    public void removeBrowsing(Player player) {
        browses.remove(player);
    }
    
    /**
     * Checks if the player browsing menu now
     * @param player target player
     * @return true if player browsing and false if not
     */
    public boolean isBrowsing(Player player) {
        return browses.contains(player);
    }
    
    // this method fils an inventory with homes of player and other items
    private Inventory initializeMenu(Player player) {
        List<HomeModel> homeModels = databaseManager.getHomes(player.getName()).join();
        
        int size = homeModels.size();
        int inventorySize = ((size - 1)/9 + 2) * 9;
        
        Inventory menu = Bukkit.createInventory(player, inventorySize, messages.getColoredString("gui.title"));
        
        for(int i = 0; i < size; i++) {
            HomeModel homeModel = homeModels.get(i);
            
            Material material = homeModel.getMaterial();
            ItemStack item = new ItemStack(material);
            
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.GREEN + homeModel.getHomeName());
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
        
        int limit = HomesLimitFetcher.getHomesLimit(player);
        
        String name;
        if(limit <= 0) name = messages.getColoredString("gui.counter.unavailable");
        else name = messages.getFormatted("gui.counter.available", "%count%", limit);
        
        limitMeta.setDisplayName(name);
        limitMeta.setCustomModelData(1001);
        limitItem.setItemMeta(limitMeta);
        
        menu.setItem(inventorySize - 5, limitItem);
        
        // creating button what will changing modes
        ItemStack button = new ItemStack(Material.LIME_STAINED_GLASS_PANE);
        ItemMeta buttonMeta = button.getItemMeta();
        
        buttonMeta.setDisplayName(messages.getColoredString("gui.editor.tip.enable"));
        button.setItemMeta(buttonMeta);
        
        menu.setItem(inventorySize - 9, button);
        return menu;
    }
    
    /**
     * Adds the player to changeMode list
     * @param player target player
     */
    public void setEditorMode(Player player) {
        editorMode.add(player);
    }
    
    /**
     * Checks if the player contains in changeMode list
     * @param player target player
     * @return true if contains and false if not
     */
    public boolean isEditorMode(Player player) {
        return editorMode.contains(player);
    }
    
    /**
     * Removes the player from changeMode list
     * @param player target player
     */
    public void removeEditorMode(Player player) {
        editorMode.remove(player);
    }
}


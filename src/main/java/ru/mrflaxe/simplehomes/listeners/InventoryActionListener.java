package ru.mrflaxe.simplehomes.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;
import ru.mrflaxe.simplehomes.database.Home;
import ru.mrflaxe.simplehomes.managers.GUIManager;
import ru.mrflaxe.simplehomes.managers.HomeManager;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.cooldown.preset.LitePlayersCooldownStorage;

public class InventoryActionListener implements Listener {
    
    private final LitePlayersCooldownStorage cooldown;
    
    private final Messages messages;
    private final HomeManager homeManager;
    private final GUIManager guiManager;
    
    public InventoryActionListener(LitePlayersCooldownStorage cooldown, Messages messages, HomeManager homeManager, GUIManager guiManager) {
        this.cooldown = cooldown;
        
        this.messages = messages;
        this.homeManager = homeManager;
        this.guiManager = guiManager;
    }
    
    @EventHandler
    public void onClcik(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack clickedItem = e.getCurrentItem();
        
        if(clickedItem == null) return;
        
        // If a player don't browse menu
        if(!guiManager.isBrowsing(player)) return;
        
        Material type = clickedItem.getType();
        Inventory inv = e.getClickedInventory();
        
        // if player clicked on common items
        if(type == Material.BLUE_STAINED_GLASS_PANE || type == Material.WARPED_SIGN) {
            e.setCancelled(true);
            return;
        }
        
        // if player clicked on change buttons
        if(type == Material.LIME_STAINED_GLASS_PANE || type == Material.RED_STAINED_GLASS_PANE) {
            e.setCancelled(true);
            
            // TODO I think this place can be more clear
            
            Material material = Material.LIME_STAINED_GLASS_PANE;
            String buttonName = messages.getColoredString("gui.button.enable");
            String message = messages.getColoredString("gui.button.disabled");
            
            if(type == Material.LIME_STAINED_GLASS_PANE) {
                guiManager.setChangedMode(player);
                
                material = Material.RED_STAINED_GLASS_PANE;
                buttonName = messages.getColoredString("gui.button.disable");
                message = messages.getColoredString("gui.button.enabled");
            } else  guiManager.removeChangedMode(player);
            
            ItemStack button = new ItemStack(material);
            ItemMeta buttonMeta = button.getItemMeta();
            buttonMeta.setDisplayName(buttonName);
            button.setItemMeta(buttonMeta);
            
            inv.setItem(inv.getSize() - 9, button);
            player.sendMessage(message);
            
            return;
        }
        
        // If a player click in changing mode
        if(guiManager.isChangedMode(player)) {
            
            // This string checks if inventory is a home menu
            // In the menu the item in a given slot have custom model data which I use for identification it
            // This data setts in GUIManager class on string #107
            
            if(inv.getItem(inv.getSize() - 5).getItemMeta().getCustomModelData() != 1001) return;
            
            e.setCancelled(true);
            ItemStack cursor = e.getCursor();
            
            // if a player clicking on empty slots it will returns
            if(!clickedItem.hasItemMeta()) return;
            
            // So now I know that player clicked on a home item and now
            // I gonna change icons of homes how wants the player
            String homeName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
            Home home = homeManager.getHome(player, homeName);
            
            // If cursor is empty I will return icon to default
            if(cursor == null) {
                
                // If item is already default
                if(home.getMaterial() == home.chooseMaterial().toString()) return;
                
                
                changeMaterial(home, null, e.getSlot(), inv, player);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1, 1);
                return;
            }

            changeMaterial(home, cursor, e.getSlot(), inv, player);
            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
            return;
        }
        
        e.setCancelled(true);
        
        if(!clickedItem.hasItemMeta()) return;
        
        String homeName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        
        Home home = homeManager.getHome(player, homeName);
        player.teleport(home.getLocation());
        cooldown.refreshResetDate(player.getName());
        messages.sendFormatted(player, "command.home.success.by-name", "%name%", homeName);
    }
    
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        
        if(guiManager.isBrowsing(player)) guiManager.removeBrowsing(player);
        if(guiManager.isChangedMode(player)) {
            guiManager.removeChangedMode(player);
            messages.getAndSend(player, "gui.button.disabled");
        }
    }
    
    /**
     * This method changes material of item in given inventory in a given slot
     * to material of ItemStack from cursor or if a cursor is null to default material
     * 
     * @param home - Home object
     * @param cursor - ItemStack on a cursor
     * @param slot - number of slot in the inventory
     * @param inv - the inventory
     * @param player - current player
     */
    private void changeMaterial(Home home, ItemStack cursor, int slot, Inventory inv, Player player) {
        ItemStack item;
        
        if(cursor == null) {
            Material material = home.chooseMaterial();
            home.setMaterial(material.toString());
            
            item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName("\u00a7e" + home.getHomeName());
            item.setItemMeta(meta);
            
            inv.setItem(slot, item);
        } else {
            home.setMaterial(cursor.getType().toString());
            
            Material material = cursor.getType();
            
            item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta(); // How I understood meta is null but why?
            meta.setDisplayName("\u00a7e" + home.getHomeName()); // TODO there is error
            item.setItemMeta(meta);
        }
        
        homeManager.refreshHome(home);
        inv.setItem(slot, item);
    }
    
    public void register(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}

package ru.mrflaxe.simplehomes.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
        if(guiManager.isChangedMode(player)) {
            // TODO Обработчик изменения иконок
        }
        
        e.setCancelled(true);
        
        if(!clickedItem.hasItemMeta()) return;
        
        Material type = clickedItem.getType();
        
        if(type == Material.BLUE_STAINED_GLASS_PANE || type == Material.WARPED_SIGN) return;
        if(type == Material.LIME_STAINED_GLASS_PANE || type == Material.RED_STAINED_GLASS_PANE) {
            Inventory inv = e.getClickedInventory();
            
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
    
    public void register(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}

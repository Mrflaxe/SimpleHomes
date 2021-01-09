package ru.mrflaxe.simplehomes.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import net.md_5.bungee.api.ChatColor;
import ru.mrflaxe.simplehomes.database.Home;
import ru.mrflaxe.simplehomes.managers.GUIManager;
import ru.mrflaxe.simplehomes.managers.HomeManager;
import ru.soknight.lib.configuration.Messages;

public class InventoryActionListener implements Listener {
    
    private final Messages messages;
    private final HomeManager homeManager;
    private final GUIManager guiManager;
    
    public InventoryActionListener(Messages messages, HomeManager homeManager, GUIManager guiManager) {
        this.messages = messages;
        this.homeManager = homeManager;
        this.guiManager = guiManager;
    }
    
    @EventHandler
    public void onClcik(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        
        // If a player don't browse menu
        if(!guiManager.isBrowsing(player)) return;
        
        e.setCancelled(true);
        
        ItemStack clickedItem = e.getCurrentItem();
        if(clickedItem == null) return;
        if(!clickedItem.hasItemMeta()) return;
        
        Material type = clickedItem.getType();
        if(type == Material.BLUE_STAINED_GLASS_PANE || type == Material.WARPED_SIGN) return;
        
        String homeName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
        
        Home home = homeManager.getHome(player, homeName);
        player.teleport(home.getLocation());
        messages.sendFormatted(player, "command.home.success.by-name", "%name%", homeName);
    }
    
    @EventHandler
    public void onClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        
        if(guiManager.isBrowsing(player)) guiManager.removeBrowsing(player);
    }
    
    public void register(Plugin plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }
}

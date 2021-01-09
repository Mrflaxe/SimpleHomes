package ru.mrflaxe.simplehomes.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
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
    
    private final DatabaseManager dbManager;
    private final Messages messages;
    
    public GUIManager(DatabaseManager dbManager, Messages messages) {
        this.browses = new ArrayList<>();
        
        this.dbManager = dbManager;
        this.messages = messages;
    }
    
    public void openMenu(Player player) {
        player.openInventory(initializeMenu(player));
        browses.add(player.getName());
    }
    
    public void removeBrowsing(Player player) {
        String name = player.getName();
        
        if(!browses.contains(name)) return;
        browses.remove(name);
    }
    
    public boolean isBrowsing(Player player) {
        return browses.contains(player.getName());
    }
    
    private Inventory initializeMenu(Player player) {
        List<Home> homes = dbManager.getHomesByPlayer(player.getName());
        int size = homes.size();
        int inventorySize = ((size - 1)/9 + 2) * 9;
        
        Inventory menu = Bukkit.createInventory(player, inventorySize, messages.getColoredString("gui.title"));
        
        for(int i = 0; i < size; i++) {
            Home home = homes.get(i);
            
            ItemStack item = chooseMaterial(home.getLocation());
            
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
        
        ItemStack limitItem = new ItemStack(Material.WARPED_SIGN);
        ItemMeta meta = limitItem.getItemMeta();
        
        int limit = getHomesLimit(player);
        
        String name;
        if(limit <= 0) name = messages.getColoredString("gui.limit.ended");
        else name = messages.getFormatted("gui.limit.free", "%count%", limit);
        
        meta.setDisplayName(name);
        limitItem.setItemMeta(meta);
        
        menu.setItem(inventorySize - 5, limitItem);
        
        return menu;
    }
    
    private ItemStack chooseMaterial(Location location) {
        if(location.getBlockY() <= 30) return new ItemStack(Material.STONE);
        
        String world = location.getWorld().getName();
        String biome = location.getBlock().getBiome().toString();
        
        if(world.equals("world")) {
            if(biome.startsWith("MOUNTAIN") || biome.equals("GRAVELLY_MOUNTAINS") || biome.equals("STONE_SHORE")) return new ItemStack(Material.STONE);
            if(biome.startsWith("BADLANDS") || biome.endsWith("BADLANDS_PLATEAU") || biome.equals("ERODED_BADLANDS")) return new ItemStack(Material.ORANGE_TERRACOTTA);
            if(biome.startsWith("FROZEN") || biome.equals("ICE_SPIKES")) return new ItemStack(Material.ICE);
            if(biome.startsWith("DEEP_") || biome.endsWith("OCEAN")) return new ItemStack(Material.BRAIN_CORAL);
            if(biome.startsWith("JUNGLE") || biome.startsWith("BAMBOO")) return new ItemStack(Material.JUNGLE_LOG);
            if(biome.startsWith("DESERT") || biome.equals("BEACH")) return new ItemStack(Material.SAND);
            if(biome.startsWith("BRICH") || biome.startsWith("TALL")) return new ItemStack(Material.BIRCH_LOG);
            if(biome.startsWith("TAIGA") || biome.startsWith("GIANT")) return new ItemStack(Material.SPRUCE_LOG);
            if(biome.startsWith("SAVANNA") || biome.startsWith("SHATTERED")) return new ItemStack(Material.ACACIA_LOG);
            if(biome.startsWith("SNOWY")) return new ItemStack(Material.SNOW_BLOCK);
            if(biome.startsWith("SWAMP")) return new ItemStack(Material.LILY_PAD);
            if(biome.startsWith("DARK")) return new ItemStack(Material.DARK_OAK_LOG);
        }
        
        if(world.equals("world_nether")) {
            if(biome.startsWith("BASALT")) return new ItemStack(Material.BASALT);
            if(biome.startsWith("CRIMSON")) return new ItemStack(Material.CRIMSON_STEM);
            if(biome.startsWith("SOUL")) return new ItemStack(Material.SOUL_SAND);
            if(biome.startsWith("WARPED")) return new ItemStack(Material.WARPED_STEM);
            return new ItemStack(Material.NETHERRACK);
        }
        
        if(world.equals("world_the_end")) return new ItemStack(Material.END_STONE);
        
        return new ItemStack(Material.GRASS_BLOCK);
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
}


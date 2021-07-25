package ru.mrflaxe.simplehomes.menu.item;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import ru.mrflaxe.simplehomes.exception.ItemStackParsingException;
import ru.mrflaxe.simplehomes.exception.UnknownItemTypeException;
import ru.mrflaxe.simplehomes.menu.session.MenuSession;
import ru.mrflaxe.simplehomes.menu.util.SlotsFetcher;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class SimpleItem implements MenuItem {

    private final ItemStack baseItem;
    private final int[] slots;
    
    @Override
    public ItemStack render(MenuSession session) {
        return baseItem != null ? baseItem.clone() : null;
    }
    
    @Override
    public boolean isLocatedIn(int slot) {
        return Arrays.stream(slots).anyMatch(i -> i == slot);
    }
    
    public static void updateItemName(ItemStack item, Function<String, String> updater) {
        if(item == null) return;
        
        ItemMeta meta = item.getItemMeta();
        if(meta == null)
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        
        if(meta == null || !meta.hasDisplayName()) return;
        
        String updated = updater.apply(meta.getDisplayName());
        meta.setDisplayName(updated);
        
        item.setItemMeta(meta);
    }
    
    public static void remapItemLore(ItemStack item, Function<String, String> mapper) {
        if(item == null) return;
        
        ItemMeta meta = item.getItemMeta();
        if(meta == null)
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        
        if(meta == null || !meta.hasLore()) return;
        
        List<String> remapped = meta.getLore().stream().map(mapper).collect(Collectors.toList());
        meta.setLore(remapped);
        
        item.setItemMeta(meta);
    }
    
    public static SimpleItem parse(ConfigurationSection config) {
        if(config == null) return null;
        
        ItemStack item = parsePattern(config);
        int[] slots = SlotsFetcher.fetchSlotsArray(config, ", ");
        
        return new SimpleItem(item, slots);
    }
    
    public static ItemStack parsePattern(ConfigurationSection config) {
        if(config == null) return null;
        
        ItemStack item;
        if(!config.isString("material"))
            throw new ItemStackParsingException("item type isn't specified");
        
        try {
            Material type = Material.valueOf(config.getString("material").toUpperCase());
            item = new ItemStack(type);
        } catch (EnumConstantNotPresentException ex) {
            throw new UnknownItemTypeException(config.getString("type"));
        }
        
        ItemMeta meta = item.getItemMeta();
        if(meta == null)
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        
        meta.addItemFlags(ItemFlag.values());
        
        if(config.isString("name")) {
            String displayname = colorize(config.getString("name"));
            meta.setDisplayName(displayname);
        }
        
        if(config.isList("lore")) {
            List<String> lore = config.getStringList("lore").stream()
                    .map(SimpleItem::colorize)
                    .collect(Collectors.toList());
            meta.setLore(lore);
        }
        
        if(config.getBoolean("enchanted", false))
            meta.addEnchant(Enchantment.ARROW_INFINITE, 1, true);
        
        meta.addItemFlags(ItemFlag.values());
        item.setItemMeta(meta);
        return item;
    }
    
    private static String colorize(String original) {
        return ChatColor.translateAlternateColorCodes('&', original);
    }
    
}

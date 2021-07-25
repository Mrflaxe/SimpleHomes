package ru.mrflaxe.simplehomes.menu;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrflaxe.simplehomes.menu.item.SimpleItem;
import ru.mrflaxe.simplehomes.menu.item.StateableItem;
import ru.mrflaxe.simplehomes.menu.util.SlotsFetcher;
import ru.soknight.lib.configuration.Configuration;

import java.util.Arrays;

@Getter
public class MenuConfiguration extends Configuration {
    
    private ItemStack homePattern;
    
    private SimpleItem informationItem;
    private SimpleItem bannerItem;
    
    private StateableItem editorItem;
    private StateableItem nextPageItem;
    private StateableItem prevPageItem;
    
    private int[] displaySlots;
    
    public MenuConfiguration(JavaPlugin plugin) {
        super(plugin, "menu.yml");
    }
    
    public Inventory createInventory(InventoryHolder holder) {
        String title = getColoredString("interface.title", "");
        int rows = getInt("interface.rows", 6);
        
        if(rows < 1) rows = 1;
        else if(rows > 6) rows = 6;
        
        return Bukkit.createInventory(holder, rows * 9, title);
    }
    
    public boolean isDisplaySlot(int slot) {
        return Arrays.stream(displaySlots).anyMatch(i -> i == slot);
    }
    
    private void loadPatterns() {
        this.homePattern = SimpleItem.parsePattern(getSection("home"));
        
        this.informationItem = SimpleItem.parse(getSection("information"));
        this.bannerItem = SimpleItem.parse(getSection("banner"));
        
        this.editorItem = StateableItem.parse(
                getSection("editor.active"),
                getSection("editor.inactive")
        );
        
        this.nextPageItem = StateableItem.parse(
                getSection("next-page.available"),
                getSection("next-page.unavailable")
        );
        
        this.prevPageItem = StateableItem.parse(
                getSection("previous-page.available"),
                getSection("previous-page.unavailable")
        );
        
        String slots = getString("display-slots", "");
        this.displaySlots = SlotsFetcher.fetchSlotsFromRanges(slots, ", ").stream()
                .mapToInt(Integer::intValue)
                .toArray();
    }
    
    @Override
    public void refresh() {
        super.refresh();
        
        loadPatterns();
    }
    
}

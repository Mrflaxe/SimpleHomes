package ru.mrflaxe.simplehomes.menu;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrflaxe.simplehomes.common.HomesLimitFetcher;
import ru.mrflaxe.simplehomes.database.DatabaseManager;
import ru.mrflaxe.simplehomes.database.model.HomeModel;
import ru.mrflaxe.simplehomes.menu.item.SimpleItem;
import ru.mrflaxe.simplehomes.menu.session.MenuSession;
import ru.mrflaxe.simplehomes.menu.session.MenuSessionStorage;
import ru.soknight.lib.tool.CollectionsTool;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

@Getter
public class MenuProvider {
    
    private final NamespacedKey homeItemKey;

    private final MenuConfiguration menuConfiguration;
    private final MenuSessionStorage sessionStorage;
    private final DatabaseManager databaseManager;
    
    public MenuProvider(JavaPlugin plugin, DatabaseManager databaseManager) {
        this.menuConfiguration = new MenuConfiguration(plugin);
        this.sessionStorage = new MenuSessionStorage();
        this.databaseManager = databaseManager;
        
        this.homeItemKey = new NamespacedKey(plugin, "home_item");
    }
    
    public void closeAll() {
        sessionStorage.closeAll();
    }
    
    public void refresh() {
        closeAll();
        
        menuConfiguration.refresh();
    }
    
    public MenuSession openSession(Player holder, List<HomeModel> homes) {
        int homesAmount = homes != null ? homes.size() : 0;
        int pageSize = menuConfiguration.getDisplaySlots().length;
        
        int totalPages = homesAmount / pageSize;
        if(homesAmount % pageSize != 0) totalPages++;
        
        Inventory inventory = menuConfiguration.createInventory(holder);
        MenuSession session = new MenuSession(holder, inventory, homes, totalPages);
        reloadContent(session);
        
        sessionStorage.openSession(session);
        return session;
    }
    
    public boolean switchEditorMode(MenuSession menuSession) {
        Inventory inventory = menuSession.getInventory();
        boolean isActive = !menuSession.isEditorActive();
        menuSession.getEditorActive().set(isActive);
        
        // adding editor item
        SimpleItem editorPattern = menuConfiguration.getEditorItem().getItem(isActive);
        fill(inventory, editorPattern.getSlots(), editorPattern.render(menuSession));
        return isActive;
    }
    
    public void reloadContent(MenuSession menuSession) {
        Inventory inventory = menuSession.getInventory();
        inventory.clear();
        
        // gathering some data
        int currentPage = menuSession.getCurrentPage().get();
        int totalPages = menuSession.getTotalPages();
        
        int homesAmount = menuSession.getHomesList().size();
        int homesLimit = HomesLimitFetcher.getHomesLimit(menuSession.getHolder());
        
        int homesAvailableAmount = homesLimit != -1 ? homesLimit - homesAmount : -1;
        if(homesLimit != -1 && homesAvailableAmount < 0) homesAvailableAmount = 0;
        
        String homesAvailable = homesAvailableAmount != -1
                ? String.valueOf(homesAvailableAmount)
                : menuConfiguration.getColoredString("information.unlimited", "");
        
        // getting list of homes on the current page
        int pageSize = menuConfiguration.getDisplaySlots().length;
        List<HomeModel> homes = menuSession.getHomesList();
        List<HomeModel> onpage = homes != null
                ? CollectionsTool.getSubList(homes, pageSize, currentPage)
                : Collections.emptyList();
        
        // adding banners
        ItemStack bannerItem = menuConfiguration.getBannerItem().render(menuSession);
        fill(inventory, menuConfiguration.getBannerItem().getSlots(), bannerItem);
        
        // adding page switchers
        SimpleItem nextPageItem = menuConfiguration.getNextPageItem().getItem(menuSession.hasNextPage());
        SimpleItem prevPageItem = menuConfiguration.getPrevPageItem().getItem(menuSession.hasPrevPage());
        
        fill(inventory, nextPageItem.getSlots(), nextPageItem.render(menuSession));
        fill(inventory, prevPageItem.getSlots(), prevPageItem.render(menuSession));
        
        // adding information
        ItemStack infoPattern = menuConfiguration.getInformationItem().render(menuSession);
        SimpleItem.remapItemLore(infoPattern, line -> menuConfiguration.format(line,
                "%page%", currentPage,
                "%total%", totalPages,
                "%homes_amount%", homesAmount,
                "%homes_available%", homesAvailable
        ));

        fill(inventory, menuConfiguration.getInformationItem().getSlots(), infoPattern);
        
        // adding editor item
        SimpleItem editorPattern = menuConfiguration.getEditorItem().getItem(menuSession.isEditorActive());
        fill(inventory, editorPattern.getSlots(), editorPattern.render(menuSession));
        
        // adding homes content
        ItemStack homeItemPattern = menuConfiguration.getHomePattern();
        int[] slots = menuConfiguration.getDisplaySlots();
        Iterator<HomeModel> iterator = onpage.iterator();
        
        int index = 0;
        while(iterator.hasNext()) {
            if(index >= slots.length) break;
            
            HomeModel home = iterator.next();
            ItemStack homeItem = getHomeItem(homeItemPattern, home);
            
            inventory.setItem(slots[index], homeItem);
            index++;
        }
    }
    
    private ItemStack getHomeItem(ItemStack original, HomeModel homeModel) {
        ItemStack clone = original.clone();
        clone.setType(homeModel.getMaterial());
        
        String homeName = homeModel.getHomeName();
        
        SimpleItem.updateItemName(clone, name -> name.replace("%home_name%", homeName));
        SimpleItem.remapItemLore(clone, line -> line.replace("%home_name%", homeName));
        
        setPersistentValue(clone, homeItemKey, homeName);
        return clone;
    }
    
    private void setPersistentValue(ItemStack item, NamespacedKey key, String value) {
        ItemMeta meta = item.getItemMeta();
        if(meta == null)
            meta = Bukkit.getItemFactory().getItemMeta(item.getType());
        
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        dataContainer.set(key, PersistentDataType.STRING, value);
        
        item.setItemMeta(meta);
    }
    
    private static void fill(Inventory inventory, int[] slots, ItemStack item) {
        Arrays.stream(slots).forEach(slot -> inventory.setItem(slot, item));
    }
    
}

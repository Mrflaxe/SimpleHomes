package ru.mrflaxe.simplehomes.listener;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import ru.mrflaxe.simplehomes.database.DatabaseManager;
import ru.mrflaxe.simplehomes.menu.MenuConfiguration;
import ru.mrflaxe.simplehomes.menu.MenuProvider;
import ru.mrflaxe.simplehomes.menu.item.SimpleItem;
import ru.mrflaxe.simplehomes.menu.session.MenuSession;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.cooldown.preset.PlayersCooldownStorage;
import ru.soknight.lib.format.DateFormatter;

public class InventoryActionListener implements Listener {
    
    private static final DateFormatter DATE_FORMATTER;
    
    private final Plugin plugin;
    private final Configuration config;
    private final Messages messages;
    
    private final DatabaseManager databaseManager;
    private final MenuProvider menuProvider;
    
    private final PlayersCooldownStorage cooldownStorage;
    
    public InventoryActionListener(
            Plugin plugin,
            Configuration config,
            Messages messages,
            DatabaseManager databaseManager,
            MenuProvider menuProvider,
            PlayersCooldownStorage cooldownStorage
    ) {
        this.plugin = plugin;
        this.config = config;
        this.messages = messages;
        
        this.databaseManager = databaseManager;
        this.menuProvider = menuProvider;
        
        this.cooldownStorage = cooldownStorage;
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        HumanEntity whoClicked = event.getWhoClicked();
        if(!(whoClicked instanceof Player)) return;
        
        Player player = (Player) whoClicked;
        MenuSession session = menuProvider.getSessionStorage().getSession(player);
        if(session == null) return;
        
        InventoryView view = event.getView();
        Inventory clickedInventory = event.getClickedInventory();
        if(view == null || clickedInventory == null) return;
        
        if(event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            return;
        }
        
        Inventory topInventory = view.getTopInventory();
        if(topInventory != clickedInventory) return;
        if(event.getAction() == InventoryAction.CLONE_STACK) return;
        
        ItemStack currentItem = event.getCurrentItem();
        ItemStack cursor = event.getCursor();
        
        int slot = event.getSlot();
        MenuConfiguration menuConfig = menuProvider.getMenuConfiguration();
        
        // if home item slot was clicked
        if(menuConfig.isDisplaySlot(slot)) {
            String homeName = getPersistentValue(currentItem, menuProvider.getHomeItemKey());
            
            if(session.isEditorActive()) {
                if(currentItem != null && cursor != null) {
                    Material currentType = currentItem.getType();
                    Material cursorType = cursor.getType();
                    
                    if(homeName != null && !currentType.isAir() && !cursorType.isAir() && currentType != cursorType) {
                        currentItem.setType(cursorType);
                        session.updateHomeIcon(homeName, cursorType);
                        updateIcon(player, homeName, cursorType);
                        event.setResult(Result.DENY);
                        event.setCancelled(true);
                        return;
                    }
                }

                event.setResult(Result.DENY);
                event.setCancelled(true);
                return;
            } else if(homeName != null) {
                if(cursor == null || cursor.getType().isAir())
                    teleport(player, homeName);
                    
                event.setCancelled(true);
                return;
            }
        }
            
        // if next page button was clicked
        if(session.hasNextPage()) {
            SimpleItem item = menuConfig.getNextPageItem().getItem(true);
            if(item.isLocatedIn(slot)) {
                session.goToNextPage();
                menuProvider.reloadContent(session);
                event.setCancelled(true);
                return;
            }
        }
        
        // if previous page button was clicked
        if(session.hasPrevPage()) {
            SimpleItem item = menuConfig.getPrevPageItem().getItem(true);
            if(item.isLocatedIn(slot)) {
                session.goToPrevPage();
                menuProvider.reloadContent(session);
                event.setCancelled(true);
                return;
            }
        }
        
        // if editor button was clicked
        SimpleItem editorItem = menuConfig.getEditorItem().getItem(session.isEditorActive());
        if(editorItem.isLocatedIn(slot))
            menuProvider.switchEditorMode(session);

        event.setResult(Result.DENY);
        event.setCancelled(true);
    }
    
    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        HumanEntity whoClosed = event.getPlayer();
        if(!(whoClosed instanceof Player)) return;
        
        Player player = (Player) whoClosed;
        menuProvider.getSessionStorage().closeSession(player);
    }
    
    private void teleport(Player player, String homeName) {
        player.closeInventory();
        
        // cooldown check
        if(config.getBoolean("cooldown.enable", false) && !player.hasPermission("simplehomes.bypass.cooldown")) {
            long remainedTime = cooldownStorage.getRemainedTime(player.getName());
            if(remainedTime != -1) {
                int remainedSeconds = (int) (remainedTime / 1000);
                if(remainedTime % 1000 != 0) remainedSeconds++;
                
                if(remainedSeconds > 0) {
                    messages.sendFormatted(player, "home.failed.cooldown",
                            "%time%", DATE_FORMATTER.format(remainedSeconds)
                    );
                    return;
                }
            }
        }
        
        databaseManager.getHome(player.getName(), homeName).thenAcceptAsync(homeModel -> {
            if(homeModel == null) {
                messages.sendFormatted(player, "home.failed.unknown-home", "%name%", homeName);
                return;
            }
            
            Location location = homeModel.getLocation();
            if(location == null) {
                messages.getAndSend(player, "home.failed.unknown-world");
                return;
            }
            
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.teleport(location));
            cooldownStorage.refreshResetDate(player.getName());
            
            messages.sendFormatted(player, "home.success.by-name", "%name%", homeName);
        });
    }
    
    private void updateIcon(Player player, String homeName, Material type) {
        databaseManager.getHome(player.getName(), homeName).thenAcceptAsync(homeModel -> {
            if(homeModel == null) {
                messages.sendFormatted(player, "menu.update-icon.failed.unknown-home", "%name%", homeName);
                return;
            }
            
            homeModel.changeIcon(type.toString());
            databaseManager.saveHome(homeModel).join();
            
            messages.sendFormatted(player, "menu.update-icon.success", "%home%", homeName);
        });
    }
    
    private String getPersistentValue(ItemStack item, NamespacedKey key) {
        if(item == null) return null;
        
        ItemMeta meta = item.getItemMeta();
        if(meta == null) return null;
        
        PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
        return dataContainer.get(key, PersistentDataType.STRING);
    }
    
    static {
        DATE_FORMATTER = new DateFormatter();
        DATE_FORMATTER.loadRussianTimeUnitFormats();
    }
    
//    @EventHandler(priority = EventPriority.LOWEST)
//    public void onInventoryClick(InventoryClickEvent event) {
//        HumanEntity whoClicked = event.getWhoClicked();
//        if(!(whoClicked instanceof Player)) return;
//
//        ItemStack clickedItem = event.getCurrentItem();
//        if(clickedItem == null) return;
//
//        // if a player don't browse menu
//        Player player = (Player) whoClicked;
//        if(!guiManager.isBrowsing(player)) return;
//
//        Material type = clickedItem.getType();
//        Inventory clickedInventory = event.getClickedInventory();
//
//        // if player clicked on common items
//        if(type == Material.BLUE_STAINED_GLASS_PANE || type == Material.WARPED_SIGN) {
//            event.setCancelled(true);
//            return;
//        }
//
//        // if player clicked on change buttons
//        if(type == Material.LIME_STAINED_GLASS_PANE || type == Material.RED_STAINED_GLASS_PANE) {
//            event.setCancelled(true);
//
//            // TODO I think this place can be more clear
//
//            Material material = Material.LIME_STAINED_GLASS_PANE;
//            String buttonName = messages.getColoredString("gui.button.enable");
//            String message = messages.getColoredString("gui.button.disabled");
//
//            if(type == Material.LIME_STAINED_GLASS_PANE) {
//                guiManager.setChangedMode(player);
//
//                material = Material.RED_STAINED_GLASS_PANE;
//                buttonName = messages.getColoredString("gui.button.disable");
//                message = messages.getColoredString("gui.button.enabled");
//            } else  guiManager.removeChangedMode(player);
//
//            ItemStack button = new ItemStack(material);
//            ItemMeta buttonMeta = button.getItemMeta();
//            buttonMeta.setDisplayName(buttonName);
//            button.setItemMeta(buttonMeta);
//
//            clickedInventory.setItem(clickedInventory.getSize() - 9, button);
//            player.sendMessage(message);
//
//            return;
//        }
//
//        // If a player click in changing mode
//        if(guiManager.isChangedMode(player)) {
//
//            // This string checks if inventory is a home menu
//            // In the menu the item in a given slot have custom model data which I use for identification it
//            // This data setts in GUIManager class on string #107
//
//            if(clickedInventory.getItem(clickedInventory.getSize() - 5).getItemMeta().getCustomModelData() != 1001) return;
//
//            event.setCancelled(true);
//            ItemStack cursor = event.getCursor();
//
//            // if a player clicking on empty slots it will returns
//            if(!clickedItem.hasItemMeta()) return;
//
//            // So now I know that player clicked on a home item and now
//            // I gonna change icons of homes how wants the player
//            String homeName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
//            HomeModel homeModel = homeManager.getHome(player, homeName);
//
//            // If cursor is empty I will return icon to default
//            if(cursor == null) {
//
//                // If item is already default
//                if(homeModel.getMaterial() == homeModel.chooseMaterial().toString()) return;
//
//
//                changeMaterial(homeModel, null, event.getSlot(), clickedInventory, player);
//                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_BREAK, 1, 1);
//                return;
//            }
//
//            changeMaterial(homeModel, cursor, event.getSlot(), clickedInventory, player);
//            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_USE, 1, 1);
//            return;
//        }
//
//        event.setCancelled(true);
//
//        if(!clickedItem.hasItemMeta()) return;
//
//        String homeName = ChatColor.stripColor(clickedItem.getItemMeta().getDisplayName());
//        HomeModel homeModel = homeManager.getHome(player, homeName);
//
//        player.teleport(homeModel.getLocation());
//        cooldownStorage.refreshResetDate(player.getName());
//
//        messages.sendFormatted(player, "home.success.by-name", "%name%", homeName);
//    }
//
//    @EventHandler
//    public void onInventoryClose(InventoryCloseEvent event) {
//        Player player = (Player) event.getPlayer();
//
//        if(guiManager.isBrowsing(player))
//            guiManager.removeBrowsing(player);
//
//        if(guiManager.isEditorMode(player)) {
//            guiManager.removeEditorMode(player);
//            messages.getAndSend(player, "gui.editor.chat.disabled");
//        }
//    }
//
//    /**
//     * This method changes material of item in given inventory in a given slot
//     * to material of ItemStack from cursor or if a cursor is null to default material
//     *
//     * @param homeModel - Home object
//     * @param cursor - ItemStack on a cursor
//     * @param slot - number of slot in the inventory
//     * @param inv - the inventory
//     * @param player - current player
//     */
//    private void changeMaterial(HomeModel homeModel, ItemStack cursor, int slot, Inventory inv, Player player) {
//        ItemStack item;
//
//        if(cursor == null) {
//            Material material = homeModel.chooseMaterial();
//            homeModel.setMaterial(material.toString());
//
//            item = new ItemStack(material);
//            ItemMeta meta = item.getItemMeta();
//            meta.setDisplayName("\u00a7e" + homeModel.getHomeName());
//            item.setItemMeta(meta);
//
//            inv.setItem(slot, item);
//        } else {
//            homeModel.setMaterial(cursor.getType().toString());
//
//            Material material = cursor.getType();
//
//            item = new ItemStack(material);
//            ItemMeta meta = item.getItemMeta(); // How I understood meta is null but why?
//            meta.setDisplayName("\u00a7e" + homeModel.getHomeName()); // TODO there is error
//            item.setItemMeta(meta);
//        }
//
//        homeManager.refreshHome(homeModel);
//        inv.setItem(slot, item);
//    }
//
//    public void register(Plugin plugin) {
//        Bukkit.getPluginManager().registerEvents(this, plugin);
//    }
}

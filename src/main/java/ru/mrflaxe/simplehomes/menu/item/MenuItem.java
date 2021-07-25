package ru.mrflaxe.simplehomes.menu.item;

import org.bukkit.inventory.ItemStack;

import ru.mrflaxe.simplehomes.menu.session.MenuSession;

public interface MenuItem {

    ItemStack render(MenuSession session);
    
    boolean isLocatedIn(int slot);
    
}

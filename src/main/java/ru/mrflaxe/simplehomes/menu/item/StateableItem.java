package ru.mrflaxe.simplehomes.menu.item;

import org.bukkit.configuration.ConfigurationSection;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StateableItem {

    private final SimpleItem activeItem;
    private final SimpleItem inactiveItem;
    
    public SimpleItem getItem(boolean isActive) {
        return isActive ? activeItem : inactiveItem;
    }
    
    public static StateableItem parse(
            ConfigurationSection activeItemConfig,
            ConfigurationSection inactiveItemConfig
    ) {
        SimpleItem active = activeItemConfig != null ? SimpleItem.parse(activeItemConfig) : null;
        SimpleItem inactive = inactiveItemConfig != null ? SimpleItem.parse(inactiveItemConfig) : null;
        return new StateableItem(active, inactive);
    }
    
}

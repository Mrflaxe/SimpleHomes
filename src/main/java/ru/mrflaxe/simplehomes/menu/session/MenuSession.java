package ru.mrflaxe.simplehomes.menu.session;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import ru.mrflaxe.simplehomes.database.model.HomeModel;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Getter @Setter
public class MenuSession {

    private final Player holder;
    private final Inventory inventory;
    private final List<HomeModel> homesList;
    private final int totalPages;
    
    private final AtomicBoolean editorActive = new AtomicBoolean(false);
    private final AtomicInteger currentPage = new AtomicInteger(1);
    
    public MenuSession(Player holder, Inventory inventory, List<HomeModel> homesList, int totalPages) {
        this.holder = holder;
        this.inventory = inventory;
        this.homesList = homesList;
        this.totalPages = totalPages;
    }
    
    public void view() {
        holder.openInventory(inventory);
    }
    
    public void updateHomeIcon(String homeName, Material iconMaterial) {
        homesList.stream()
                .filter(home -> home.getHomeName().equals(homeName))
                .forEach(home -> home.changeIcon(iconMaterial.toString()));
    }
    
    public boolean isEditorActive() {
        return editorActive.get();
    }
    
    public boolean hasNextPage() {
        return currentPage.get() < totalPages;
    }
    
    public boolean hasPrevPage() {
        return currentPage.get() > 1;
    }
    
    public boolean goToNextPage() {
        if(!hasNextPage()) return false;
        
        currentPage.incrementAndGet();
        return true;
    }
    
    public boolean goToPrevPage() {
        if(!hasPrevPage()) return false;
        
        currentPage.decrementAndGet();
        return true;
    }
    
    public String getHolderName() {
        return holder.getName();
    }
    
    public void closeInventory() {
        holder.closeInventory();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        MenuSession that = (MenuSession) o;
        return Objects.equals(holder, that.holder) &&
                Objects.equals(editorActive, that.editorActive) &&
                Objects.equals(currentPage, that.currentPage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(holder, editorActive, currentPage);
    }

    @Override
    public String toString() {
        return "MenuSession{" +
                "holder=" + holder +
                ", homesList=" + homesList +
                ", totalPages=" + totalPages +
                ", editorActive=" + editorActive +
                ", currentPage=" + currentPage +
                '}';
    }

}

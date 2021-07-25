package ru.mrflaxe.simplehomes.menu.session;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class MenuSessionStorage {

    private final Map<String, MenuSession> sessions = new HashMap<>();
    
    public MenuSession getSession(Player holder) {
        return sessions.get(holder.getName());
    }
    
    public MenuSession openSession(MenuSession session) {
        sessions.put(session.getHolderName(), session);
        return session;
    }
    
    public boolean hasSession(Player holder) {
        return sessions.containsKey(holder.getName());
    }
    
    public void closeSession(Player holder) {
        sessions.remove(holder.getName());
    }
    
    public void closeAll() {
        sessions.values().forEach(MenuSession::closeInventory);
        sessions.clear();
    }
    
}

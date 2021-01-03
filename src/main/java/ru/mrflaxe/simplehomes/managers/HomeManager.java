package ru.mrflaxe.simplehomes.managers;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import ru.mrflaxe.simplehomes.database.DatabaseManager;
import ru.mrflaxe.simplehomes.database.Home;

public class HomeManager {

	private final DatabaseManager dbManager;
	
	public HomeManager(DatabaseManager databaseManager) {
		this.dbManager = databaseManager;
	}
	
	public int getHomeCount(Player player) {
		return dbManager.getHomesByPlayer(player.getName()).size();
	}

	public Home getFirstHome(Player player) {
		return dbManager.getHomesByPlayer(player.getName()).get(0);
	}
	
	public Home getHome(Player player, String homeName) {
		return dbManager.getHome(player.getName(), homeName);
	}
	
	public boolean isExist(Player player, String homeName) {
		return dbManager.getHome(player.getName(), homeName) != null;
	}
	
	public void createHome(Player player, String name, Location location) {
		dbManager.createHome(player.getName(), name, location.getWorld().getName(),
		        location.getYaw(),
		        location.getPitch(),
		        location.getBlockX(),
		        location.getBlockY(),
		        location.getBlockZ());
	}
	
	public void deleteHome(Player player, String name) {
	    dbManager.deleteHome(player.getName(), name);
	}
	
	public List<Home> getHomes(Player player) {
	    return dbManager.getHomesByPlayer(player.getName());
	}
}

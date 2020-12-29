package ru.mrflaxe.simplehomes.managers;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;

import ru.mrflaxe.simplehomes.database.DatabaseManager;
import ru.mrflaxe.simplehomes.database.Home;

public class HomeManager {

	private final DatabaseManager dbManager;
	
	public HomeManager(DatabaseManager databaseManager) {
		this.dbManager = databaseManager;
	}
	
	public int getHomeCount(String playerName) {
		return dbManager.getHomesByPlayer(playerName).size();
	}

	public Home getFirstHome(String playerName) {
		return dbManager.getHomesByPlayer(playerName).get(0);
	}
	
	public Home getHome(String playerName, String homeName) {
		return dbManager.getHome(playerName, homeName);
	}
	
	public boolean isExist(String playerName, String name) {
		List<Home> exists = dbManager.getHomesByPlayer(playerName).parallelStream()
						.filter(h -> h.getName() == name)
						.collect(Collectors.toList());
		if(exists.size() > 0) return true;
		return false;
	}
	
	public void createHome(String playerName, String name, Location location) {
		dbManager.createHome(playerName, name, location.getWorld().getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}
}

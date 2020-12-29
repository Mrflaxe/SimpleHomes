package ru.mrflaxe.simplehomes.database;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;

import ru.mrflaxe.simplehomes.SimpleHomes;

public class DatabaseManager {

	private final Logger logger;
	private final ConnectionSource connection;
	
	private final Dao<Home, String> homeDao;
	
	public DatabaseManager(SimpleHomes plugin, Database database) throws SQLException {
		logger = plugin.getLogger();
		connection = database.getConnection();
		
		this.homeDao = DaoManager.createDao(connection, Home.class);
	}
	
	// Добавление в БД точки дома
	
	public void createHome(String playerName, String name, String world, int x, int y, int z) {
		try {
			Home home = new Home(playerName, name, world, x, y, z);
			homeDao.create(home);
		} catch (SQLException e) {
			logger.severe("Failed to create the home data in database: " + e.getMessage());
			return;
		}
	}
	
	// Удаление из БД точки дома
	
	public void deleteHome(String plyaerName, String name) {
		Home home = getHome(plyaerName, name);
		
		try {
			homeDao.delete(home);
		} catch (SQLException e) {
			logger.severe("Failed to delete a home data in database: " + e.getMessage());
			return;
		}
	}
	
	// Получение объекта точки дома из БД по имени игрока и имени точки дома
	
	public Home getHome(String playerName, String homeName) {
		List<Home> homes = getHomesByPlayer(playerName);
		List<Home> filted = homes.parallelStream()
				.filter(h -> h.getName().equals(homeName))
				.collect(Collectors.toList());
		
		if(filted.isEmpty() || filted.size() > 1) {
			logger.severe("Exeption on 50th line of DatabaseManager class: number of homes with name " + homeName + " of " + playerName + " player is 0 or more then 1");
			return null;
		}
		
		return filted.get(0);
	}
	
	// Получение всех объектов существующих точек дома у определённго игрока
	
	public List<Home> getHomesByPlayer(String playerName) {
		try {
			return homeDao.queryForEq("playerName", playerName);
		} catch (SQLException e) {
			logger.severe("Failed to get homes by player's name from database: " + e.getMessage());
			return null;
		}
	}
}
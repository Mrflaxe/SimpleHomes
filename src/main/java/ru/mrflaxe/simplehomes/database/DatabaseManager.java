package ru.mrflaxe.simplehomes.database;

import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

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
	
	public void createHome(String playerName, String name, String world, float yaw, float pitch, int x, int y, int z) {
		try {
			Home home = new Home(playerName, name, world, yaw, pitch, x, y, z);
			homeDao.create(home);
		} catch (SQLException e) {
			logger.severe("Failed to create the home data in database: " + e.getMessage());
			return;
		}
	}
	
	
	public void refreshHome(Home home) {
	    try {
            homeDao.update(home);
        } catch (SQLException e) {
            logger.severe("Failed to update the home data in database: " + e.getMessage());
        }
	}
	
	public void deleteHome(String plyaerName, String name) {
		Home home = getHome(plyaerName, name);
		
		try {
			homeDao.delete(home);
		} catch (SQLException e) {
			logger.severe("Failed to delete a home data in database: " + e.getMessage());
			return;
		}
	}
	
	public Home getHome(String playerName, String homeName) {
	    try {
            return homeDao.queryBuilder().where()
                        .eq("player_name", playerName).and()
                        .eq("home_name", homeName)
                        .queryForFirst();
        } catch (Exception ex) {
            
            return null;
        }
	}
	
	/**
	 * Returns a list with all homes that belong to a player
	 * 
	 * @param playerName - name of player
	 * @return List of homes or null if happened an exception
	 */
	public List<Home> getHomesByPlayer(String playerName) {
		try {
			return homeDao.queryForEq("player_name", playerName);
		} catch (SQLException e) {
			logger.severe("Failed to get homes by player's name from database: " + e.getMessage());
			return null;
		}
	}
}

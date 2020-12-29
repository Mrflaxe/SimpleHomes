package ru.mrflaxe.simplehomes.database;

import java.io.File;
import java.sql.SQLException;

import org.bukkit.plugin.Plugin;

import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import ru.soknight.lib.configuration.Configuration;

public class Database {
	
	private String url;
	private String user;
	private String password;
	
	private boolean useSQLite;
	
	public Database(Plugin plugin, Configuration config) throws Exception {
		useSQLite = config.getBoolean("database.use-sqlite");
		if(!useSQLite) {
			String host = config.getString("database.host", "localhost");
			String name = config.getString("database.name", "betterprison");
			int port = config.getInt("database.port", 3306);
			
			this.user = config.getString("database.user", "admin");
			this.password = config.getString("database.password", "betterprison");
			this.url = "jdbc:mysql://" + host + ":" + port + "/" + name;
			
			Class.forName("com.mysql.jdbc.Driver").newInstance();
		} else {
			String file = config.getString("database.file", "database.db");
			this.url = "jdbc:sqlite:" + plugin.getDataFolder() + File.separator + file;
			
			Class.forName("org.sqlite.JDBC").newInstance();
		}
		
		// Allowing only ORMLite errors logging
		System.setProperty("com.j256.ormlite.logger.type", "LOCAL");
		System.setProperty("com.j256.ormlite.logger.level", "ERROR");
						
		ConnectionSource source = getConnection();
				
		 TableUtils.createTableIfNotExists(source, Home.class);
				
		source.close();
				
		plugin.getLogger().info("Database type " + (useSQLite ? "SQLite" : "MySQL") + " connected.");
				
	}
	
	public ConnectionSource getConnection() throws SQLException {
		return useSQLite ? new JdbcConnectionSource(url, user, password) : new JdbcConnectionSource(url);
	}

}

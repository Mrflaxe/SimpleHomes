package ru.mrflaxe.simplehomes;

import org.bukkit.plugin.java.JavaPlugin;

import ru.mrflaxe.simplehomes.commands.CommandHome;
import ru.mrflaxe.simplehomes.commands.CommandSethome;
import ru.mrflaxe.simplehomes.database.Database;
import ru.mrflaxe.simplehomes.database.DatabaseManager;
import ru.mrflaxe.simplehomes.managers.HomeManager;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;

public class SimpleHomes extends JavaPlugin {
	
	private Configuration config;
	private Messages messages;
	
	private DatabaseManager databaseManager;
	private HomeManager homeManager;
	
	@Override
	public void onEnable() {
		initConfigs();
		
		try {
			Database database = new Database(this, config);
			this.databaseManager = new DatabaseManager(this, database);
		} catch (Exception e) {
			getLogger().severe("Failed to connect SQLite database");
			e.printStackTrace();
			getServer().getPluginManager().disablePlugin(this);
			return;
		}
		
		this.homeManager = new HomeManager(databaseManager);
		
		registerCommands();
	}

	private void initConfigs() {
		this.config = new Configuration(this, "config.yml");
		this.config.refresh();
		
		this.messages = new Messages(this, "messages");
		this.messages.refresh();
	}
	
	private void registerCommands() {
		new CommandHome(messages, homeManager).register(this);
		new CommandSethome(messages, config, homeManager).register(this);
	}
}

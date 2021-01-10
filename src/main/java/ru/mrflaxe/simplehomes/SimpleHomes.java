package ru.mrflaxe.simplehomes;

import org.bukkit.plugin.java.JavaPlugin;

import ru.mrflaxe.simplehomes.commands.CommandDelhome;
import ru.mrflaxe.simplehomes.commands.CommandHome;
import ru.mrflaxe.simplehomes.commands.CommandSethome;
import ru.mrflaxe.simplehomes.commands.SubcommandHandler;
import ru.mrflaxe.simplehomes.database.Database;
import ru.mrflaxe.simplehomes.database.DatabaseManager;
import ru.mrflaxe.simplehomes.listeners.InventoryActionListener;
import ru.mrflaxe.simplehomes.managers.GUIManager;
import ru.mrflaxe.simplehomes.managers.HomeManager;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.cooldown.preset.LitePlayersCooldownStorage;

public class SimpleHomes extends JavaPlugin {

    private LitePlayersCooldownStorage cooldownStorage;
    
	private Configuration config;
	private Messages messages;
	
	private DatabaseManager databaseManager;
	private HomeManager homeManager;
	private GUIManager guiManager;

	
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
		
		this.cooldownStorage = new LitePlayersCooldownStorage(config.getInt("cooldown.delay"));
		
		this.homeManager = new HomeManager(databaseManager);
		this.guiManager = new GUIManager(databaseManager, messages);
		
		new InventoryActionListener(cooldownStorage, messages, homeManager, guiManager).register(this);
		
		registerCommands();
	}

	private void initConfigs() {
		this.config = new Configuration(this, "config.yml");
		this.config.refresh();
		
		this.messages = new Messages(this, "messages.yml");
		this.messages.refresh();
	}
	
	private void registerCommands() {
	    new SubcommandHandler(this, messages, config);
	    
		new CommandHome(messages, config, cooldownStorage, homeManager, guiManager).register(this);
		new CommandSethome(messages, config, homeManager, guiManager).register(this);
		new CommandDelhome(messages, homeManager).register(this);
	}
}

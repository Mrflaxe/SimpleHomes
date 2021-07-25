package ru.mrflaxe.simplehomes;

import org.bukkit.plugin.java.JavaPlugin;
import ru.mrflaxe.simplehomes.command.*;
import ru.mrflaxe.simplehomes.database.DatabaseManager;
import ru.mrflaxe.simplehomes.database.model.HomeModel;
import ru.mrflaxe.simplehomes.listener.InventoryActionListener;
import ru.mrflaxe.simplehomes.menu.MenuProvider;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.cooldown.preset.LitePlayersCooldownStorage;
import ru.soknight.lib.cooldown.preset.PlayersCooldownStorage;
import ru.soknight.lib.database.Database;

import java.sql.SQLException;

public class SimpleHomes extends JavaPlugin {
    
	private Configuration config;
	private Messages messages;
	
	private DatabaseManager databaseManager;
	private MenuProvider menuProvider;

    private PlayersCooldownStorage cooldownStorage;
	
	@Override
    public void onEnable() {
        loadConfigurations();
        
        // database initialization
        try {
            Database database = new Database(this, config)
                    .createTable(HomeModel.class)
                    .complete();
            this.databaseManager = new DatabaseManager(this, database);
        } catch (SQLException ex) {
            getLogger().severe("Failed to establish a database connection: " + ex.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        } catch (Exception ex) {
            getLogger().severe("Failed to initialize the database!");
            ex.printStackTrace();
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        // cooldowns storage & menu manager initialization
		this.cooldownStorage = new LitePlayersCooldownStorage(config.getInt("cooldown.delay"));
		this.menuProvider = new MenuProvider(this, databaseManager);
        
        // commands executors registration
        registerCommands();
        
        // event listeners registration
        registerListeners();
    }
    
    @Override
    public void onDisable() {
        // closing all opened menus
        if(menuProvider != null)
            menuProvider.closeAll();
        
        // close-up the database connection
        if(databaseManager != null)
            databaseManager.shutdown();
    }
    
    private void loadConfigurations() {
        this.config = new Configuration(this, "config.yml");
        this.config.refresh();
        
        this.messages = new Messages(this, "messages.yml");
        this.messages.refresh();
    }
    
    private void registerCommands() {
        new CommandSimplehomes(this, messages);
	    
		new CommandHome(this, config, messages, databaseManager, menuProvider, cooldownStorage);
		new CommandHomes(this, messages, databaseManager);
        new CommandSethome(this, config, messages, databaseManager);
		new CommandDelhome(this, messages, databaseManager);
    }
    
    private void registerListeners() {
        new InventoryActionListener(this, config, messages, databaseManager, menuProvider, cooldownStorage);
    }
    
    public void reload() {
        config.refresh();
        messages.refresh();
        
        menuProvider.refresh();
        
        cooldownStorage = new LitePlayersCooldownStorage(config.getInt("cooldown.delay"));
        registerCommands();
    }
    
}

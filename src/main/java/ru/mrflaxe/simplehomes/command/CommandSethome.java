package ru.mrflaxe.simplehomes.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import org.bukkit.plugin.java.JavaPlugin;
import ru.mrflaxe.simplehomes.common.HomesLimitFetcher;
import ru.mrflaxe.simplehomes.database.DatabaseManager;
import ru.mrflaxe.simplehomes.database.model.HomeModel;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.OmnipotentCommand;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;

public class CommandSethome extends OmnipotentCommand {

	private final Configuration config;
	private final Messages messages;
	private final DatabaseManager databaseManager;

	public CommandSethome(
			JavaPlugin plugin,
			Configuration config,
			Messages messages,
			DatabaseManager databaseManager
	) {
		super("sethome", null, "simplehomes.command.sethome", 1, messages);

		this.config = config;
		this.messages = messages;
		this.databaseManager = databaseManager;

		super.register(plugin);
	}

	@Override
	protected void executeCommand(CommandSender sender, CommandArguments args) {
		Player player = (Player) sender;
		
		databaseManager.getHomesAmount(player.getName()).thenAccept(homesAmount -> {
		    int homesLimit = HomesLimitFetcher.getHomesLimit(player);
		    
		    // can a player make more homes than he has now?
	        if(homesLimit != -1 && homesAmount >= homesLimit) {
	            messages.sendFormatted(sender, "sethome.failed.limit-reached", "%limit%", homesLimit);
	            return;
	        }
	        
	        String homeName = args.get(0);
	        int length = homeName.length();
	        
	        int minLength = config.getInt("homes.min-name-length", 2);
	        int maxLength = config.getInt("homes.max-name-length", 32);
	        
	        // check for length of home name
	        if(length < minLength || length > maxLength) {
	            messages.sendFormatted(sender, "sethome.failed.out-of-bounds",
	                    "%min-length%", minLength,
	                    "%max-length%", maxLength
	             );
	            return;
	        }
	        
	        // is already exist this home or not
	        if(databaseManager.hasHome(player.getName(), homeName).join()) {
	            messages.sendFormatted(sender, "sethome.failed.already-exists", "%name%", homeName);
	            return;
	        }
	        
	        // creating home
	        HomeModel home = new HomeModel(player.getName(), homeName, player.getLocation());
	        databaseManager.saveHome(home);
	        
	        messages.sendFormatted(player, "sethome.success", "%name%", homeName);
		}).exceptionally(t -> {
		    t.printStackTrace();
		    return null;
		});
	}
}

package ru.mrflaxe.simplehomes.commands;


import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.mrflaxe.simplehomes.database.Home;
import ru.mrflaxe.simplehomes.managers.GUIManager;
import ru.mrflaxe.simplehomes.managers.HomeManager;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.PlayerOnlyCommand;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.cooldown.preset.LitePlayersCooldownStorage;

public class CommandHome extends PlayerOnlyCommand {

    private final LitePlayersCooldownStorage cooldown;
    
	private final Messages messages;
	private final Configuration config;
	
	private final HomeManager homeManager;
	private final GUIManager guiManager;
	
	public CommandHome(Messages messages, Configuration config, LitePlayersCooldownStorage cooldown, HomeManager homeManager, GUIManager guiManager) {
		super("home", "simplehomes.command.home", messages);
		
		this.cooldown = cooldown;
		
		this.messages = messages;
		this.config = config;
		
		this.homeManager = homeManager;
		this.guiManager = guiManager;
	}

	@Override
	protected void executeCommand(CommandSender sender, CommandArguments args) {
		Player player = (Player) sender;
		String name = player.getName();
		
		if(args.size() == 0) {
			if(homeManager.getHomeCount(player) == 0) {
				messages.getAndSend(sender, "command.home.error.empty");
				return;
			}
			
			// cooldown check
			if(config.getBoolean("cooldown.enable", false)) {
			    long remainedTime = cooldown.getRemainedTime(name);
			        
			    if(remainedTime > 0) {
			        messages.sendFormatted(player, "command.home.error.cooldown", "%seconds%", remainedTime / 1000);
			        return;
			    }
			}
			
			// if a player has only 1 home
			if(homeManager.getHomeCount(player) == 1) {
				Home home = homeManager.getFirstHome(player);
				player.teleport(home.getLocation());
	            cooldown.refreshResetDate(name);
				
				messages.getAndSend(sender, "command.home.success.by-first");
				return;
			} else {
			    guiManager.openMenu(player);
			    return;
			}
			
		}
		
		// teleporting by home's name
		String homeName = args.get(0);
		
		Home home = homeManager.getHome(player, homeName);
		if(home == null) {
			messages.getAndSend(sender, "command.home.error.not-exist");
			return;
		}
		
		player.teleport(home.getLocation());
		cooldown.refreshResetDate(name);
		messages.sendFormatted(sender, "command.home.success.by-name", "%name%", homeName);
	}

}

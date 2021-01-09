package ru.mrflaxe.simplehomes.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.mrflaxe.simplehomes.database.Home;
import ru.mrflaxe.simplehomes.managers.GUIManager;
import ru.mrflaxe.simplehomes.managers.HomeManager;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.PlayerOnlyCommand;
import ru.soknight.lib.configuration.Messages;

public class CommandHome extends PlayerOnlyCommand {

	private final Messages messages;
	private final HomeManager homeManager;
	private final GUIManager guiManager;
	
	public CommandHome(Messages messages, HomeManager homeManager, GUIManager guiManager) {
		super("home", "simplehomes.command.home", messages);
		
		this.messages = messages;
		this.homeManager = homeManager;
		this.guiManager = guiManager;
	}

	@Override
	protected void executeCommand(CommandSender sender, CommandArguments args) {
		Player player = (Player) sender;
		
		if(args.size() == 0) {
			if(homeManager.getHomeCount(player) == 0) {
				messages.getAndSend(sender, "command.home.error.empty");
				return;
			}
			
			// if a player has only 1 home
			if(homeManager.getHomeCount(player) == 1) {
				Home home = homeManager.getFirstHome(player);
				player.teleport(home.getLocation());
				
				messages.getAndSend(sender, "command.home.success.by-first");
				return;
			} else {
			    guiManager.openMenu(player);
			    return;
			}
			
		}
		
		String homeName = args.get(0);
		
		Home home = homeManager.getHome(player, homeName);
		if(home == null) {
			messages.getAndSend(sender, "command.home.error.not-exist");
			return;
		}
		
		player.teleport(home.getLocation());
		messages.sendFormatted(sender, "command.home.success.by-name", "%name%", homeName);
	}

}

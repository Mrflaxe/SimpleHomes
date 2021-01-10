package ru.mrflaxe.simplehomes.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.mrflaxe.simplehomes.managers.GUIManager;
import ru.mrflaxe.simplehomes.managers.HomeManager;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.OmnipotentCommand;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;

public class CommandSethome extends OmnipotentCommand {

	private final Messages messages;
	private final Configuration config;
	private final HomeManager homeManager;
	private final GUIManager guiManager;
	
	public CommandSethome(Messages messages, Configuration config, HomeManager homeManager, GUIManager guiManager) {
		super("sethome", null, "simplehomes.command.sethome", 1, messages);
		
		this.messages = messages;
		this.config = config;
		this.homeManager = homeManager;
		this.guiManager = guiManager;
	}

	@Override
	protected void executeCommand(CommandSender sender, CommandArguments args) {
		Player player = (Player) sender;
		
		// Can a player make more homes than he has now?
		int homeCount = homeManager.getHomeCount(player);
		int homeLimit = guiManager.getHomesLimit(player);
//		if(homeCount >= homeLimit || homeCount >= 45) {
//			messages.getAndSend(sender, "command.sethome.error.limit-reached");
//			return;
//		}
		
		String homeName = args.get(0);
		int length = homeName.length();
		int min = config.getInt("homes.min-name-size");
		int max = config.getInt("homes.max-name-size");
		
		// Checks for length of home name
		if(length < min || length > max) {
			String message = messages.getFormatted("command.sethome.error.out-of-bounds", "%min-count%", min, "%max-count%", max);
			player.sendMessage(message);
			return;
		}
		
		// Is already exist this home or not
		if(homeManager.isExist(player, homeName)) {
			messages.getAndSend(sender, "command.sethome.error.already-exist");
			return;
		}
		
		// creating home
		homeManager.createHome(player, homeName, player.getLocation());
		messages.sendFormatted(player, "command.sethome.success", "%name%", homeName);
		return;
	}
}

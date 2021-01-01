package ru.mrflaxe.simplehomes.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.mrflaxe.simplehomes.database.Home;
import ru.mrflaxe.simplehomes.managers.HomeManager;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.PlayerOnlyCommand;
import ru.soknight.lib.configuration.Messages;

public class CommandHome extends PlayerOnlyCommand {

	private final Messages messages;
	private final HomeManager homeManager;
	
	public CommandHome(Messages messages, HomeManager homeManager) {
		super("home", "sh.command.home", messages);
		
		this.messages = messages;
		this.homeManager = homeManager;
	}

	@Override
	protected void executeCommand(CommandSender sender, CommandArguments args) {
		Player player = (Player) sender;
		String name = sender.getName();
		
		if(args.size() == 0) {
			if(homeManager.getHomeCount(name) == 0) {
				messages.getAndSend(sender, "command.home.error.empty");
				return;
			}
			
			if(homeManager.getHomeCount(name) == 1) {
				Home home = homeManager.getFirstHome(name);
				player.teleport(home.getLocation());
				
				messages.getAndSend(sender, "command.home.succes.by-first");
				return;
			} else {
				
			// TODO Тут будет открыватья менюшка с хомами игрока
			}
		}
		
		String homeName = args.get(0);
		
		Home home = homeManager.getHome(name, homeName);
		if(home == null) {
			messages.getAndSend(sender, "command.home.error.not-exist");
			return;
		}
		
		player.teleport(home.getLocation());
		messages.getAndSend(sender, "command.home.success.by-name");
	}

}
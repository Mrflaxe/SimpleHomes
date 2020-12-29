package ru.mrflaxe.simplehomes.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import ru.mrflaxe.simplehomes.managers.HomeManager;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.PlayerOnlyCommand;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;

public class CommandSethome extends PlayerOnlyCommand {

	private final Messages messages;
	private final Configuration config;
	private final HomeManager homeManager;
	
	public CommandSethome(Messages messages, Configuration config, HomeManager homeManager) {
		super("sethome", "sh.command.sethome", messages);
		
		this.messages = messages;
		this.config = config;
		this.homeManager = homeManager;
	}

	@Override
	protected void executeCommand(CommandSender sender, CommandArguments args) {
		String name = sender.getName();
		Player player = (Player) sender;
		
		int homeCount = homeManager.getHomeCount(name);
		int homeLimit = getHomesLimit(player);
		if(homeCount >= homeLimit) {
			messages.getAndSend(sender, "command.sethome.error.after-limit");
			return;
		}
		
		// Проверка на наличие имени дома
		if(args.size() == 0) {
			messages.getAndSend(sender, "command.sethome.error.wrong-syntax");
			return;
		}
		
		String homeName = args.get(0);
		int length = homeName.length();
		int min = config.getInt("homes.min-name-size");
		int max = config.getInt("homes.max-name-size");
		
		// Проверка на длину имени дома
		if(length < min || length > max) {
			String message = messages.getFormatted("command.sethome.error.out-of-bounds", "%min-count%", min, "%max-count%", max);
			player.sendMessage(message);
			return;
		}
		
		// Существует ли уже этот дом у игрока
		if(homeManager.isExist(name, homeName)) {
			messages.getAndSend(sender, "command.sethome.error.already-exist");
			return;
		}
		
		homeManager.createHome(name, homeName, player.getLocation());
		String message = messages.getFormatted("command.sethome.succes", "%name%", homeName);
		player.sendMessage(message);
		return;
	}
	
	private int getHomesLimit(Player player) {
        int limit = player.getEffectivePermissions()
                .parallelStream()
                .filter(PermissionAttachmentInfo::getValue)
                .map(PermissionAttachmentInfo::getPermission)
                .filter(p -> p.startsWith("simplehomes.limit.multiple."))
                .filter(p -> p.length() > 15)
                .map(p -> p.substring(15))
                .map(this::getAsInteger)
                .filter(i -> i != null)
                .mapToInt(Integer::intValue)
                .sum();
        return limit;
    }
    
    private Integer getAsInteger(String source) {
        try {
            return Integer.parseInt(source);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

}

package ru.mrflaxe.simplehomes.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.mrflaxe.simplehomes.managers.HomeManager;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.PlayerOnlyCommand;
import ru.soknight.lib.configuration.Messages;

public class CommandHomes extends PlayerOnlyCommand {

    private final Messages messages;
    private final HomeManager homeManager;
    
    public CommandHomes(Messages messages, HomeManager homeManager) {
        super("homes", "simplehomes", messages);
        
        this.messages = messages;
        this.homeManager = homeManager;
    }

    @Override
    protected void executeCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) sender;
        int size = args.size();
        
        if(size == 0) {
            if(homeManager.getHomeCount(player) == 0) {
                messages.getAndSend(player, "command.homes.no-homes");
                return;
            }
            
            
        }
        
    }

}

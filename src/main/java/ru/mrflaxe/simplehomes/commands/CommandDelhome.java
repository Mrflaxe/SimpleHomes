package ru.mrflaxe.simplehomes.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import ru.mrflaxe.simplehomes.managers.HomeManager;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.OmnipotentCommand;
import ru.soknight.lib.configuration.Messages;

public class CommandDelhome extends OmnipotentCommand {

    private final Messages messages;
    private final HomeManager homeManager;
        
    public CommandDelhome(Messages messages, HomeManager homeManager) {
        super("delhome", null, "simplehomes.command.delhome", 1, messages);
        
        this.messages = messages;
        this.homeManager = homeManager;
    }

    @Override
    protected void executeCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) sender;
        String homeName = args.get(0);
        
        // Checks for existing home
        if(!homeManager.isExist(player, homeName)) {
            messages.getAndSend(player, "command.delhome.error.not-exist");
            return;
        }
        
        homeManager.deleteHome(player, homeName);
        messages.sendFormatted(player, "command.delhome.success", "%name%", homeName);
        
    }

}

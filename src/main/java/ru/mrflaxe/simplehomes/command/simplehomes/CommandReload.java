package ru.mrflaxe.simplehomes.command.simplehomes;

import org.bukkit.command.CommandSender;

import ru.mrflaxe.simplehomes.SimpleHomes;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.subcommand.PermissibleSubcommand;
import ru.soknight.lib.configuration.Messages;

public class CommandReload extends PermissibleSubcommand {
    
    private final SimpleHomes plugin;
    private final Messages messages;
    
    public CommandReload(SimpleHomes plugin, Messages messages) {
        super("simplehomes.command.reload", messages);
        
        this.plugin = plugin;
        this.messages = messages;
    }

    @Override
    protected void executeCommand(CommandSender sender, CommandArguments args) {
        plugin.reload();
        
        messages.getAndSend(sender, "reload-success");
    }

}

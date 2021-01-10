package ru.mrflaxe.simplehomes.commands.sub;

import org.bukkit.command.CommandSender;

import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.subcommand.PermissibleSubcommand;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;

public class SubcommandReload extends PermissibleSubcommand {
    
    private final Messages messages;
    private final Configuration config;
    
    public SubcommandReload(Messages messages, Configuration config) {
        super("simplehomes.command.reload", messages);
        
        this.messages = messages;
        this.config = config;
    }

    @Override
    protected void executeCommand(CommandSender sender, CommandArguments args) {
        messages.refresh();
        config.refresh();
        
        messages.getAndSend(sender, "command.reload.success");
    }

}

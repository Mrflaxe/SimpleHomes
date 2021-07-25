package ru.mrflaxe.simplehomes.command.simplehomes;

import ru.soknight.lib.command.enhanced.help.command.EnhancedHelpExecutor;
import ru.soknight.lib.command.response.CommandResponseType;
import ru.soknight.lib.configuration.Messages;

public class CommandHelp extends EnhancedHelpExecutor {

    public CommandHelp(Messages messages) {
        super(messages);
        
        super.setHeaderFrom("help.header");
        super.setFooterFrom("help.footer");
        
        super.factory()
            .helpLineFormatFrom("help.body")
            .permissionFormat("simplehomes.command.%s")
            
            // /simplehomes help
            .newLine()
                    .command("simplehomes help")
                    .descriptionFrom("help")
                    .permission("help")
                    .add()
             
            // /home [name]
            .newLine()
                    .command("home", true)
                    .argumentFrom("name")
                    .add()
                    
            // /homes [player] [page]
            .newLine()
                    .command("homes", true)
                    .argumentsFrom("player", "page")
                    .add()
            
            // /sethome <name>
            .newLine()
                    .command("sethome", true)
                    .argumentFrom("name")
                    .add()
            
            // /delhome <name>
            .newLine()
                    .command("delhome", true)
                    .argumentFrom("name")
                    .add()
                    
            // /simplehomes reload
            .newLine()
                    .command("simplehomes reload")
                    .descriptionFrom("reload")
                    .permission("reload")
                    .add();
        
        super.completeMessage();
        
        super.setPermission("simplehomes.command.help");
        super.setResponseMessageByKey(CommandResponseType.NO_PERMISSIONS, "error.no-permissions");
    }

}

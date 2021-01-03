package ru.mrflaxe.simplehomes.commands.sub;

import ru.soknight.lib.command.enhanced.help.command.EnhancedHelpExecutor;
import ru.soknight.lib.command.response.CommandResponseType;
import ru.soknight.lib.configuration.Messages;

public class SubcommandHelp extends EnhancedHelpExecutor {

    public SubcommandHelp(Messages messages) {
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
            
            // /sethome <name>
            .newLine()
                    .command("sethome", true)
                    .argumentsFrom("home-name")
                    .add()
             
            // /home <name>
            .newLine()
                    .command("home", true)
                    .argumentsFrom("home-name")
                    .add()
            
            // /delhome <name>
            .newLine()
                    .command("delhome", true)
                    .argumentsFrom("home-name")
                    .add();
        
        super.completeMessage();
        
        super.setPermission("simplehomes.command.help");
        super.setResponseMessageByKey(CommandResponseType.NO_PERMISSIONS, "error.no-permissions");
    }

}

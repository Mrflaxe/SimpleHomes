package ru.mrflaxe.simplehomes.command;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrflaxe.simplehomes.database.DatabaseManager;
import ru.mrflaxe.simplehomes.database.model.HomeModel;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.OmnipotentCommand;
import ru.soknight.lib.configuration.Messages;

import java.util.List;
import java.util.stream.Collectors;

public class CommandDelhome extends OmnipotentCommand {

    private final Messages messages;
    private final DatabaseManager databaseManager;
        
    public CommandDelhome(JavaPlugin plugin, Messages messages, DatabaseManager databaseManager) {
        super("delhome", null, "simplehomes.command.delhome", 1, messages);
        
        this.messages = messages;
        this.databaseManager = databaseManager;

        super.register(plugin, true);
    }

    @Override
    protected void executeCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) sender;
        String homeName = args.get(0);
        
        databaseManager.getHome(player.getName(), homeName).thenAccept(home -> {
            if(home == null) {
                messages.sendFormatted(player, "delhome.failed.unknown-home", "%name%", homeName);
                return;
            }
            
            databaseManager.removeHome(home);
            messages.sendFormatted(player, "delhome.success", "%name%", homeName);
        });
    }
    
    @Override
    protected List<String> executeTabCompletion(CommandSender sender, CommandArguments args) {
        if(args.size() != 1) return null;
        
        List<HomeModel> homes = databaseManager.getHomes(sender.getName()).join();
        if(homes == null || homes.isEmpty()) return null;
        
        String arg = getLastArgument(args, true);
        return homes.stream()
                .map(HomeModel::getHomeName)
                .filter(n -> n.toLowerCase().startsWith(arg))
                .collect(Collectors.toList());
    }

}

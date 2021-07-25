package ru.mrflaxe.simplehomes.command;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrflaxe.simplehomes.database.DatabaseManager;
import ru.mrflaxe.simplehomes.database.model.HomeModel;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.PlayerOnlyCommand;
import ru.soknight.lib.component.injection.TextComponentInjector;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.tool.CollectionsTool;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandHomes extends PlayerOnlyCommand {
    
    private static final TextComponent EMPTY_COMPONENT = new TextComponent();

    private final Messages messages;
    private final DatabaseManager databaseManager;
    
    public CommandHomes(JavaPlugin plugin, Messages messages, DatabaseManager databaseManager) {
        super("homes", "simplehomes.command.homes", messages);
        
        this.messages = messages;
        this.databaseManager = databaseManager;

        super.register(plugin, true);
    }

    @Override
    protected void executeCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) sender;
        
        String owner0 = player.getName();
        int page0 = 1;
        
        if(args.size() == 1) {
            String arg = args.get(0);
            // if this argument is integer -> that is page
            // overwise that is target clan tag
            if(isInteger(arg))
                page0 = args.getAsInteger(0);
            else
                owner0 = arg;
        } else if(args.size() == 2) {
            // in this case both arguments is specified
            owner0 = args.get(0);
            page0 = args.getAsInteger(1);
        }
        
        // page may be invalid
        int page = page0;
        if(page <= 0) {
            messages.sendFormatted(sender, "error.arg-is-not-int", "%arg%", args);
            return;
        }
        
        boolean other0 = false;
        if(player.hasPermission("simplehomes.command.homes.other"))
            other0 = !owner0.equals(player.getName());
        else
            owner0 = player.getName();
        
        // getting effectively final vars
        String owner = owner0;
        boolean other = other0;
        
        // database queries should be executed asynchronously
        databaseManager.getHomes(owner).thenAccept(homes -> {
            if(homes == null || homes.isEmpty()) {
                String path = "homes.failed.no-homes." + (other ? "other" : "own");
                messages.sendFormatted(sender, path, "%player%", owner);
                return;
            }
            
            int pageSize = messages.getInt("homes.page-size", 10);
            int total = homes.size() / pageSize;
            if(homes.size() % pageSize != 0) total++;
            
            if(page > total) {
                messages.sendFormatted(sender, "homes.failed.page-is-empty", "%page%", page);
                return;
            }
            
            // homes sorting
            homes.sort(HomeModel::compareTo);
            
            // homes to show on selected page
            List<HomeModel> onpage = CollectionsTool.getSubList(homes, pageSize, page);
            
            // determining the go buttons mode
            boolean doAddGoButtons = sender.hasPermission("simplehomes.command.home.other");
            
            // getting header
            String path = "homes.header." + (other ? "other" : "own");
            String header = messages.getFormatted(path,
                    "%player%", owner,
                    "%page%", page,
                    "%total%", total
            );
            
            // sending message
            List<BaseComponent> output = new ArrayList<>();
            BaseComponent breakline = new TextComponent("\n");
            
            output.add(new TextComponent(header));
            
            onpage.stream()
                    .map(home -> formatHomeModel(home, !other, doAddGoButtons))
                    .forEach(component -> {
                        output.add(breakline);
                        output.add(component);
                    });
            
            output.add(breakline);
            output.add(new TextComponent(messages.get("homes.footer")));
            
            BaseComponent[] array = output.toArray(new BaseComponent[0]);
            sender.spigot().sendMessage(array);
        });
    }
    
    @Override
    protected List<String> executeTabCompletion(CommandSender sender, CommandArguments args) {
        if(args.size() != 1 || !sender.hasPermission("simplehomes.command.homes.other")) return null;
        
        String arg = getLastArgument(args, true);
        return Arrays.stream(Bukkit.getOfflinePlayers())
                .map(OfflinePlayer::getName)
                .filter(n -> n.toLowerCase().startsWith(arg))
                .collect(Collectors.toList());
    }
    
    private TextComponent formatHomeModel(HomeModel homeModel, boolean itsMy, boolean addGoButton) {
        TextComponent button = addGoButton || itsMy ? getGoButton(homeModel) : EMPTY_COMPONENT;
        
        String text = messages.getFormatted("homes.body",
                "%home_name%", homeModel.getHomeName(),
                "%world%", homeModel.getWorld(),
                "%x%", homeModel.getX(),
                "%y%", homeModel.getY(),
                "%z%", homeModel.getZ()
        );
        
        return TextComponentInjector.inject(text, "%button%", button);
    }
    
    private TextComponent getGoButton(HomeModel home) {
        String text = messages.get("homes.button.text");
        String hover = messages.getFormatted("homes.button.hover", "%player%", home.getHolder());
        
        String command = messages.getString("homes.button.command");
        command = messages.format(command, "%player%", home.getHolder(), "%home_name%", home.getHomeName());
        
        TextComponent button = new TextComponent(text);
        button.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        button.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(hover)));
        return button;
    }

}

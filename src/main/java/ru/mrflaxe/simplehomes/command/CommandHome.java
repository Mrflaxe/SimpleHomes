package ru.mrflaxe.simplehomes.command;


import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import ru.mrflaxe.simplehomes.database.DatabaseManager;
import ru.mrflaxe.simplehomes.database.model.HomeModel;
import ru.mrflaxe.simplehomes.menu.MenuProvider;
import ru.soknight.lib.argument.CommandArguments;
import ru.soknight.lib.command.preset.standalone.PlayerOnlyCommand;
import ru.soknight.lib.configuration.Configuration;
import ru.soknight.lib.configuration.Messages;
import ru.soknight.lib.cooldown.preset.PlayersCooldownStorage;
import ru.soknight.lib.format.DateFormatter;

import java.util.List;
import java.util.stream.Collectors;

public class CommandHome extends PlayerOnlyCommand {
    
    private static final DateFormatter DATE_FORMATTER;
    private static final String BYPASS_PERM = "simplehomes.bypass.cooldown";
    
    private final Plugin plugin;
    private final Configuration config;
    private final Messages messages;
    
    private final DatabaseManager databaseManager;
    private final MenuProvider menuProvider;

    private final PlayersCooldownStorage cooldownStorage;
    
    public CommandHome(
            JavaPlugin plugin,
            Configuration config,
            Messages messages,
            DatabaseManager databaseManager,
            MenuProvider menuProvider,
            PlayersCooldownStorage cooldownStorage
    ) {
        super("home", "simplehomes.command.home", messages);
        
        this.plugin = plugin;
        this.messages = messages;
        this.config = config;
        
        this.databaseManager = databaseManager;
        this.menuProvider = menuProvider;
        
        this.cooldownStorage = cooldownStorage;

        super.register(plugin, true);
    }

    @Override
    protected void executeCommand(CommandSender sender, CommandArguments args) {
        Player player = (Player) sender;
        
        if(args.isEmpty()) {
            databaseManager.getHomes(player.getName()).thenAccept(homes -> {
                int homeAmount = homes != null ? homes.size() : 0;
                if(homeAmount == 0) {
                    messages.getAndSend(sender, "home.failed.no-homes");
                    return;
                }
                
                // cooldown check
                if(config.getBoolean("cooldown.enable", false) && !player.hasPermission(BYPASS_PERM)) {
                    long remainedTime = cooldownStorage.getRemainedTime(player.getName());
                    if(remainedTime != -1) {
                        int remainedSeconds = (int) (remainedTime / 1000);
                        if(remainedTime % 1000 != 0) remainedSeconds++;
                        
                        if(remainedSeconds > 0) {
                            messages.sendFormatted(player, "home.failed.cooldown",
                                    "%time%", DATE_FORMATTER.format(remainedSeconds)
                            );
                            return;
                        }
                    }
                }
                
                // if a player has only 1 home
                if(homeAmount == 1) {
                    HomeModel home = databaseManager.getFirstHome(player.getName()).join();
                    Location location = home.getLocation();
                    if(location == null) {
                        messages.getAndSend(sender, "home.failed.unknown-world");
                        return;
                    }
                    
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.teleport(location));
                    cooldownStorage.refreshResetDate(player.getName());
                    
                    messages.getAndSend(sender, "home.success.only-one");
                } else {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> menuProvider.openSession(player, homes).view());
                }
            });
            
            return;
        }
        
        String homeName0 = args.get(0);
        String owner0 = player.getName();
        boolean other0 = false;
        
        if(homeName0.contains(":")) {
            int indexOf = homeName0.indexOf(':');
            if(indexOf != 0 && indexOf < homeName0.length()) {
                owner0 = homeName0.substring(0, indexOf);
                homeName0 = homeName0.substring(indexOf + 1);
                if(!player.hasPermission("simplehomes.command.home.other"))
                    owner0 = player.getName();
                else
                    other0 = !owner0.equals(player.getName());
            }
        }
        
        // getting effectively final vars
        String homeName = homeName0;
        String owner = owner0;
        boolean other = other0;
        
        // cooldown check
        if(!other && config.getBoolean("cooldown.enable", false) && !player.hasPermission(BYPASS_PERM)) {
            long remainedTime = cooldownStorage.getRemainedTime(player.getName());
            if(remainedTime != -1) {
                int remainedSeconds = (int) (remainedTime / 1000);
                if(remainedTime % 1000 != 0) remainedSeconds++;
                
                if(remainedSeconds > 0) {
                    messages.sendFormatted(player, "home.failed.cooldown",
                            "%time%", DATE_FORMATTER.format(remainedSeconds)
                    );
                    return;
                }
            }
        }
        
        // teleporting by home name
        databaseManager.getHome(owner, homeName).thenAccept(home -> {
            if(home == null) {
                String path = "home.failed.unknown-home." + (other ? "other" : "own");
                messages.sendFormatted(sender, path, "%name%", homeName, "%player%", owner);
                return;
            }
            
            Location location = home.getLocation();
            if(location == null) {
                messages.getAndSend(sender, "home.failed.unknown-world");
                return;
            }
            
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> player.teleport(location));
            
            if(other) {
                messages.sendFormatted(sender, "home.success.other", "%name%", homeName, "%player%", owner);
            } else {
                cooldownStorage.refreshResetDate(player.getName());
                messages.sendFormatted(sender, "home.success.by-name", "%name%", homeName);
            }
        });
    }
    
    @Override
    protected List<String> executeTabCompletion(CommandSender sender, CommandArguments args) {
        if(args.size() != 1) return null;
        
        if(sender.hasPermission("simplehomes.command.home.other")) {
            String input = getLastArgument(args);
            
            if(input.contains(":")) {
                int indexOf = input.indexOf(':');
                
                if(indexOf != 0) {
                    String owner = input.substring(0, indexOf);
                    String homeName = input.substring(indexOf + 1);
                    
                    List<HomeModel> homes = databaseManager.getHomes(owner).join();
                    if(homes == null || homes.isEmpty()) return null;
                    
                    String homeLower = homeName.toLowerCase();
                    return homes.stream()
                            .map(HomeModel::getHomeName)
                            .filter(n -> n.toLowerCase().startsWith(homeLower))
                            .map(n -> owner + ":" + n)
                            .collect(Collectors.toList());
                }
            }
            
            List<HomeModel> homes = databaseManager.getHomes(sender.getName()).join();
            if(homes == null || homes.isEmpty()) return null;
            
            String arg = input.toLowerCase();
            List<String> output = homes.stream()
                    .map(HomeModel::getHomeName)
                    .filter(n -> n.toLowerCase().startsWith(input))
                    .collect(Collectors.toList());
            
            Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(n -> n.toLowerCase().startsWith(arg))
                    .map(n -> n + ":")
                    .forEach(output::add);
            
            return output;
        }
        
        List<HomeModel> homes = databaseManager.getHomes(sender.getName()).join();
        if(homes == null || homes.isEmpty()) return null;
        
        String arg = getLastArgument(args, true);
        return homes.stream()
                .map(HomeModel::getHomeName)
                .filter(n -> n.toLowerCase().startsWith(arg))
                .collect(Collectors.toList());
    }

    static {
        DATE_FORMATTER = new DateFormatter();
        DATE_FORMATTER.loadRussianTimeUnitFormats();
    }
    
}

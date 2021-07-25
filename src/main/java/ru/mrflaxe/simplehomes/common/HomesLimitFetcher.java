package ru.mrflaxe.simplehomes.common;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class HomesLimitFetcher {

    public static int getHomesLimit(Player player) {
        if(player.hasPermission("simplehomes.limit.unlimited"))
            return -1;

        return player.getEffectivePermissions().stream()
                .mapToInt(HomesLimitFetcher::getPermissionValue)
                .sum();
    }
    
    private static int getPermissionValue(PermissionAttachmentInfo attachment) {
        String permission = attachment.getPermission();
        if(!permission.startsWith("simplehomes.limit.") || permission.length() < 18)
            return 0;
        
        int indexOf = permission.lastIndexOf(".");
        if(permission.endsWith(".") || indexOf == -1)
            return 0;
        
        String value = permission.substring(indexOf + 1);
        int asInteger = asInteger(value);
        if(asInteger == 0)
            return 0;
        
        boolean enabled = attachment.getValue();
        return enabled ? asInteger : asInteger * -1;
    }
    
    private static int asInteger(String source) {
        try {
            return Integer.parseInt(source);
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
    
}

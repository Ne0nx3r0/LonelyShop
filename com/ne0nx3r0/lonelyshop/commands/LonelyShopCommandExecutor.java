package com.ne0nx3r0.lonelyshop.commands;

import com.ne0nx3r0.lonelyshop.LonelyShopPlugin;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class LonelyShopCommandExecutor implements CommandExecutor {
    private final Map<String,LonelyCommand> subCommands;

    public LonelyShopCommandExecutor(LonelyShopPlugin plugin) {
        this.subCommands = new HashMap<>();
        
        this.registerSubcommand(new CommandSell(plugin));
        this.registerSubcommand(new CommandBuy(plugin));
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String alias, String[] args) {
        if(args.length == 0 || args[0].equals("?")) {
            this.sendUsage(cs);
            
            return true;
        }

        LonelyCommand lonelyCommand = this.subCommands.get(args[0]);
        
        if(lonelyCommand != null) {
            if(cs.hasPermission(lonelyCommand.getPermissionNode())) {
                return lonelyCommand.execute(cs,args);
            }
            else {
                lonelyCommand.send(cs, 
                    ChatColor.RED+"You do not have permission to "+lonelyCommand.getAction(),
                    ChatColor.RED+"Required node: "+ChatColor.WHITE+lonelyCommand.getPermissionNode()
                );
            }
        }
        
        return false;
    }

    private void sendUsage(CommandSender cs) {
        cs.sendMessage(ChatColor.GRAY+"---"+ChatColor.GREEN+" LonelyShop "+ChatColor.GRAY+"---");
        cs.sendMessage("Here are the commands you have access to:");
        
        for(LonelyCommand lc : this.subCommands.values()) {
            if(cs.hasPermission(lc.getPermissionNode())) {
                cs.sendMessage(lc.getUsage());
            }
        }
    }
    
    public final void registerSubcommand(LonelyCommand lc) {
        this.subCommands.put(lc.getName(), lc);
    }
}

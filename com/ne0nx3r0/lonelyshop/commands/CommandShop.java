package com.ne0nx3r0.lonelyshop.commands;

import com.ne0nx3r0.lonelyshop.LonelyShopPlugin;
import com.ne0nx3r0.lonelyshop.inventory.InventoryActionResponse;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class CommandShop extends LonelyCommand {
    private final LonelyShopPlugin plugin;

    public CommandShop(LonelyShopPlugin plugin) {
        super("shop","","open your for sale items","lonelyshop.shop");
        
        this.plugin = plugin;
    }

    @Override
    boolean execute(CommandSender cs, String[] args) {
        if(!(cs instanceof Player)) {
            this.sendError(cs,"Not from console!");
            
            return true;
        }

        InventoryActionResponse iar = plugin.shopsManager.openPlayerShop((Player) cs);
        
        if(!iar.wasSuccessful()){
            this.sendError(cs, iar.getMessage());
        }
        
        return true;
    }
    
    // Not everything fits into your happy little formatting box man!
    @Override
    public String[] getUsage() {
        return new String[]{
            "/shop",
        };
    }
}

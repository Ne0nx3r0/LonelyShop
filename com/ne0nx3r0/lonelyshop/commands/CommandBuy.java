package com.ne0nx3r0.lonelyshop.commands;

import com.ne0nx3r0.lonelyshop.LonelyShopPlugin;
import com.ne0nx3r0.lonelyshop.inventory.ItemForSale;
import java.util.ArrayList;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class CommandBuy extends LonelyCommand {
    private final LonelyShopPlugin plugin;

    public CommandBuy(LonelyShopPlugin plugin) {
        super("buy","<item type>","Search for items that are for sale","lonelyshop.buy");
        
        this.plugin = plugin;
    }

    @Override
    boolean execute(CommandSender cs, String[] args) {
        if(!(cs instanceof Player)) {
            this.sendError(cs,"Not from console!");
            
            return true;
        }
                
        if(args.length < 2)
        {
            this.send(cs,this.getUsage());

            return true;
        }
        
        String sMaterial = args[1];
        
        Material material = Material.matchMaterial(sMaterial);
        
        if(material == null){
            this.sendError(cs, sMaterial+" is not a valid material!");
            
            return true;
        }
        
        Player player = (Player) cs;

        ArrayList<ItemForSale> items = plugin.inventoryManager.getItemsForSale(material);
        
        if(items.isEmpty()) {
            this.sendError(cs, "There is no "+sMaterial+" for sale currently!");
            
            return true;
        }
        
        plugin.shopsManager.openShopInventory(player,items);
        
        return true;
    }
}

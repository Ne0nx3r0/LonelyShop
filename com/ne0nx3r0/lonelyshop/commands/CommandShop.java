package com.ne0nx3r0.lonelyshop.commands;

import com.ne0nx3r0.lonelyshop.LonelyShopPlugin;
import com.ne0nx3r0.lonelyshop.inventory.ItemForSale;
import com.ne0nx3r0.lonelyshop.shop.ShopType;
import java.util.ArrayList;
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
        
        Player player = (Player) cs;
        
        ArrayList<ItemForSale> items = plugin.inventoryManager.getSellerItems(player, 1);
        
        if(items.isEmpty()) {
            this.sendError(cs,"You don't have any items for sale!");
            
            return true;
        }
        
        plugin.shopsManager.openShopInventory(player,items,ShopType.MyForSaleItems);
        
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

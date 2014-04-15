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
        
        Player player = (Player) cs;
                
        if(args.length < 2)
        {
            this.send(cs,this.getUsage());

            return true;
        }
        
        String sMaterial = args[1];
        
        // /buy *
        if(sMaterial.equals("*")) {
            ArrayList<ItemForSale> items = plugin.inventoryManager.getItemsForSale();

            if(items.isEmpty()) {
                this.sendError(cs, "There is no "+sMaterial+" for sale currently!");
            }
            else {
                plugin.shopsManager.openShopInventory(player,items);
            }
            
            return true;
        }
        
        Material material = Material.matchMaterial(sMaterial);
        
        if(material == null){
            this.sendError(cs, sMaterial+" is not a valid material!");
            
            return true;
        }
        
        // buy <material>
        if(args.length == 2) {
            ArrayList<ItemForSale> items = plugin.inventoryManager.getItemsForSale(material);

            if(items.isEmpty()) {
                this.sendError(cs, "There are no "+sMaterial+" for sale currently!");

                return true;
            }

            plugin.shopsManager.openShopInventory(player,items);
            
            return true;
        }
 
        byte data = 0;
        
        if(args.length > 2){
            String sData = args[2];
            
            try {
                data = Byte.parseByte(sData);
            }
            catch(NumberFormatException ex){
                this.sendError(cs, sData+" is not a valid data!");
                
                return true;
            }
        }

        ArrayList<ItemForSale> items = plugin.inventoryManager.getItemsForSale(material,data);
        
        if(items.isEmpty()) {
            this.sendError(cs, "There are no "+sMaterial+"("+data+") for sale currently!");
            
            return true;
        }
        
        plugin.shopsManager.openShopInventory(player,items);
        
        return true;
    }
    
    // Not everything fits into your happy little formatting box man!
    @Override
    public String[] getUsage() {
        return new String[]{
            "/buy <material> [data]",
            "/buy *"
        };
    }
}

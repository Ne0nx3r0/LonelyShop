package com.ne0nx3r0.lonelyshop.commands;

import com.ne0nx3r0.lonelyshop.LonelyShopPlugin;
import com.ne0nx3r0.lonelyshop.inventory.InventoryActionResponse;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

class CommandSell extends LonelyCommand {
    private final LonelyShopPlugin plugin;

    public CommandSell(LonelyShopPlugin plugin) {
        super("sell","<price>","sell the item in your hand","lonelyshop.sell");
        
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
        
        String sPrice = args[1];
        
        double price = 0;
        
        try {
            price = Double.parseDouble(sPrice);
        }
        catch(NumberFormatException e) {
            this.sendError(cs, sPrice+" is not a valid amount!");
            
            return true;
        }
        
        if(price > 1000000) {
            this.sendError(cs, "The max you can sell an item for is "+plugin.economy.format(1000000));
            
            return true;
        }
        else if(price < 0){
            this.sendError(cs, "Price must be positive");
            
            return true;
        }
        
        Player player = (Player) cs;
        
        ItemStack isInHand = player.getItemInHand();
        
        if(isInHand == null || isInHand.getType().equals(Material.AIR)){
            this.sendError(cs,"No item in your hand to sell!");
            
            return true;
        }
        
        InventoryActionResponse response = plugin.inventoryManager.putItemForSale(player,isInHand,price);
        
        if(response.wasSuccessful()) {
            this.send(cs,isInHand.getType().name()+" was put up for sale for "+plugin.economy.format(price)+"!");
            
            player.setItemInHand(null);
            
            return true;
        }
        else {
            this.sendError(cs, response.getMessage());
        }
        
        return true;
    }
    
    // Not everything fits into your happy little formatting box man!
    @Override
    public String[] getUsage() {
        return new String[]{
            "/sell <price>",
        };
    }
}

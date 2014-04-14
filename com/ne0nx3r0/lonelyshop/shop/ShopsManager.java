package com.ne0nx3r0.lonelyshop.shop;

import com.ne0nx3r0.lonelyshop.LonelyShopPlugin;
import com.ne0nx3r0.lonelyshop.inventory.ItemForSale;
import com.ne0nx3r0.lonelyshop.inventory.InventoryActionResponse;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopsManager {
    private final String SHOP_TITLE = "LonelyShop";
    private final int SHOP_INVENTORY_SLOTS = 9*6;
    
    private final LonelyShopPlugin plugin;

    public ShopsManager(LonelyShopPlugin plugin) {
        this.plugin = plugin;
    }

    public void openShopInventory(Player player, ArrayList<ItemForSale> items) {
        
        Inventory inventory = plugin.getServer().createInventory(null, SHOP_INVENTORY_SLOTS, this.getShopTitle());
        
        int currentSlot = 0;
        
        for(ItemForSale item : items) {
            //reserve slots 0 & 8
            /*if(currentSlot == 0 || currentSlot == 8) {
                currentSlot++;
            }*/
            
            ItemStack is = item.getItemStack();
            
            ItemMeta meta = is.getItemMeta();
            
            List<String> lore;
            
            if(meta.hasLore()){
                lore = meta.getLore();
            }
            else{
                lore = new ArrayList<>();
            }
            
            lore.add(ChatColor.GRAY+"- LonelyShop -");
            
            String perItem = "";
            
            if(is.getAmount() > 1){
                lore.add(ChatColor.GRAY+"Amount: "+ChatColor.DARK_PURPLE+is.getAmount());
                
                perItem = ChatColor.GRAY+" ("+plugin.economy.format(item.getPricePerItem())+ChatColor.GRAY+" each)";
            }
            
            lore.add(ChatColor.GRAY+"Price: "+ChatColor.DARK_PURPLE+plugin.economy.format(item.getPrice()) + perItem);
            lore.add(ChatColor.GRAY+"Posted: "+ChatColor.DARK_PURPLE+item.getDisplayPostedDate());
            lore.add(ChatColor.GRAY+"ID: "+ChatColor.DARK_PURPLE+item.getDbID());
            
            meta.setLore(lore);
            
            is.setItemMeta(meta);
            
            inventory.setItem(currentSlot, is);
            
            currentSlot++;
            
            if(currentSlot == SHOP_INVENTORY_SLOTS) {
                break;
            }
        }
        
        player.openInventory(inventory);
    }

    public InventoryActionResponse attemptToBuy(Player player, ItemStack is) {
        if(!is.hasItemMeta() || !is.getItemMeta().hasLore()) { 
            return new InventoryActionResponse(null,false,"Invalid LonelyShop item!");
        }
        
        List<String> lore = is.getItemMeta().getLore();

        String sId = lore.get(lore.size()-1);
        
        int itemId;

        try {
            itemId = Integer.parseInt(sId.substring(sId.lastIndexOf(ChatColor.COLOR_CHAR)+2));               
        }
        catch(NumberFormatException ex){
            return new InventoryActionResponse(null,false,"Invalid LonelyShop item!");
        }
        
        return plugin.inventoryManager.attemptToBuy(player, itemId);
    }
    
    public String getShopTitle() {
        return this.SHOP_TITLE;
    }    
    
    public int getMaxShopSlots() {
        return this.SHOP_INVENTORY_SLOTS;
    }

    public void closeAllShops() {
        for(Player player : plugin.getServer().getOnlinePlayers()){
            HumanEntity hent = player;
            
            if(hent.getOpenInventory().getTitle().equals(this.SHOP_TITLE)) {
                player.closeInventory();
                
                player.sendMessage(ChatColor.RED+"LonelyShops was disabled or reloaded.");
            }
        }
    }
}

package com.ne0nx3r0.lonelyshop.shop;

import com.ne0nx3r0.lonelyshop.LonelyShopPlugin;
import com.ne0nx3r0.lonelyshop.inventory.ItemForSale;
import com.ne0nx3r0.lonelyshop.inventory.InventoryActionResponse;
import com.ne0nx3r0.util.TimeSince;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ShopsManager {
    private final String SHOPS_STARTER = "LonelyShop";
    private final String SHOP_ENDING_YOUR_ITEMS = " - Your Items";
    private final int SHOP_INVENTORY_SLOTS = 9*6;
    
    private final LonelyShopPlugin plugin;
    private final String CLICK_TO_RETREIVE = ChatColor.GREEN+"Click to retreive";
    private final String CLICK_TO_BUY = ChatColor.GREEN+"Click to Buy";
    private final String CLICK_TO_CONFIRM = ChatColor.DARK_GREEN+"Click again"+ChatColor.GREEN+" to confirm";

    public ShopsManager(LonelyShopPlugin plugin) {
        this.plugin = plugin;
    }

    public void openShopInventory(Player player, ArrayList<ItemForSale> items) {
        
        Inventory inventory = plugin.getServer().createInventory(null, SHOP_INVENTORY_SLOTS, this.getShopsStarter());
        
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
            
            lore.add(this.CLICK_TO_BUY);
            
            String perItem = "";
            
            if(is.getAmount() > 1){
                lore.add(ChatColor.GRAY+"Amount: "+ChatColor.DARK_PURPLE+is.getAmount());
                
                perItem = ChatColor.GRAY+" ("+plugin.economy.format(item.getPricePerItem())+ChatColor.GRAY+" each)";
            }
            
            lore.add(ChatColor.GRAY+"Price: "+ChatColor.DARK_PURPLE+plugin.economy.format(item.getPrice()) + perItem);
            lore.add(ChatColor.GRAY+"Posted: "+ChatColor.DARK_PURPLE+TimeSince.getTimeSinceString(item.getPostedDate()));
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

    public InventoryActionResponse openPlayerShop(Player player) {
        ArrayList<ItemForSale> items = plugin.inventoryManager.getSellerItems(player);
        
        if(items.isEmpty()){
            return new InventoryActionResponse(null,false,"You don't have any items for sale currently!");
        }
        
        Inventory inventory = plugin.getServer().createInventory(null, SHOP_INVENTORY_SLOTS, this.SHOPS_STARTER+this.SHOP_ENDING_YOUR_ITEMS);
        
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
            
            lore.add(this.CLICK_TO_RETREIVE);
            
            String perItem = "";
            
            if(is.getAmount() > 1){
                lore.add(ChatColor.GRAY+"Amount: "+ChatColor.DARK_PURPLE+is.getAmount());
                
                perItem = ChatColor.GRAY+" ("+plugin.economy.format(item.getPricePerItem())+ChatColor.GRAY+" each)";
            }
            
            lore.add(ChatColor.GRAY+"Price: "+ChatColor.DARK_PURPLE+plugin.economy.format(item.getPrice()) + perItem);
            lore.add(ChatColor.GRAY+"Posted: "+ChatColor.DARK_PURPLE+TimeSince.getTimeSinceString(item.getPostedDate()));
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
        
        return new InventoryActionResponse(null,true,"Opened your shop!");
    }

    public String getShopsStarter() {
        return this.SHOPS_STARTER;
    }    
    
    public int getMaxShopSlots() {
        return this.SHOP_INVENTORY_SLOTS;
    }

    public void closeAllShops() {
        for(Player player : plugin.getServer().getOnlinePlayers()){
            HumanEntity hent = player;
            
            if(hent.getOpenInventory().getTitle().equals(this.SHOPS_STARTER)) {
                player.closeInventory();
                
                player.sendMessage(ChatColor.RED+"LonelyShops was disabled or reloaded.");
            }
        }
    }
    
    public void takeShopAction(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        ItemStack is = e.getCurrentItem();
        String shopTitle = e.getInventory().getTitle();
        
        if(!is.hasItemMeta() || !is.getItemMeta().hasLore()) { 
            player.sendMessage("Invalid LonelyShop item!");
            
            return;
        }
        
        List<String> lore = is.getItemMeta().getLore();

        String sId = lore.get(lore.size()-1);

        int itemId;

        try {
            itemId = Integer.parseInt(sId.substring(sId.lastIndexOf(ChatColor.COLOR_CHAR)+2));               
        }
        catch(NumberFormatException ex){
            player.sendMessage("Invalid LonelyShop item!");

            return;
        }
        
        if(shopTitle.equals(this.SHOPS_STARTER+this.SHOP_ENDING_YOUR_ITEMS)) {
            InventoryActionResponse response = plugin.inventoryManager.attemptToHandBackToSeller(player, itemId);

            if(response.wasSuccessful()) {
                e.getClickedInventory().setItem(e.getRawSlot(), new ItemStack(Material.AIR));

                // the original itemstack without additional shop metadata
                ItemStack isGiveToPlayer = response.getItemStack();

                e.setCursor(isGiveToPlayer);

                player.sendMessage(ChatColor.GREEN+response.getMessage());
            }
            else {
                player.sendMessage(ChatColor.RED+response.getMessage());
            }
        }
        else {//size - 5
            if(lore.get(lore.size() - 4).equals(this.CLICK_TO_BUY)) {
                player.sendMessage(this.CLICK_TO_CONFIRM);
                
                lore.set(lore.size() - 4,this.CLICK_TO_CONFIRM);
                
                ItemMeta meta = is.getItemMeta();
                
                meta.setLore(lore);
                
                is.setItemMeta(meta);
                
                return;
            }
            
            InventoryActionResponse response = plugin.inventoryManager.attemptToBuy(player, itemId);

            if(response.wasSuccessful()) {
                e.getClickedInventory().setItem(e.getRawSlot(), new ItemStack(Material.AIR));

                // the original itemstack without additional shop metadata
                ItemStack isGiveToPlayer = response.getItemStack();

                e.setCursor(isGiveToPlayer);

                player.sendMessage(ChatColor.GREEN+response.getMessage());
            }
            else {
                player.sendMessage(ChatColor.RED+response.getMessage());
            }
        }
    }
}
